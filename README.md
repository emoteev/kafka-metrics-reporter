[![Clojars Project](https://img.shields.io/clojars/v/kafka_metrics_reporter.svg)](https://clojars.org/kafka_metrics_reporter)

# DogStatsd Kafka Metrics Reporter

Kafka Metrics Reporter is a DogStatsd reporter for Kafka metrics.

Kafka's StreamConfig `metrics.reporters` should be set to `["kafka_metrics_reporter.KafkaMetricsReporter"]`

statsd-reporter is provided as a component, and should be initialized with the following parameters:
- reporting-freq-in-ms
- app-id
- app-name 
- environment

The former is the frequency in milliseconds the reporter will attempt to send the metrics to the dogstatsd process.  
The latters will be attached as _tags_ to the metrics.

*NOTE:* dogstatsd process URI is set be default to `localhost:8125` and is overridable throught the `DOGSTATSD_URI` environment variable.

## Example setup for a Kafka Stream job application

```
(defn job-config [reporting-freq-in-ms app-id app-name environment kafka-broker-uri]
  {:environment environment
   :app-id id
   :app-name app-name
   :reporting-freq-in-ms reporting-freq-in-ms
   :kafka-broker-uri kafka-broker-uri})

(defn new-system [config]
  (let [system {:statsd-reporter (statsd/map->StatsdReporter config)
                :job (stream/map->Job config)}]
    (component/map->SystemMap system)))

(defn -main [& _]
  (log/info "Starting Kafka Stream job")
  (-> job-config
      new-system
      component/start))
```

## Changelog

### 0.3.0 - July 19, 2019

- Initial version

## License

Copyright © 2019 Jérémy Van de Wyngaert

Licensed under MIT (see [LICENSE](LICENSE)).
