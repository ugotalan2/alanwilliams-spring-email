package com.alanwilliams.email;

import com.alanwilliams.email.resend.ResendEmailService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class ResendEmailServiceTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void sendsExpectedResendRequest() throws InterruptedException {
        server.enqueue(
                new MockResponse()
                        .setResponseCode(200)
                        .setBody("{\"id\":\"test-email-id\"}")
                        .addHeader("Content-Type", "application/json")
        );

        EmailProperties properties = new EmailProperties();
        properties.setApiKey("test-api-key");
        properties.setFromName("AlanWilliams Agenda");

        RestClient restClient = RestClient.builder()
                .baseUrl(server.url("/").toString())
                .build();

        ResendEmailService emailService =
                new ResendEmailService(restClient, properties);

        emailService.send(
                new EmailMessage(
                        "recipient@example.com",
                        "Agenda invitation",
                        "<p>You have been invited.</p>"
                )
        );

        RecordedRequest request = server.takeRequest();

        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo("/emails");
        assertThat(request.getHeader("Authorization"))
                .isEqualTo("Bearer test-api-key");
        assertThat(request.getHeader("Content-Type"))
                .startsWith("application/json");

        String body = request.getBody().readUtf8();

        assertThat(body)
                .contains("\"from\":\"AlanWilliams Agenda <notifications@alanwilliams.app>\"")
                .contains("\"to\":[\"recipient@example.com\"]")
                .contains("\"subject\":\"Agenda invitation\"")
                .contains("\"html\":\"<p>You have been invited.</p>\"");
    }
}