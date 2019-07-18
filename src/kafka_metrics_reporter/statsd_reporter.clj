(ns kafka-metrics-reporter.statsd-reporter
  (:require
    [clojure.core.async :refer [go-loop timeout <!]]
    [clojure.string :as s]
    [clojure.tools.logging :as log]
    [cognician.dogstatsd :as d]
    [com.stuartsierra.component :as component]
    [environ.core :refer [env]]
    [kafka-metrics-reporter.core :as kmc])
  (:import (org.apache.kafka.common MetricName)
           (org.apache.kafka.common.metrics KafkaMetric)))

(def dogstatsd (env :dogstatsd-uri "localhost:8125"))

(defn send-metrics [metrics]
  (doseq [^KafkaMetric m metrics]
    (let [metric-value (.value m)
          metric-name (.metricName m)
          statsd-metrics-name (-> (format "kafka.%s.%s" (.group metric-name) (.name metric-name))
                                  (s/replace "-" "_"))
          statsd-opts {:tags (.tags ^MetricName metric-name)}]
      (d/gauge! statsd-metrics-name metric-value statsd-opts))))

(defn start-reporting-loop [this]
  (go-loop [metrics (vals @kmc/metrics)]
    (send-metrics metrics)
    (<! (timeout (:reporting-freq-in-ms this)))
    (when @(:started this) (recur (vals @kmc/metrics)))))

(defrecord StatsdReporter [reporting-freq-in-ms app-id app-name environment started]
  component/Lifecycle
  (start [this]
    (log/info "Starting DogStatsD Reporter")
    (let [component (assoc this :started (atom true))
          tags {:environment environment
                :app-name app-name
                :runtime-id app-id}]
      (d/configure! dogstatsd {:tags tags})
      (start-reporting-loop component)
      component))
  (stop [this]
    (log/info "Stopping DogStatsD Reporter")
    (reset! (:started this) false)
    this))
