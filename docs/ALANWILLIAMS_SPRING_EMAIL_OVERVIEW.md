# AlanWilliams Spring Email - Project Overview

## Purpose

`alanwilliams-spring-email` is the reusable transactional email delivery library for AlanWilliams Spring Boot applications.

It centralizes provider-level email delivery while keeping notification business rules and message content inside each application.

It is intentionally a library, not a deployed notification microservice.

## Repository

```text
ugotalan2/alanwilliams-spring-email
```

Maven artifact:

```text
com.alanwilliams:alanwilliams-spring-email
```

## Role in AlanWilliams Apps

The shared boundary is:

```text
application business event
-> application-owned email composition
-> EmailMessage
-> EmailService
-> Resend
-> recipient
```

Examples:

```text
Agenda invitation
-> Agenda invitation-email service
-> alanwilliams-spring-email
-> Resend

Budget reminder
-> Budget reminder service
-> alanwilliams-spring-email
-> Resend
```

## What This Library Owns

* generic `EmailMessage` contract
* generic `EmailService` contract
* Spring Boot auto-configuration
* provider configuration
* Resend-backed delivery
* shared sender-address convention
* provider-level delivery tests

## What This Library Does Not Own

* Agenda invitations
* Budget reminders or snapshots
* application notification rules
* application email templates/content
* scheduling
* notification preferences
* notification persistence
* delivery queues
* retries/workflow orchestration
* Platform Person or membership data
* a standalone notification API/service

## Email Contract

V1 messages contain:

```text
to
subject
html
```

HTML is the standard body format.

Plain-text bodies are not part of the V1 contract. They may be added later if a real consumer requirement justifies them.

## Sender Identity

The shared sender address is:

```text
notifications@alanwilliams.app
```

The consuming application supplies the display name.

Examples:

```text
AlanWilliams Agenda <notifications@alanwilliams.app>
AlanWilliams Budget <notifications@alanwilliams.app>
```

The sender address is intentionally package-owned because it is shared AlanWilliams email infrastructure rather than application-domain configuration.

## Provider

Initial provider:

```text
Resend
```

`alanwilliams.app` is authenticated with Resend for transactional sending.

The shared package communicates with Resend through Spring `RestClient`.

Provider credentials remain backend environment secrets.

## Configuration

Consumer applications configure:

```properties
alanwilliams.email.api-key=${RESEND_API_KEY}
alanwilliams.email.from-name=AlanWilliams Agenda
```

The provider API base URL has a production default and can be overridden for deterministic testing.

## Testing

The package currently verifies:

```text
Spring Boot auto-configuration
-> EmailService bean created

EmailService
-> expected authenticated Resend HTTP request
-> fixed sender address
-> application display name
-> recipient
-> subject
-> HTML body
```

A live integration test is opt-in through environment variables and has successfully delivered an email through Resend to a real inbox.

Normal builds do not send live email.

## Distribution

The package follows the same general shared-Java-library distribution model as `alanwilliams-spring-security`.

```text
GitHub repository
-> Maven build
-> GitHub Packages
-> consumer application build
```

Initial version:

```text
0.1.0-SNAPSHOT
```

## Current Status

Implemented:

* repository/build skeleton
* Java 25 baseline
* Spring Boot 4.1 baseline
* `EmailMessage`
* `EmailService`
* configuration properties
* Spring Boot auto-configuration
* Resend delivery adapter
* deterministic HTTP request test
* opt-in live Resend integration test
* authenticated `alanwilliams.app` sending domain
* successful real email delivery

Next consumer:

```text
alanwilliams-agenda
```

Agenda will own invitation HTML and invitation business behavior while delegating provider delivery to this package.

## Explicitly Deferred

* notification microservice
* shared notification database
* queue infrastructure
* shared template database/framework
* scheduling framework
* cross-app notification preferences
* provider abstractions beyond what real consumers require
