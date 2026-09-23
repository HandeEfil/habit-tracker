# Habit Tracker

> A backend engineering playground disguised as a habit tracking app.

This isn't just a CRUD app with a habit list bolted on. It's a deliberately
over-engineered (in the good sense) Spring Boot backend, built module by
module to explore real production concerns: security, event-driven
architecture, resilience, and eventually — a full journey from modular
monolith to microservices.

The domain (habits) is almost incidental. The real subject is **how backend
systems are actually built.**

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language / Runtime | Java 21 |
| Framework | Spring Boot, Spring Security |
| Auth | JWT (access + refresh tokens) |
| Database | PostgreSQL, JPA / Hibernate |
| Caching / Rate Limiting | Redis |
| Messaging | Apache Kafka |
| Testing | JUnit, Testcontainers |
| Infra | Docker, Docker Compose |

---

## Architecture

The project is organized as a **modular monolith** — strict module
boundaries today, so that splitting into real microservices later is a
migration, not a rewrite.

```text
habit_tracker
├── app             → application entrypoint, wiring
├── auth            → authentication, JWT, token management, lockout
├── user            → profile, account, password management
├── notification    → channel-agnostic notification engine
├── audit           → security event logging, change tracking
└── common          → shared infrastructure (no business logic)

Design principle: modules communicate through events and DTOs,
rather than direct entity/repository access across boundaries. This is
the same discipline real microservices require — we're just paying that
cost early, on purpose.

Current Features
Authentication & Session Management
JWT-based authentication with access & refresh tokens
Redis-backed failed-login tracking with automatic account lockout
Refresh tokens persisted per device — supports "logout everywhere"
Profile & Account
View / update profile with field-level protection
Change password and forgot/reset password flow
Soft-delete account with recovery grace period
Notifications
Channel-agnostic engine using the NotificationChannel abstraction
Email and in-app notifications
Kafka-driven consumption of domain events
Retry with backoff, Dead Letter Topic handling, and reprocessing
Audit / Security Events
Centralized, event-driven security event logging
Generic event model with metadata or before/after snapshots
Automatic change detection
Decoupled from business logic
Infrastructure
Dockerized local environment (Postgres, Redis, Kafka)
Integration tests using Testcontainers


Roadmap

The project will continue layering real backend problems on top of a
working foundation:

Authentication & Authorization
            ↓
Profile & Account

            ↓
Notification YOU ARE HERE :)
            ↓
Habit Management
            ↓
Habit Sharing / Social
            ↓
Payment
            ↓
Subscription
            ↓
Admin / Backoffice
            ↓
Import / Export
            ↓
Search
            ↓
Production Architecture
            ↓
Modular Monolith → Microservices

Future areas include payment idempotency and reconciliation,
subscription state management, asynchronous imports, event-driven search,
the outbox pattern, distributed locking, observability, resilience
patterns, and eventually service decomposition.

Each stage is meant to introduce a genuinely new class of problem —
not just more endpoints.

Getting Started
docker-compose up -d
./mvnw spring-boot:run

Environment configuration lives in .env and is not committed.

Status

🚧 Work in progress

Currently focusing on completing the notification and audit layers
before moving into the next domain.

Why this project exists

This is a learning project, built openly and iteratively — including the
mistakes, the refactors, and the "wait, why did I do it that way" moments.

If you're browsing the commit history expecting a linear, perfectly
planned build: you won't find one. You'll find something closer to how
backend systems actually get built.