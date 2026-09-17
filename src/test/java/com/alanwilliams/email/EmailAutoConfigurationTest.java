package com.alanwilliams.email;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class EmailAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(
                                    EmailAutoConfiguration.class
                            )
                    )
                    .withPropertyValues(
                            "alanwilliams.email.api-key=test-key",
                            "alanwilliams.email.from-name=AlanWilliams Agenda"
                    );

    @Test
    void createsEmailService() {
        contextRunner.run(context ->
                assertThat(context)
                        .hasSingleBean(EmailService.class)
        );
    }
}