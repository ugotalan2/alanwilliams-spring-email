package com.alanwilliams.email;

public record EmailMessage(
        String to,
        String subject,
        String html
) {
}