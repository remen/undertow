package com.meltwater.owl.hello.metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.undertow.server.HttpHandler;
import io.undertow.util.Headers;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

public class PrometheusUndertowAdapter {
    public static final HttpHandler METRICS_HANDLER = exchange -> {
        Deque<String> nameParams = exchange.getQueryParameters().get("name[]");
        Set<String> includedNames = (nameParams != null) ? new HashSet<>(nameParams) : Collections.emptySet();
        exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, TextFormat.CONTENT_TYPE_004);
        exchange.getResponseSender().send(getMetrics(includedNames));
    };

    public static HttpHandler instrument(HttpHandler handler) {
        return exchange -> {
            long before = System.currentTimeMillis();
            exchange.addExchangeCompleteListener((finishedExchange, nextListener) -> {
                Double durationSeconds = (double) (System.currentTimeMillis() - before) / 1000;
                String path = finishedExchange.getRelativePath();
                String method = finishedExchange.getRequestMethod().toString();
                String statusCode = Integer.toString(finishedExchange.getStatusCode());
                Metrics.HTTP_REQUEST_DURATION_SECONDS.labels(path, method, statusCode).observe(durationSeconds);
                nextListener.proceed();
            });
            handler.handleRequest(exchange);
        };
    }

    private static String getMetrics(Set<String> includedNames) throws IOException {
        StringWriter stringWriter = new StringWriter();
        TextFormat.write004(stringWriter, CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(includedNames));
        return stringWriter.toString();
    }
}
