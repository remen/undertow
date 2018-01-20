package com.meltwater.owl.hello;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.meltwater.owl.hello.metrics.PrometheusUndertowAdapter.METRICS_HANDLER;
import static com.meltwater.owl.hello.metrics.PrometheusUndertowAdapter.instrument;
import static io.undertow.Handlers.routing;

public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    private final Undertow server = Undertow.builder()
        .addHttpListener(8080, "localhost")
        .setHandler(
            routing()
                .get("/", instrument(ROOT_HANDLER))
                .get("/health", instrument(HEALTH_HANDLER))
                .get("/metrics", instrument(METRICS_HANDLER))
        ).build();

    private static final HttpHandler ROOT_HANDLER = exchange -> {
        exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Hello World!");
    };
    private static final HttpHandler HEALTH_HANDLER = exchange -> {
        exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send("{ \"status\" : \"running\" }");
    };


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
