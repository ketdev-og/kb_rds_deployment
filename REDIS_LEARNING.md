# Redis with Spring Boot — Learning Roadmap

A progressive, hands-on curriculum for learning everything Redis can do, built on this
project's stack: **Spring Boot 4.1.0**, **Java 21**, **spring-data-redis**, **Dragonfly**
(Redis-compatible), **Postgres** (JPA), Redis Commander UI.

Work through phases **in order** — each builds on the previous. Phases 0→1→2 are the backbone.

---

## Setup notes (read first)

- **Postgres**: You have `spring-data-jpa` + `postgresql` deps but Postgres is not yet in
  `docker-compose.yaml`. You need it from **Phase 2** onward (cache-aside needs a "slow"
  backing store to cache in front of). Add a `postgres` service whose env matches your
  `application.yaml` datasource defaults.
- **Search caveat (Phase 8)**: Full RediSearch / RedisJSON are **Redis Stack** modules.
  **Dragonfly** has its own built-in `FT.*` search, but only a *subset*. When you reach
  Phase 8 you'll either accept Dragonfly's limits or swap to `redis/redis-stack` for that phase.
- **Config hygiene** (currently in `application.yaml`):
  - `spring.profiles.active` must be nested under `spring:` — at the root it's silently ignored.
  - `spring.datasource.connection-fetch` is not a real property (ignored). HikariCP is lazy by
    default; the real knob is `spring.datasource.hikari.initialization-fail-timeout`.

---

## Phase 0 — Foundations: talk to Redis directly
- **0.1** Verify connectivity: `docker compose up -d`, app connects, see keys in Redis Commander (http://localhost:8081).
- **0.2** Choose client: Lettuce (default, async/Netty) vs Jedis. Understand why your `jedis.pool` block is inert without the Jedis dependency.
- **0.3** `RedisTemplate` vs `StringRedisTemplate`; **serializers** (JDK vs `GenericJackson2JsonRedisSerializer`) — the #1 cause of "unreadable bytes in Redis Commander".
- **0.4** Manually read/write each core type via `RedisTemplate`: String, Hash, List, Set, ZSet.

## Phase 1 — Spring's caching abstraction
- **1.1** `@EnableCaching`; `@Cacheable`, `@CacheEvict`, `@CachePut` on a service.
- **1.2** Configure `RedisCacheManager` with per-cache **TTLs** and JSON serialization.
- **1.3** Custom keys (`key = "#id"`, `KeyGenerator`); `condition` / `unless`.
- **1.4** Observe hits/misses (logging) and verify keys + TTL in Redis Commander.

## Phase 2 — Caching patterns & pitfalls  *(needs Postgres)*
- **2.1** Cache-aside (lazy loading) end-to-end over a JPA entity.
- **2.2** TTL strategies; cache `null`/empty results to stop **cache penetration**.
- **2.3** **Cache stampede / thundering herd** — `sync = true`, jittered TTLs.
- **2.4** Write-through vs write-behind; consistency on update/delete (`@CacheEvict`).
- **2.5** Eviction policies (`maxmemory-policy`, LRU/LFU) — behavior when memory fills.

## Phase 3 — Redis as a primary datastore
- **3.1** Spring Data Redis repositories with `@RedisHash` (Redis as system of record).
- **3.2** Secondary indexes (`@Indexed`) and per-entity TTL.
- **3.3** Trade-offs vs JPA: when Redis-as-DB fits, when it bites.

## Phase 4 — Data structures as features (one real use case each)
- **4.1** Strings/counters → page-view counter, atomic `INCR`.
- **4.2** Hashes → store an object as fields, partial updates.
- **4.3** Sets → tags, unique visitors, intersections ("follows both X and Y").
- **4.4** Sorted Sets → **leaderboard / ranking**, time-window data.
- **4.5** Bitmaps & HyperLogLog → daily-active-users, approximate unique counts.
- **4.6** Geo → "stores near me" radius queries.

## Phase 5 — Distributed system patterns
- **5.1** Distributed **rate limiter** (fixed/sliding window via `INCR`+TTL or Lua).
- **5.2** Distributed **lock** (`SET NX PX`); why naive locks are unsafe (Redlock discussion).
- **5.3** Atomic multi-step ops with **Lua scripting** (`DefaultRedisScript`).

## Phase 6 — Messaging
- **6.1** Pub/Sub with `RedisMessageListenerContainer` (fire-and-forget events).
- **6.2** **Redis Streams** — durable log, consumer groups, ack/replay.

## Phase 7 — Sessions
- **7.1** Spring Session backed by Redis (`session-data-redis` already present) — externalize HTTP session.
- **7.2** Inspect session keys, TTL/expiry, and what serialization stores.

## Phase 8 — Search  ⚠️ *(engine decision here)*
- **8.1** Decide engine: Dragonfly built-in search vs swap to Redis Stack for full RediSearch.
- **8.2** Secondary-index search: `FT.CREATE` / `FT.SEARCH` over hashes.
- **8.3** Full-text, filters, sorting, pagination; (RedisJSON + JSON indexes on Redis Stack).
- **8.4** Autocomplete / suggestions.

## Phase 9 — Operations & correctness
- **9.1** Persistence (RDB/AOF) — is your cache data durable, and should it be?
- **9.2** Keyspace notifications (react to key-expiry events).
- **9.3** Pipelining & transactions (`MULTI`/`EXEC`) for throughput.
- **9.4** **Testing with Testcontainers** — spin Dragonfly/Redis per test; make it all repeatable.
- **9.5** Metrics/observability — cache hit ratio via Micrometer/Actuator.