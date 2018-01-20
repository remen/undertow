package com.meltwater.owl.hello;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.prometheus.client.hotspot.DefaultExports;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import static io.undertow.Handlers.routing;

public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    static {
        DefaultExports.initialize();
    }

    private static final HttpHandler ROOT_HANDLER = exchange -> {
        exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Hello World!");
    };
    private static final HttpHandler HEALTH_HANDLER = exchange -> {
        exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send("{ \"status\" : \"running\" }");
    };

    private static final HttpHandler METRICS_HANDLER = exchange -> {
        Deque<String> nameParams = exchange.getQueryParameters().get("name[]");
        Set<String> includedNames = (nameParams != null) ? new HashSet<>(nameParams) : Collections.emptySet();
        exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, TextFormat.CONTENT_TYPE_004);
        exchange.getResponseSender().send(getMetrics(includedNames));
    };

    private static String getMetrics(Set<String> includedNames) throws IOException {
        StringWriter stringWriter = new StringWriter();
        TextFormat.write004(stringWriter, CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(includedNames));
        return stringWriter.toString();
    }

    private static HttpHandler instrument(HttpHandler handler) {
        return exchange -> {
            long before = System.currentTimeMillis();
            exchange.addExchangeCompleteListener((finishedExchange, nextListener) -> {
                long duration = System.currentTimeMillis() - before;
                String path = finishedExchange.getRelativePath();
                String method = finishedExchange.getRequestMethod().toString();
                String statusCode = Integer.toString(finishedExchange.getStatusCode());
                Metrics.httpRequestsLatencySeconds.labels(path, method, statusCode).observe((double)duration / 1000);
                nextListener.proceed();
            });
            handler.handleRequest(exchange);
        };
    }

    private final Undertow server = Undertow.builder()
        .addHttpListener(8080, "localhost")
        .setHandler(
            routing()
                .get("/", instrument(ROOT_HANDLER))
                .get("/health", instrument(HEALTH_HANDLER))
                .get("/metrics", instrument(METRICS_HANDLER))
        ).build();

    public void start() {
        server.start();
        LOGGER.info("Application started");
    }

    public void stop() {
        server.stop();
        LOGGER.info("Application stopped");
    }

    public static void main(final String[] args) {

        new Application().start();
    }
}
