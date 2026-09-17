package com.alanwilliams.email;

import com.alanwilliams.email.resend.ResendEmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.web.client.RestClient;

class ResendEmailIntegrationTest {

    @Test
    @EnabledIfEnvironmentVariable(
            named = "RESEND_API_KEY",
            matches = ".+"
    )
    @EnabledIfEnvironmentVariable(
            named = "RESEND_TEST_EMAIL",
            matches = ".+"
    )
    void sendsRealEmail() {
        EmailProperties properties = new EmailProperties();
        properties.setApiKey(System.getenv("RESEND_API_KEY"));
        properties.setFromName("AlanWilliams Agenda");

        RestClient restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();

        EmailService emailService =
                new ResendEmailService(restClient, properties);

        emailService.send(
                new EmailMessage(
                        System.getenv("RESEND_TEST_EMAIL"),
                        "AlanWilliams Spring Email Test",
                        """
                        <div style="font-family: Arial, sans-serif;">
                            <h2>AlanWilliams Spring Email</h2>
                            <p>The shared Spring email package is working.</p>
                            <p>This message was delivered through the Resend implementation.</p>
                        </div>
                        """
                )
        );
    }
}