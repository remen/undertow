package com.meltwater.owl.hello;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.InputStream;
import java.io.InputStreamReader;

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
        HttpResponse httpResponse = Request.Get(baseUrl + "/health")
            .execute()
            .returnResponse();
        assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("The / endpoint is successful")
    void the_root_endpoint_is_successful() throws Exception {
        HttpResponse httpResponse = Request.Get(baseUrl + "/")
            .execute()
            .returnResponse();
        assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(200);
        InputStream inputStream = httpResponse.getEntity().getContent();
        String content = CharStreams.toString(new InputStreamReader(inputStream, Charsets.UTF_8));
        assertThat(content).isEqualTo("Hello World!");
    }
}
