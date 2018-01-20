package com.meltwater.owl.hello;

import io.prometheus.client.Histogram;

class Metrics {
    static final Histogram httpRequestsLatencySeconds = Histogram
        .build("http_requests_latency_seconds", "The http request latency in seconds.")
        .labelNames("path", "method", "status_code")
        .register();
}
