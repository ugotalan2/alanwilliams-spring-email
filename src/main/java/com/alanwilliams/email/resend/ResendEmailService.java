package com.alanwilliams.email.resend;

import com.alanwilliams.email.EmailMessage;
import com.alanwilliams.email.EmailProperties;
import com.alanwilliams.email.EmailService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.List;

public class ResendEmailService implements EmailService {

    private static final String FROM_ADDRESS = "notifications@alanwilliams.app";

    private final RestClient restClient;
    private final EmailProperties properties;

    public ResendEmailService(
            RestClient restClient,
            EmailProperties properties
    ) {
        this.restClient = restClient;
        this.properties = properties;
    }

    @Override
    public void send(EmailMessage message) {
        ResendEmailRequest request = new ResendEmailRequest(
                properties.getFromName() + " <" + FROM_ADDRESS + ">",
                List.of(message.to()),
                message.subject(),
                message.html()
        );

        restClient.post()
                .uri("/emails")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + properties.getApiKey()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    private record ResendEmailRequest(
            String from,
            List<String> to,
            String subject,
            String html
    ) {
    }
}