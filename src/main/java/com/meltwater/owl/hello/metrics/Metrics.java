package com.meltwater.owl.hello.metrics;

import io.prometheus.client.Histogram;
import io.prometheus.client.hotspot.DefaultExports;

public class Metrics {
    // Add JVM metrics by default too
    static {
        DefaultExports.initialize();
    }

    public static final Histogram HTTP_REQUEST_DURATION_SECONDS = Histogram
        .build("http_request_duration_seconds", "The http request latency in seconds.")
        .labelNames("path", "method", "status_code")
        .register();


}
