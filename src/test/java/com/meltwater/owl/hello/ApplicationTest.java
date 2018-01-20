package com.meltwater.owl.hello;

import com.google.common.base.Charsets;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApplicationTest {
    private final Application application = new Application(0); // Randomize port
    private String baseUrl;

    @BeforeAll
    void beforeAll() throws Exception {
        application.start();
        baseUrl = "http://localhost:" + application.getPort();
    }

    @AfterAll
    void afterAll() throws Exception {
        application.stop();
    }

    @Test
    @DisplayName("The /health endpoint is successful")
    void the_health_endpoint_is_successful() throws Exception {
        Response response = get("/health");
        assertThat(response.statusCode).isEqualTo(200);
    }

    @Test
    @DisplayName("The / endpoint is successful")
    void the_root_endpoint_is_successful() throws Exception {
        Response response = get("/");
        assertThat(response.statusCode).isEqualTo(200);
        assertThat(response.body).isEqualTo("Hello World!");
    }

    @Test
    @DisplayName("The /metrics endpoint is successful")
    void the_metrics_endpoint_is_successful() throws Exception {
        Response response = get("/metrics");
        assertThat(response.statusCode).isEqualTo(200);
        assertThat(response.contentType).isEqualTo("text/plain; version=0.0.4; charset=utf-8");
    }

    private Response get(String path) throws IOException {
        HttpResponse httpResponse = Request.Get(baseUrl + path)
            .execute()
            .returnResponse();
        return new Response(
            httpResponse.getStatusLine().getStatusCode(),
            httpResponse.getFirstHeader("Content-Type").getValue(),
            new String(EntityUtils.toByteArray(httpResponse.getEntity()), Charsets.UTF_8)
        );
    }

    private static class Response {
        final int statusCode;
        final String contentType;
        final String body;

        Response(int statusCode, String contentType, String body) {
            this.statusCode = statusCode;
            this.contentType = contentType;
            this.body = body;
        }
    }
}
