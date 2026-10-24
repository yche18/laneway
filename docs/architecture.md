# Architecture

The browser sends requests through Nginx to Spring Boot. Controllers handle HTTP input, services own business rules and transactions, and MyBatis mappers access MySQL. Flyway applies the versioned database migrations.

## Reads and cache consistency

Shop reads traverse request-local storage, Caffeine, Memcached, Redis and MySQL. Empty results are cached and concurrent loads of the same key are coordinated. Version and lock-owner checks prevent an older load from overwriting newer state. Transactional invalidation tasks cover application writes; Canal observes direct database changes through the binary log.

## Orders and flash sales

Regular voucher orders use user-scoped coordination, durable uniqueness and transactional writes. Orders keep snapshots of prices and rules. Flash-sale admission reserves stock through Lua and writes a Redis Stream message. Durable reservations and outbox tasks carry fulfillment forward, with conditional stock deduction and identity checks before compensation. A request being accepted is distinct from an order being fulfilled.

## Durable delivery

Business changes and outbox records share database transactions. Workers claim tasks with leases and update results only while retaining ownership. Retries use backoff; inbox records suppress duplicate consumer effects. Deadline messages and database scans cooperate to close unpaid orders. Components do not share one global atomic transaction.

## Analytics

Kafka transports browsing and order events. Consumers deduplicate event IDs and update minute aggregates transactionally before acknowledging offsets. Browsing collection is bounded and can drop events under pressure; order analytics use durable tasks. Rankings support 5-minute and 60-minute windows.

## Runtime inspection

The inspection page shows actual cache samples, persisted event receipts and the current user's order progress. Controlled concurrency, reconnection and compensation scenarios are covered by the isolated regression suite. The page's saved test report is a separate record of a completed run, not a live execution triggered by visiting the page.
