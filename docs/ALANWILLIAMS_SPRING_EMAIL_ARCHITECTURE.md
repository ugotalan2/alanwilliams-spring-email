# AlanWilliams Spring Email - Architecture

## Scope

This document defines the reusable transactional email delivery boundary for AlanWilliams Java applications.

Application-specific notification behavior remains owned by each consuming application.

## Architecture Principle

Delivery is shared; notification behavior is application-specific.

The library answers:

```text
How is this email delivered through the configured provider?
What shared sender identity should be used?
```

It does not answer:

```text
Should an Agenda invitation be sent?
What should an invitation say?
When should Budget send a reminder?
Who should receive a domain notification?
```

## Runtime Model

There is no `alanwilliams-spring-email` container.

It is packaged as a Java dependency consumed by independently deployed Spring Boot applications.

```text
alanwilliams-agenda backend ----\
                                 +--> alanwilliams-spring-email
alanwilliams-budget backend ----/
                                 |
                                 v
                               Resend
```

Future Java applications may consume the same dependency.

## Public Contract

### `EmailMessage`

V1:

```text
to
subject
html
```

The contract intentionally remains small.

The consuming application owns the subject and complete HTML content.

### `EmailService`

Conceptually:

```java
void send(EmailMessage message)
```

Successful submission returns normally.

Provider/HTTP failures propagate as delivery failures to the consuming application.

Provider-specific message IDs are not currently part of the public business contract because no consumer requires them.

## Sender Model

Shared sender address:

```text
notifications@alanwilliams.app
```

The sender address is owned by this package.

Each application configures its display name:

```text
Agenda -> AlanWilliams Agenda
Budget -> AlanWilliams Budget
```

Result:

```text
AlanWilliams Agenda <notifications@alanwilliams.app>
AlanWilliams Budget <notifications@alanwilliams.app>
```

V1 does not expose per-message sender-address overrides.

If multiple sender addresses become a real requirement, the contract may be expanded without requiring current consumers to specify a sender for every message.

## HTML Model

V1 uses HTML email bodies.

```text
EmailMessage.html
```

The shared package does not contain application templates.

Example ownership:

```text
Agenda
-> invitation business event
-> invitation URL
-> invitation subject
-> invitation HTML
-> EmailMessage

alanwilliams-spring-email
-> provider delivery only
```

Plain-text multipart support is deferred until a consumer requirement justifies it.

## Configuration

Current consumer configuration:

```properties
alanwilliams.email.api-key=${RESEND_API_KEY}
alanwilliams.email.from-name=AlanWilliams Agenda
```

`EmailProperties` owns provider-level configuration.

The Resend API base URL defaults to the production Resend endpoint and may be overridden for testing.

Secrets must remain outside Git.

## Spring Boot Integration

The package registers its configuration through Spring Boot auto-configuration.

Conceptually:

```text
consumer starts
-> EmailAutoConfiguration
-> EmailProperties
-> RestClient
-> ResendEmailService
-> EmailService bean
```

Consumers should be able to inject:

```java
private final EmailService emailService;
```

without manually constructing the provider implementation.

## Provider Implementation

Initial provider:

```text
Resend
```

Delivery flow:

```text
EmailMessage
-> ResendEmailService
-> construct From identity
-> construct Resend request
-> Authorization: Bearer <API key>
-> POST email request
-> Resend
```

The implementation uses Spring `RestClient`.

JSON serialization is provided through Jackson.

The library does not require a full Spring MVC application stack solely for provider delivery.

## Domain Authentication

The sending domain is:

```text
alanwilliams.app
```

Resend domain authentication has been configured and verified using the required DNS records.

Current shared sender:

```text
notifications@alanwilliams.app
```

A manual provider-level test and a Java-library integration test have both successfully delivered real email.

## Security Boundaries

* Resend API keys are backend secrets.
* API keys must never be committed to Git.
* API keys must never be exposed to frontend applications.
* The Resend key should use Sending access rather than unnecessary account-wide permissions.
* Sending access should be restricted to `alanwilliams.app`.
* Tests must not hard-code live API keys or recipient addresses.
* Normal builds must not accidentally send live email.

## Testing Strategy

### Auto-Configuration Test

Verifies that configured Spring Boot consumers receive one `EmailService` bean.

### Provider Request Test

A local mock HTTP server verifies:

```text
POST /emails
Authorization header
Content-Type
From display name
notifications@alanwilliams.app sender
recipient
subject
HTML body
```

This test does not require Resend or network access.

### Live Integration Test

The live test requires explicit environment variables:

```text
RESEND_API_KEY
RESEND_TEST_EMAIL
```

If either is absent, the test is skipped.

This prevents normal builds and publishing workflows from sending email.

The integration test has successfully proven:

```text
Java library
-> Resend
-> authenticated AlanWilliams sender
-> real inbox
```

## Application Ownership

### Agenda

Agenda owns:

* invitation lifecycle
* invitation recipient
* invitation URL
* invitation subject
* invitation HTML
* deciding when initial/resend delivery occurs
* handling delivery failure within Agenda's workflow

Agenda delegates only provider delivery.

### Budget

Budget will own:

* snapshot/reminder business rules
* recipients
* scheduling
* subjects
* HTML content
* notification preferences where required

Budget will delegate provider delivery through the same shared package.

## Failure Boundary

Provider delivery failures must not be silently treated as successful delivery.

The shared package reports provider/HTTP failure to the caller.

The consuming application decides what that failure means for its own transaction/workflow.

The shared package does not persist notification state or implement a cross-app retry queue.

## Distribution

Maven coordinates:

```text
com.alanwilliams:alanwilliams-spring-email
```

Current initial version:

```text
0.1.0-SNAPSHOT
```

Distribution:

```text
GitHub Packages
```

Publishing follows the same general model as `alanwilliams-spring-security`:

```text
push main
-> GitHub Actions
-> Java 25
-> Maven deploy
-> GitHub Packages
```

## Current Locked Decisions

* reusable Java library, not a microservice
* Java 25 / Spring Boot 4.1 baseline
* Resend is the initial provider
* `notifications@alanwilliams.app` is the shared sender address
* consuming applications supply their display name
* V1 `EmailMessage` contains `to`, `subject`, and `html`
* HTML is the V1 body format
* app-specific templates/content remain outside this package
* API credentials remain environment secrets
* provider failures propagate to the consumer
* live email tests are opt-in only
* no queue, persistence, scheduler, notification preferences, or template framework

## Near-Term Integration

Next:

```text
publish 0.1.0-SNAPSHOT
-> consume from alanwilliams-agenda
-> Agenda invitation-email service
-> Agenda-owned invitation HTML
-> issue/resend invokes EmailService
-> invitation URL delivered
-> invitation acceptance flow verified end to end
```
