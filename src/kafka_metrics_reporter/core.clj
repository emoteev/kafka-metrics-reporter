(ns kafka-metrics-reporter.core
  (:import (org.apache.kafka.common.metrics KafkaMetric)
           (java.util List))
  (:gen-class
    :name kafka_metrics_reporter.KafkaMetricsReporter
    :implements [org.apache.kafka.common.metrics.MetricsReporter]
    :init init-class
    :state state
    :prefix "-"
    :main false))

(def metrics (atom {}))

(defn metric->store-key [^KafkaMetric metric]
  (-> metric .metricName .hashCode))

(defn -init-class []
  [[] {:metrics metrics :started (atom false)}])

;; Given argument is List<KafkaMetric>
(defn -init [this ^List kafka-metrics]
  (reset! (:started (.state this)) true)
  (reset! (:metrics (.state this))
          (reduce #(assoc %1 (metric->store-key %2) %2)
                  {}
                  kafka-metrics)))

(defn -metricChange [this ^KafkaMetric metric]
  (swap! (:metrics (.state this)) assoc (metric->store-key metric) metric))

(defn -metricRemoval [this ^KafkaMetric metric]
  (swap! (:metrics (.state this)) dissoc (metric->store-key metric)))

(defn -configure [_ _])

(defn -close [this]
  (reset! (:started (.state this)) false)
  (reset! (:metrics (.state this)) {}))
