package org.example;

import com.newrelic.api.agent.Trace;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

public class Main {
    // Autoconfigure the OTel SDK
    static OpenTelemetrySdk sdk = AutoConfiguredOpenTelemetrySdk.initialize()
            .getOpenTelemetrySdk();
    static Tracer tracer = sdk.getTracer("instrumentation-scope-name", "instrumentation-scope-version");

    public static void main(String[] args) throws InterruptedException {
        for (int i = 1; i <= 500000; i++) {
            createManualOtelSpans();
            createOtelMetrics();
        }
    }

    /**
     * Generates OpenTelemetry dimensional metrics
     */
    public static void createOtelMetrics() {
        LongCounter longCounter = GlobalOpenTelemetry.get().getMeterProvider().get("otel-api-test").counterBuilder("otel.api.test.counter").build();
        longCounter.add(1, Attributes.of(AttributeKey.stringKey("foo"), "bar"));

        DoubleHistogram doubleHistogram = GlobalOpenTelemetry.get().getMeterProvider().get("otel-api-test").histogramBuilder("otel.api.test.histogram").build();
        doubleHistogram.record(1, Attributes.of(AttributeKey.stringKey("bar"), "baz"));
    }

    /**
     * Generates OpenTelemetry spans.
     * <p>
     * Depending on the SpanKind, and whether a New Relic transaction is present, different outcomes
     * can be expected as far as how the New Relic Java agent handles the OpenTelemetry spans.
     */
    public static void createManualOtelSpans() throws InterruptedException {
        // no txn started on its own
        noSpanKind();

        // calls noSpanKind() but wraps it in a NR txn
        nrTraceNoSpanKind();

        // For client spans, we either turn it into a database span or an external based on the span attributes. No txn started on its own
        clientSpanKind();
        dbClientSpanKind();
        externalClientSpanKind();

        // calls clientSpanKind() but wraps it in a NR txn
        nrTraceClientSpanKind();

        // starts an OtherTransaction txn based on consumer span kind
        consumerSpanKind();

        // no txn started on its own
        producerSpanKind();

        // calls producerSpanKind() but wraps it in a NR txn
        nrTraceProducerSpanKind();

        // starts a WebTransaction/Uri txn based on server span kind
        serverSpanKind();
    }

    @Trace(dispatcher = true)
    public static void nrTraceNoSpanKind() {
        System.out.println("called nrTraceNoSpanKind");
        try {
            noSpanKind();
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }

    @Trace(dispatcher = true)
    public static void nrTraceClientSpanKind() {
        System.out.println("called nrTraceClientSpanKind");
        try {
            clientSpanKind();
            dbClientSpanKind();
            externalClientSpanKind();
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }

    @Trace(dispatcher = true)
    public static void nrTraceProducerSpanKind() {
        System.out.println("called nrTraceProducerSpanKind");
        try {
            producerSpanKind();
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
    }

    // create span with no span kind
    public static void noSpanKind() throws InterruptedException {
        Span span = tracer.spanBuilder("noSpanKind").startSpan();
        try (Scope scope = span.makeCurrent()) {
            System.out.println("called noSpanKind");
            Thread.sleep(1000);
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    // create generic consumer span
    public static void consumerSpanKind() throws InterruptedException {
        Span span = tracer.spanBuilder("consumerSpanKind").setSpanKind(SpanKind.CONSUMER).startSpan();
        try (Scope scope = span.makeCurrent()) {
            System.out.println("called consumerSpanKind");
            Thread.sleep(1000);
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    // create generic producer span
    public static void producerSpanKind() throws InterruptedException {
        Span span = tracer.spanBuilder("producerSpanKind").setSpanKind(SpanKind.PRODUCER).startSpan();
        try (Scope scope = span.makeCurrent()) {
            System.out.println("called producerSpanKind");
            Thread.sleep(1000);
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    // create generic server span
    public static void serverSpanKind() throws InterruptedException {
        Span span = tracer.spanBuilder("serverSpanKind").setSpanKind(SpanKind.SERVER)
                .setAttribute("url.path", "/whatever")
                .startSpan();
        try (Scope scope = span.makeCurrent()) {
            System.out.println("called serverSpanKind");
            Thread.sleep(1500);
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    // create generic client span
    public static void clientSpanKind() throws InterruptedException {
        Span span = tracer.spanBuilder("clientSpanKind").setSpanKind(SpanKind.CLIENT).startSpan();
        try (Scope scope = span.makeCurrent()) {
            System.out.println("called clientSpanKind");
            Thread.sleep(2000);
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    // create DB client span
    public static void dbClientSpanKind() throws InterruptedException {
        Span span = tracer.spanBuilder("owners select").setSpanKind(SpanKind.CLIENT)
                .setAttribute("db.system", "mysql")
                .setAttribute("db.operation", "select")
                .setAttribute("db.sql.table", "owners")
                .setAttribute("db.statement", "SELECT * FROM owners WHERE ssn = 4566661792").startSpan();
        try (Scope scope = span.makeCurrent()) {
            System.out.println("called dbClientSpanKind");
            Thread.sleep(2000);
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }

    // create external client span
    public static void externalClientSpanKind() throws InterruptedException {
        Span span = tracer.spanBuilder("example.com").setSpanKind(SpanKind.CLIENT)
                .setAttribute("server.address", "www.foo.bar")
                .setAttribute("url.full", "https://www.foo.bar:8080/search?q=OpenTelemetry#SemConv")
                .setAttribute("server.port", 8080)
                .setAttribute("http.request.method", "GET")
                .startSpan();
        try (Scope scope = span.makeCurrent()) {
            System.out.println("called externalClientSpanKind");
            Thread.sleep(2000);
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
    }
}