# New Relic OpenTelemetry Span Support (APM Java)

Sample app to test capturing Otel Spans and adding them to New Relic Java agent transactions.

# Requirements

New Relic Java agent must be built off of the branch associated with PR https://github.com/newrelic/newrelic-java-agent/pull/1886

# Configure

When running the app configure it with the following system properties and environment variables:

```shell
-javaagent:/path/to/newrelic/newrelic.jar
-Dnewrelic.config.log_file_name=otel-api-test.log
-Dnewrelic.config.app_name=otel-api-test
-Dnewrelic.config.log_level=info
-Dopentelemetry.sdk.spans.enabled=true
-Dopentelemetry.sdk.autoconfigure.enabled=true
```

```shell
OTEL_LOGS_EXPORTER=logging
OTEL_METRIC_EXPORT_INTERVAL=15000
OTEL_METRICS_EXPORTER=logging
OTEL_SERVICE_NAME=otel-api-test
OTEL_TRACES_EXPORTER=logging
```