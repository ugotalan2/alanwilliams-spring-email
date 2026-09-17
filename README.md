# AlanWilliams Spring Email

Shared transactional email delivery library for AlanWilliams Spring Boot applications.

`alanwilliams-spring-email` provides a small reusable email-delivery boundary so AlanWilliams applications can send transactional email without duplicating provider integration.

It is a Java library consumed by applications, not a deployed email or notification service.

## Current Provider

Resend

## Current Sender

```text
notifications@alanwilliams.app
```

Each consuming application supplies its own display name.

Examples:

```text
AlanWilliams Agenda <notifications@alanwilliams.app>
AlanWilliams Budget <notifications@alanwilliams.app>
```

## Public Contract

```java
EmailMessage(
    String to,
    String subject,
    String html
)
```

```java
EmailService.send(EmailMessage message)
```

Email content is HTML. Business-specific content and templates remain owned by the consuming application.

## Configuration

```properties
alanwilliams.email.api-key=${RESEND_API_KEY}
alanwilliams.email.from-name=AlanWilliams Agenda
```

The Resend API key must be supplied as a backend environment secret and must never be committed to Git or exposed to frontend code.

The Resend API base URL has a production default and may be overridden for testing.

## Ownership

The shared library owns:

* generic email message/service contract
* Spring Boot auto-configuration
* sender-address convention
* provider configuration
* Resend HTTP delivery

Consuming applications own:

* business rules determining when email is sent
* recipients
* subjects
* HTML content/templates
* invitation/reminder lifecycle
* scheduling
* notification preferences

The library does not provide a notification database, queue, scheduling system, template framework, or standalone notification service.

## Build

```bash
./mvnw clean verify
```

Normal builds run unit tests and skip the live Resend integration test unless the required environment variables are explicitly present.

## Live Integration Test

Set:

```bash
export RESEND_API_KEY='...'
export RESEND_TEST_EMAIL='...'
```

Then run:

```bash
./mvnw -Dtest=ResendEmailIntegrationTest test
```

The live integration test verifies:

```text
alanwilliams-spring-email
-> Resend
-> authenticated alanwilliams.app sender
-> real recipient inbox
```

Do not commit either test value.

## Distribution

Maven coordinates:

```text
com.alanwilliams:alanwilliams-spring-email
```

The package is published through GitHub Maven Packages.

Current initial version:

```text
0.1.0-SNAPSHOT
```

## Repository

```text
ugotalan2/alanwilliams-spring-email
```
