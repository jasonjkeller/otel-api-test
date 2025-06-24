# New Relic OpenTelemetry Span Support (APM Java)

Example application illustrating how the OpenTelemetry Span APIs can be integrated with the New Relic Java agent.

## Requirements

### Java

This project requires Java 11 or higher

### Java Agent

New Relic Java agent must be built off of the branch associated with PR https://github.com/newrelic/newrelic-java-agent/pull/1886

A custom Java agent jar built with this functionality can be located in the `newrelic` directory in this project or downloaded from here: https://github.com/newrelic/newrelic-java-agent/actions/runs/15835856990

### OpenTelemetry

This functionality will only work with `io.opentelemetry:opentelemetry-sdk-extension-autoconfigure` version `1.17.0-alpha` or higher.

To use this functionality, the following OpenTelemetry dependencies are required:

```groovy
    implementation(platform("io.opentelemetry:opentelemetry-bom:1.51.0"))
    implementation("io.opentelemetry:opentelemetry-sdk-extension-autoconfigure")
    implementation("io.opentelemetry:opentelemetry-api")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
```

## Configuration

The following [configuration](https://docs.newrelic.com/docs/apm/agents/java-agent/configuration/java-agent-configuration-config-file/#otel-sdk-autoconfiguration) options are required for sending OpenTelemetry dimensional metrics to New Relic using the APM Java agent.

```
-javaagent:/full/path/to/otel-api-test/newrelic/newrelic.jar
-Dopentelemetry.sdk.autoconfigure.enabled=true
-Dotel.java.global-autoconfigure.enabled=true
-Dopentelemetry.sdk.spans.enabled=true
```

Note, if you are following along and wish to run the demo yourself, you can add your own ingest [license_key](https://docs.newrelic.com/docs/apis/intro-apis/new-relic-api-keys/#license-key) to the `newrelic.yml` file found in the `otel-api-test/newrelic/` directory of this project. You will also find the Java agent `newrelic.jar` file located there. By default, the Java agent will send data to a US Production data center, but you can change that by configuring `-Dnewrelic.environment=<environment>` to use one of the other environments defined in the `newrelic.yml` file.

## Build and Run The Demo

From the project root, create an executable jar:

```commandline
./gradlew shadowJar
```

## Run

From the project root, run the executable jar (entering the correct path to the `newrelic.jar`):

```commandline
java -javaagent:./newrelic/newrelic.jar \
-Dnewrelic.environment=us-production-data-center \
-Dotel.java.global-autoconfigure.enabled=true \
-Dopentelemetry.sdk.autoconfigure.enabled=true \
-Dopentelemetry.sdk.spans.enabled=true \
-jar build/libs/otel-api-test-1.0-SNAPSHOT-all.jar
```

# Visualizing Data

Span data can be directly queried or viewed in the context of the "Transactions" and "Distributed Tracing" UIs.

You can view dimensional metrics in the New Relic Data Explorer by selecting "Metrics" as the data type and specifying the correct account and entity name.