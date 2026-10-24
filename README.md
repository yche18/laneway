# Laneway

Laneway is a local discovery and voucher platform built with Java and Spring Boot. It brings together shop search, community posts, follows, daily check-ins, vouchers and asynchronous flash-sale orders, with a desktop web interface.

## Features

- Search shops by category, location, price, rating and voucher availability.
- Publish shop visits, upload images, like posts and follow other members.
- Claim vouchers, track personal orders and inspect flash-sale request status.
- Read through request-local caching, Caffeine, Memcached, Redis and MySQL; synchronize invalidation through transaction outboxes and Canal.
- Deliver durable business tasks with RocketMQ, leases, retries and consumer deduplication.
- Aggregate browsing and order events with Kafka into 5-minute and 60-minute shop rankings.
- Inspect cache paths, event receipts, order task progress and operational metrics.

## Stack

Java 8, Spring Boot 2.3.12, MyBatis-Plus 3.4.3, MySQL 8, Flyway, Redis 7, Redisson, Caffeine, Memcached, Canal, RocketMQ 5.3.2, Kafka 3.9.1, Vue 2, Nginx and Docker Compose.

## Run locally

Start Docker with Compose v2. Run these commands from the repository root:

```sh
docker compose -p laneway -f dev/docker-compose.yml up -d
docker compose -p laneway -f dev/docker-compose.yml ps -a
docker compose -p laneway -f dev/docker-compose.yml logs -f java
```

The first start imports the initial database, creates messaging topics and runs Flyway migrations. Dependency downloads and service initialization can take several minutes. Initialization containers exit successfully after completing their work.

| Endpoint | Address |
| --- | --- |
| Web app | http://127.0.0.1:8080/ |
| Runtime inspection | http://127.0.0.1:8080/experience.html |
| API | http://127.0.0.1:8081 |
| Health | http://127.0.0.1:8081/actuator/health |

```sh
curl http://127.0.0.1:8080/api/shop-type/list
docker compose -p laneway -f dev/docker-compose.yml down
```

Normal shutdown retains database and messaging volumes. The web proxy prefixes API paths with `/api`; direct backend requests do not.

Images and uploads use `data/images/`. Initial shop images are not bundled; populate this directory with the files referenced by the seed records if needed. The web interface has a fallback for unavailable images.

## Login and configuration

Request a phone verification code from the login form and read it from the Java debug log. SMS delivery is not integrated. Authentication uses the raw token in the `authorization` header; the web client stores it in session storage.

Optional settings can be placed in an untracked root `.env` file:

| Variable | Default | Purpose |
| --- | --- | --- |
| `LANEWAY_WEB_PORT` | `8080` | Web port |
| `LANEWAY_API_PORT` | `8081` | Backend port |
| `LANEWAY_EXPERIENCE_ENABLED` | `true` | Runtime inspection endpoints |
| `LANEWAY_ADMIN_IDS` | Empty | Comma-separated administrator user IDs |
| `LANEWAY_PAYMENT_MOCK_ENABLED` | `false` | Local payment simulation |
| `LANEWAY_MONITOR_TOKEN` | Local development value | Metrics access token |

Obtain the current user ID from `GET /user/me`. Apply environment changes with `docker compose ... up -d`; restarting an existing container does not refresh its environment. The Compose configuration enables the optional messaging, analytics and CDC services. Running Java alone requires configuring its database and Redis connections separately.

## Build and test

Building requires Java 8 and Maven. Frontend checks require Node.js 18 or later and no npm dependency installation.

```sh
mvn -DskipTests package
npm --prefix frontend run check
node --test frontend/tests/api.test.js frontend/tests/experience.test.js
```

Maven runs Checkstyle during validation. The existing layout check is available as `sh dev/java-style.sh check`.

Run backend regression tests against the isolated `laneway_test` database:

```sh
docker volume create laneway_maven-cache
docker compose -p laneway-test -f dev/docker-compose.test.yml run --rm test
docker compose -p laneway-test -f dev/docker-compose.test.yml down -v
```

Use the named test environment: these tests reset their database records. Coverage includes cache concurrency, CDC, message delivery, Kafka aggregation, order ownership, flash-sale compensation and runtime inspection endpoints. Validation results are recorded in [docs/verification.json](docs/verification.json).

## Monitoring and release

```sh
docker compose -p laneway -f dev/docker-compose.yml --profile monitoring up -d
```

Prometheus is available on port `9091` and Alertmanager on `9094`. Metrics cover cache reads, CDC delay, pending stream messages, task backlogs and Kafka lag. A health response alone does not confirm that an individual background task has finished; use its persisted receipt or status.

See [deploy/README.md](deploy/README.md) for release packaging and deployment commands, and [docs/architecture.md](docs/architecture.md) for component responsibilities.

## Project layout

```text
src/main/java/com/laneway/   HTTP, business services, persistence and infrastructure
src/main/resources/         Configuration, Mapper XML, SQL migrations and Lua scripts
src/test/                   Backend regression tests
frontend/                   Static web application and frontend tests
dev/                        Local services and monitoring
deploy/                     Release packaging and deployment
config/                     Java style rules
data/images/                Local image files and uploads
```

## Implementation scope

Payment is an optional local simulation; real payment settlement, redemption and refunds are not integrated. Flash-sale acceptance returns an order ID before fulfillment finishes. Redis also stores sessions, verification codes, check-ins and reservation state, so not all Redis data can be rebuilt from MySQL. Kafka visitor counts deduplicate signed-in users; anonymous events contribute to page views. Comment APIs and a multi-node disaster-recovery setup are not complete.
