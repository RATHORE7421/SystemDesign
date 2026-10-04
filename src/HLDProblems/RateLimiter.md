# 🚦 Rate Limiter — System Design (Interview Revision Guide)

> **Target Level**: SDE2 / SDE3 | **Time**: ~35–45 minutes  
> **Type**: HLD | **Key Topics**: Algorithms, Distributed Systems, Redis, API Gateway

---

## 📋 Phase 1: Requirements & Clarifications (3–5 min)

### What is a Rate Limiter?

A mechanism that **controls the rate of requests** a client can send to a server in a given time window. If a client exceeds the limit, excess requests are **throttled** (rejected with HTTP 429).

### Why do we need it?

| Reason | Example |
|--------|---------|
| **Prevent abuse / DDoS** | Bot sending 10K req/sec to crash the server |
| **Fair resource sharing** | One heavy user shouldn't starve others |
| **Cost control** | Limit calls to expensive third-party APIs |
| **Compliance** | APIs like Twitter enforce 300 tweets/3hrs |
| **Stability** | Protect downstream services from overload |

### Functional Requirements

| # | Requirement | Notes |
|---|-------------|-------|
| 1 | Limit requests per client (user/IP) | Configurable per-client rules |
| 2 | Support different rules per API endpoint | `/api/login` → 5/min, `/api/search` → 100/min |
| 3 | Return HTTP 429 when limit exceeded | Include `Retry-After` header |
| 4 | Support both hard and soft limits | Hard = reject, Soft = log/alert |
| 5 | Rules should be configurable without redeployment | Dynamic config |

### Non-Functional Requirements

| # | Requirement | Notes |
|---|-------------|-------|
| 1 | **Low latency** — must NOT add significant overhead | < 1ms per check |
| 2 | **Highly available** — if rate limiter dies, traffic should still flow | Fail-open vs fail-closed trade-off |
| 3 | **Distributed** — work across multiple servers | Shared state needed |
| 4 | **Accurate** — no significant over/under counting | Especially at window boundaries |
| 5 | **Memory efficient** — millions of users, can't store too much per user | Algorithm choice matters |

### 🎤 Clarifying Questions to Ask

1. **"Where does the rate limiter sit — client-side, middleware, or server-side?"**  
   → **Middleware / API Gateway**. Client-side is bypassable. Server-side couples concerns.

2. **"What's the throttle key — user ID, IP, API key, or combination?"**  
   → Configurable. Common: `userId + endpoint`, `IP`, or `API key`.

3. **"What happens on throttle — hard reject, queue, or degrade?"**  
   → **HTTP 429 + Retry-After header**. Don't queue (backpressure risk).

4. **"Single server or distributed across multiple servers?"**  
   → Start single, then extend to distributed.

5. **"Do we need to rate limit at multiple granularities (per-second + per-day)?"**  
   → Yes, layered rules (e.g., 10/sec AND 1000/day).

---

## 🧠 Phase 2: Rate Limiting Algorithms (Core of the Interview)

### Overview: All 5 Algorithms at a Glance

| Algorithm | Memory | Accuracy | Burst Handling | Complexity | Interview Verdict |
|-----------|--------|----------|----------------|------------|-------------------|
| **Token Bucket** ⭐ | O(1) | Good | ✅ Allows controlled bursts | Simple | **#1 — Most asked** |
| **Leaky Bucket** | O(N) queue | Good | ❌ Strict smoothing | Moderate | Niche — know the concept |
| **Fixed Window Counter** | O(1) | ⚠️ Boundary spike | ❌ Spike at edges | Simplest | Know the flaw |
| **Sliding Window Log** | O(N) | ✅ Perfect | ✅ Exact | Expensive | Know why it's impractical |
| **Sliding Window Counter** ⭐ | O(1) | ✅ Near-perfect | ✅ Smooth | Moderate | **#2 — Best practical** |

---

### 🏆 Algorithm 1: Token Bucket (⭐ PRIMARY — Know Cold)

#### The Mental Model

Imagine a bucket that holds tokens:
- Tokens are **added at a fixed rate** (e.g., 10 tokens/second)
- Each request **consumes 1 token**
- If bucket is **empty** → request is **rejected**
- Bucket has a **max capacity** → tokens don't overflow

```
    ┌─────────────┐
    │  Token Drop  │  ← Refill rate: 10 tokens/sec
    │     ↓ ↓ ↓    │
    │ ┌─────────┐  │
    │ │ ● ● ● ● │  │  ← Bucket (max capacity: 20)
    │ │ ● ● ●   │  │
    │ └────┬────┘  │
    │      ↓       │
    │  Request → Takes 1 token → ✅ Allowed
    │  Request → Bucket empty  → ❌ HTTP 429
    └─────────────┘
```

#### Two Key Parameters

| Parameter | Meaning | Example |
|-----------|---------|---------|
| **Bucket Size (capacity)** | Max tokens the bucket can hold = max burst size | 20 |
| **Refill Rate** | Tokens added per second | 10/sec |

> 🎤 **Cross-question**: *"If bucket size is 20 and refill rate is 10/sec, what's the max burst?"*  
> **Answer**: A client can send **20 requests instantly** (drain the bucket), then must wait. After 1 second, 10 tokens are refilled. So: burst of 20, then sustained 10/sec.

#### Java Implementation

```java
class TokenBucket {
    private final int maxTokens;          // Bucket capacity
    private final int refillRate;         // Tokens per second
    private double currentTokens;         // Current token count
    private long lastRefillTimestamp;      // Last refill time (nanos)

    public TokenBucket(int maxTokens, int refillRate) {
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.currentTokens = maxTokens;   // Start full
        this.lastRefillTimestamp = System.nanoTime();
    }

    public synchronized boolean allowRequest() {
        refill();
        if (currentTokens >= 1) {
            currentTokens--;
            return true;    // ✅ Allowed
        }
        return false;       // ❌ Rate limited
    }

    private void refill() {
        long now = System.nanoTime();
        double elapsed = (now - lastRefillTimestamp) / 1_000_000_000.0;  // seconds
        
        // Add tokens based on elapsed time
        double tokensToAdd = elapsed * refillRate;
        currentTokens = Math.min(maxTokens, currentTokens + tokensToAdd);
        lastRefillTimestamp = now;
    }
}
```

#### Why Token Bucket is #1 for Interviews

| Advantage | Why It Matters |
|-----------|---------------|
| **O(1) memory** per user | Just 2 numbers: `currentTokens` + `lastRefillTimestamp` |
| **Allows bursts** | Real traffic IS bursty — users click fast, then idle |
| **Simple to implement** | Easy to code in 5 min on a whiteboard |
| **Industry standard** | Used by AWS API Gateway, Stripe, Google Cloud |
| **Easy to distribute** | Store `(tokens, timestamp)` in Redis |

#### Token Bucket in Redis (Distributed)

```python
-- Lua script for atomic token bucket in Redis
-- KEYS[1] = rate_limit:{user_id}
-- ARGV[1] = max_tokens, ARGV[2] = refill_rate, ARGV[3] = now (epoch seconds)

local key = KEYS[1]
local max_tokens = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

-- Get current state
local data = redis.call('HMGET', key, 'tokens', 'last_refill')
local tokens = tonumber(data[1]) or max_tokens
local last_refill = tonumber(data[2]) or now

-- Refill
local elapsed = now - last_refill
local new_tokens = math.min(max_tokens, tokens + elapsed * refill_rate)

-- Try to consume
if new_tokens >= 1 then
    new_tokens = new_tokens - 1
    redis.call('HMSET', key, 'tokens', new_tokens, 'last_refill', now)
    redis.call('EXPIRE', key, max_tokens / refill_rate * 2)  -- Auto-cleanup
    return 1  -- Allowed
else
    redis.call('HMSET', key, 'tokens', new_tokens, 'last_refill', now)
    return 0  -- Rejected
end
```

> 🎤 **Cross-question**: *"Why a Lua script in Redis? Why not two separate GET + SET commands?"*  
> **Answer**: **Atomicity**. Without Lua, two requests could read the same token count simultaneously, both decrement it, and we'd allow double the limit. Lua scripts execute **atomically** in Redis — no interleaving.

---

### 🏆 Algorithm 2: Sliding Window Counter (⭐ SECONDARY — Strong Practical Choice)

#### The Problem with Fixed Window Counter

First, understand the flaw that Sliding Window fixes:

```
Fixed Window: 100 requests/minute

    Window 1 (0:00–1:00)         Window 2 (1:00–2:00)
    ├───────────────┤            ├───────────────┤
    │           ██ 100 req       │██ 100 req      │
    │         at 0:59            │at 1:00          │
                     ↑            ↑
                200 requests in 2 seconds!  💥
                But each window says "within limit"
```

A user sends 100 requests at 0:59 and 100 at 1:00. Each window sees only 100 → under limit. But in a **1-minute sliding window around 0:59–1:00**, they sent **200 requests** — 2x the limit!

#### How Sliding Window Counter Fixes This

**Key Idea**: Weight the previous window's count based on overlap.

```
Current time: 1:15 (15 seconds into window 2)

Previous window (0:00–1:00): 84 requests
Current window  (1:00–2:00): 36 requests so far

Overlap with previous window = 1 - (15/60) = 75%

Weighted count = 84 × 0.75 + 36 = 63 + 36 = 99

Limit is 100 → 99 < 100 → ✅ ALLOW (just barely!)
```

#### Formula

```
weighted_count = (prev_window_count × overlap%) + current_window_count

where overlap% = 1 - (current_time_in_window / window_size)
```

#### Java Implementation

```java
class SlidingWindowCounter {
    private final int maxRequests;       // e.g., 100
    private final long windowSizeMs;     // e.g., 60_000 (1 minute)
    
    // Store counts per window start time
    private int previousWindowCount;
    private int currentWindowCount;
    private long currentWindowStart;

    public SlidingWindowCounter(int maxRequests, long windowSizeMs) {
        this.maxRequests = maxRequests;
        this.windowSizeMs = windowSizeMs;
        this.currentWindowStart = System.currentTimeMillis();
        this.previousWindowCount = 0;
        this.currentWindowCount = 0;
    }

    public synchronized boolean allowRequest() {
        long now = System.currentTimeMillis();
        long currentWindowKey = now / windowSizeMs * windowSizeMs;

        // Check if we've moved to a new window
        if (currentWindowKey != currentWindowStart) {
            // Did we skip an entire window?
            if (currentWindowKey - currentWindowStart >= windowSizeMs * 2) {
                previousWindowCount = 0;  // Skipped entire prev window
            } else {
                previousWindowCount = currentWindowCount;
            }
            currentWindowCount = 0;
            currentWindowStart = currentWindowKey;
        }

        // Calculate weighted count
        long elapsedInWindow = now - currentWindowStart;
        double overlapPercent = 1.0 - ((double) elapsedInWindow / windowSizeMs);
        double weightedCount = previousWindowCount * overlapPercent + currentWindowCount;

        if (weightedCount < maxRequests) {
            currentWindowCount++;
            return true;   // ✅ Allowed
        }
        return false;      // ❌ Rate limited
    }
}
```

#### Why Sliding Window Counter is #2

| Advantage | Detail |
|-----------|--------|
| **O(1) memory** | Just 3 numbers: `prevCount`, `currCount`, `windowStart` |
| **Fixes boundary spike** | Weighted overlap eliminates the Fixed Window flaw |
| **Near-perfect accuracy** | ~99.7% accuracy in practice (Cloudflare's study) |
| **Simple in Redis** | 2 counters + EXPIRY |

> 🎤 **Cross-question**: *"Is it perfectly accurate?"*  
> **Answer**: No — it **assumes uniform distribution** within each window. If all 84 requests in the previous window came in the first 10 seconds, the 75% weight overestimates. But in practice, traffic IS roughly uniform, so the error is negligible (~0.003% per Cloudflare).

---

### Algorithm 3: Leaky Bucket (Know the Concept)

#### Mental Model

A bucket with a **small hole at the bottom**. Requests pour in from the top. They **leak out at a fixed rate**.

```
    Requests pour in (variable rate)
           ↓ ↓ ↓ ↓ ↓
        ┌───────────┐
        │ ● ● ● ● ● │  ← Queue (FIFO, fixed size)
        │ ● ● ●     │
        └─────┬─────┘
              │ drip drip  ← Leaks out at FIXED rate
              ↓
         Processing
```

- If bucket (queue) is **full** → new requests are **dropped**
- Requests are processed at a **constant rate** regardless of input

#### Key Difference from Token Bucket

| | Token Bucket | Leaky Bucket |
|---|---|---|
| **Output rate** | Bursty (can drain all tokens at once) | **Constant** (fixed drip rate) |
| **Burst handling** | ✅ Allows bursts up to bucket capacity | ❌ Smooths bursts — strict FIFO |
| **Data structure** | Counter | **Queue** |
| **Memory** | O(1) | **O(N)** — queue of pending requests |
| **Use case** | API rate limiting | **Traffic shaping** (network routers) |

#### When to use Leaky Bucket

- When you need a **constant processing rate** (e.g., sending emails — don't want to burst 1000 emails at once)
- When **downstream systems can't handle bursts** — Leaky Bucket acts as a smoother

> 🎤 **Cross-question**: *"Doesn't queuing add latency?"*  
> **Answer**: Yes! That's the trade-off. Token Bucket gives a fast yes/no. Leaky Bucket says "wait in line." For API rate limiting, fast rejection is usually better than queuing.

---

### Algorithm 4: Fixed Window Counter (Know the Flaw)

#### How It Works

Divide time into fixed windows. Count requests per window.

```java
class FixedWindowCounter {
    private final int maxRequests;
    private final long windowSizeMs;
    private int counter;
    private long windowStart;

    public synchronized boolean allowRequest() {
        long now = System.currentTimeMillis();
        // New window?
        if (now - windowStart >= windowSizeMs) {
            counter = 0;
            windowStart = now;
        }
        if (counter < maxRequests) {
            counter++;
            return true;
        }
        return false;
    }
}
```

#### The Boundary Spike Problem (MUST mention in interview)

```
Limit: 5 req/sec

Window 1 (0:00-1:00)        Window 2 (1:00-2:00)
    │         ████ 5 req     │████ 5 req          │
    │       at 0:58-0:59     │at 1:00-1:01        │
                              
    Within 2 sec: 10 requests passed! 💥 (2x the limit)
```

> **Verdict**: Simple but **flawed at window boundaries**. Always mention this flaw, then say "this is why we use Sliding Window Counter instead."

---

### Algorithm 5: Sliding Window Log (Know Why It's Impractical)

#### How It Works

Store the **timestamp of every single request** in a sorted set. On each new request, remove all timestamps older than the window, then count remaining.

```java
class SlidingWindowLog {
    private final int maxRequests;
    private final long windowSizeMs;
    private final TreeMap<Long, Integer> requestLog = new TreeMap<>(); // sorted timestamps

    public synchronized boolean allowRequest() {
        long now = System.currentTimeMillis();
        long windowStart = now - windowSizeMs;

        // Remove expired entries
        requestLog.headMap(windowStart).clear();

        // Count requests in window
        int count = requestLog.values().stream().mapToInt(i -> i).sum();

        if (count < maxRequests) {
            requestLog.merge(now, 1, Integer::sum);
            return true;
        }
        return false;
    }
}
```

#### Why It's Impractical

| Problem | Detail |
|---------|--------|
| **O(N) memory** per user | Stores every timestamp. 1000 req/min × 1M users = 1 billion entries |
| **O(N) cleanup** | Must scan and delete old entries on every request |
| **Redis ZSET works but is expensive** | `ZRANGEBYSCORE` + `ZCARD` + `ZADD` per request |

> **Verdict**: **Perfectly accurate** but **too expensive at scale**. Sliding Window Counter gives 99.7% accuracy at O(1) cost.

---

## ⚖️ Algorithm Comparison — Interview Cheat Sheet

```
                    Accuracy
                       ↑
    Sliding Window Log ●  (perfect but O(N) memory — impractical)
                       |
  Sliding Window Ctr ⭐ ●  (99.7% accurate, O(1) — BEST trade-off)
                       |
         Token Bucket ⭐ ●  (good, allows bursts — MOST popular)
                       |
        Leaky Bucket   ●  (good, but queues — use for traffic shaping)
                       |
   Fixed Window Ctr    ●  (boundary spike flaw — know to avoid)
                       |
                       └──────────────────────→ Memory Efficiency
```

> 🎤 **Interviewer**: *"Which algorithm would you choose and why?"*  
> **Strong answer**: *"Token Bucket for most API rate limiting — O(1), simple, allows bursts which matches real user behavior, and it's what AWS/Stripe use. If boundary accuracy matters (like billing or compliance), I'd use Sliding Window Counter. They can also be layered — Token Bucket for per-second, Sliding Window for per-day."*

---

## 🏗️ Phase 3: High-Level Architecture

### Where Does the Rate Limiter Sit?

```
   Client                          Server
     │                               │
     │──── Request ──────────────▶│  │
     │                            │  │
     │     ┌──────────────────┐   │  │
     │     │   API Gateway /   │   │  │
     │     │   Load Balancer   │   │  │
     │     │                    │   │  │
     │     │  ┌──────────────┐ │   │  │
     │     │  │ Rate Limiter │ │   │  │
     │     │  │  Middleware   │ │   │  │
     │     │  └──────┬───────┘ │   │  │
     │     │         │         │   │  │
     │     │    ┌────▼─────┐   │   │  │
     │     │    │  Redis    │   │   │  │
     │     │    │ (Shared   │   │   │  │
     │     │    │  State)   │   │   │  │
     │     │    └──────────┘   │   │  │
     │     └──────────────────┘   │  │
     │                            │  │
     │◀─── 429 or Response ──────│  │
```

### Key Decision: Why Middleware?

| Option | Pros | Cons |
|--------|------|------|
| **Client-side** | Reduces server load | ❌ Easily bypassed, untrustworthy |
| **Server-side** (in app code) | Full control | ❌ Couples rate limiting with business logic |
| **Middleware** (API Gateway) ⭐ | Decoupled, centralized, reusable | Slight extra hop |

> Real-world: **API Gateways** (Kong, AWS API Gateway, Nginx) have built-in rate limiting. Don't reinvent unless the interview demands it.

### Distributed Rate Limiter Architecture

```
                    ┌────────────────────┐
                    │   Rules Config DB   │
                    │  (rules per API)    │
                    └────────┬───────────┘
                             │ load rules
                             ▼
  ┌──────────┐      ┌──────────────┐      ┌──────────┐
  │ Server 1 │─────▶│    Redis     │◀─────│ Server 2 │
  │ (Rate    │      │  (Counters)  │      │ (Rate    │
  │ Limiter) │      │              │      │ Limiter) │
  └──────────┘      └──────────────┘      └──────────┘
       │                                       │
       ▼                                       ▼
  ┌──────────┐                            ┌──────────┐
  │ App      │                            │ App      │
  │ Server 1 │                            │ App      │
  └──────────┘                            │ Server 2 │
                                          └──────────┘
```

### Why Redis?

| Reason | Detail |
|--------|--------|
| **In-memory** | Sub-millisecond latency |
| **Atomic operations** | `INCR`, `EXPIRE`, Lua scripts |
| **TTL support** | Auto-cleanup of expired windows |
| **Distributed** | Shared state across all app servers |
| **Battle-tested** | Used by Stripe, GitHub, Cloudflare |

---

## 🔌 Phase 4: API & Response Headers

### Rate Limit Response Headers (Industry Standard)

```http
HTTP/1.1 429 Too Many Requests
Content-Type: application/json
Retry-After: 5
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1672531200

{
    "error": "Rate limit exceeded",
    "message": "Too many requests. Please retry after 5 seconds.",
    "retry_after": 5
}
```

| Header | Meaning |
|--------|---------|
| `X-RateLimit-Limit` | Max requests allowed in window |
| `X-RateLimit-Remaining` | Requests left in current window |
| `X-RateLimit-Reset` | Unix timestamp when window resets |
| `Retry-After` | Seconds to wait before retrying |

> 🎤 **Cross-question**: *"Should we return these headers on EVERY response or only on 429?"*  
> **Answer**: **Every response**. It lets well-behaved clients self-throttle before hitting the limit. GitHub, Twitter, Stripe all do this.

---

## 🌐 Phase 5: Distributed System Deep Dive (SDE3 Territory)

### Problem: Race Conditions in Distributed Rate Limiting

```
Server 1                         Server 2
────────                         ────────
GET counter → 99                 GET counter → 99
99 < 100? → allow                99 < 100? → allow
SET counter → 100                SET counter → 100  ← should be 101! 💥
```

Both servers read 99, both allow, but only one increment registers. Result: **101 requests allowed instead of 100**.

### Solution: Redis Lua Script (Atomic Read-Increment-Check)

```lua
-- KEYS[1] = rate_limit:{client_id}:{window}
-- ARGV[1] = max_requests
-- ARGV[2] = window_size_seconds

local current = redis.call('INCR', KEYS[1])

-- First request in this window — set expiry
if current == 1 then
    redis.call('EXPIRE', KEYS[1], tonumber(ARGV[2]))
end

if current > tonumber(ARGV[1]) then
    return 0  -- Rejected
end
return 1      -- Allowed
```

> **Why this works**: `INCR` in Redis is **atomic**. It increments AND returns the new value in one operation. No race condition possible.

### Alternative: Redis MULTI/EXEC Pipeline

```
MULTI
INCR rate_limit:user123:window_14
EXPIRE rate_limit:user123:window_14 60
EXEC
```

> But Lua script is preferred — `MULTI/EXEC` doesn't allow conditional logic within the transaction.

### What if Redis Goes Down?

| Strategy | Behavior | Risk |
|----------|----------|------|
| **Fail-open** ⭐ | Allow all traffic if Redis is unavailable | Over-limit traffic gets through |
| **Fail-closed** | Reject all traffic if Redis is unavailable | Service outage for all users |
| **Local fallback** | Fall back to per-server in-memory rate limiting | Less accurate but functional |

> **Production choice**: **Fail-open** with alerting. Rate limiting is a **safety net**, not a gate. It's better to allow some excess traffic than to block all legitimate users.

### Multi-Data-Center Rate Limiting

```
         US-East                    EU-West
    ┌───────────────┐          ┌───────────────┐
    │  App Servers   │          │  App Servers   │
    │       │        │          │       │        │
    │  ┌────▼─────┐  │          │  ┌────▼─────┐  │
    │  │ Redis-US  │◀─── async replication ──▶│ Redis-EU  │
    │  └──────────┘  │          │  └──────────┘  │
    └───────────────┘          └───────────────┘
```

**Two approaches:**

| Approach | How | Trade-off |
|----------|-----|-----------|
| **Centralized Redis** | All servers hit one Redis cluster | ✅ Accurate | ❌ Cross-region latency |
| **Local Redis + Async Sync** ⭐ | Each DC has local Redis, sync periodically | ✅ Low latency | ⚠️ Temporarily over-limit (eventual consistency) |

> **Production choice**: Local Redis + async sync. Accept slight over-counting (e.g., 105 instead of 100) for sub-ms latency. For strict limits (billing), use centralized Redis.

---

## ⚙️ Phase 6: Rules Configuration

### Rules Config Schema

```json
{
    "rules": [
        {
            "id": "login-brute-force",
            "endpoint": "/api/login",
            "key": "ip",
            "algorithm": "sliding_window",
            "limit": 5,
            "window_seconds": 60,
            "action": "reject"
        },
        {
            "id": "api-general",
            "endpoint": "/api/*",
            "key": "user_id",
            "algorithm": "token_bucket",
            "bucket_size": 50,
            "refill_rate": 10,
            "action": "reject"
        },
        {
            "id": "daily-cap",
            "endpoint": "/api/*",
            "key": "user_id",
            "algorithm": "sliding_window",
            "limit": 10000,
            "window_seconds": 86400,
            "action": "soft_reject"
        }
    ]
}
```

### Rule Evaluation Order

```
Request arrives → Extract key (userId / IP / API key)
       │
       ▼
  Match rules by endpoint (most specific first)
       │
       ▼
  Check ALL matching rules (layered)
       │
  ┌────▼────┐     ┌────────────┐     ┌──────────┐
  │ 10/sec  │ AND │ 100/min    │ AND │ 1000/day │
  └────┬────┘     └──────┬─────┘     └────┬─────┘
       │                 │                 │
       └────── ALL must pass ──────────────┘
                         │
                    ✅ or ❌
```

> **Key insight**: Rules are **layered**, not exclusive. A request must pass ALL applicable rules.

---

## ❓ Interview Cross-Questions & Answers

### Algorithm Questions

> **Q: "Token Bucket vs Sliding Window — when would you use each?"**  
> A: Token Bucket when you want to **allow bursts** (APIs — users click, wait, click). Sliding Window when you need **strict, accurate counting** (billing, compliance). Often **layer both**: Token Bucket for per-second burst control, Sliding Window for per-hour/per-day caps.

> **Q: "How is Token Bucket different from Leaky Bucket?"**  
> A: Token Bucket controls the **sending rate** and allows bursts. Leaky Bucket controls the **processing rate** and smooths output. Token Bucket says "you can send up to N instantly." Leaky Bucket says "I'll process exactly R per second, queue the rest." For API rate limiting, Token Bucket is better because fast yes/no is preferred over queuing.

> **Q: "What's wrong with Fixed Window Counter?"**  
> A: **Boundary spike** — a client can send 2x the limit across a window boundary. E.g., 100 requests at 0:59 + 100 at 1:00 = 200 in 2 seconds. Sliding Window Counter fixes this by weighting the previous window's count.

### Architecture Questions

> **Q: "Why Redis and not a local in-memory store?"**  
> A: With multiple app servers behind a load balancer, each server only sees a fraction of a user's requests. User sends 100 requests → load balancer distributes them across 10 servers → each server sees only 10 → no server triggers the limit. **Shared state** (Redis) is mandatory for accurate distributed rate limiting.

> **Q: "What if Redis is a bottleneck?"**  
> A: (1) Redis handles ~100K ops/sec single-threaded — that's a lot. (2) Use **Redis Cluster** for horizontal sharding. (3) **Batch local counts** and flush to Redis periodically (trade accuracy for throughput). (4) For extreme scale, use **local rate limiting + probabilistic sync**.

> **Q: "What if Redis fails?"**  
> A: **Fail-open**. Allow traffic through. Rate limiting is a protection mechanism, not a gate. If Redis is down for 30 seconds, some users might exceed limits briefly — that's acceptable. The alternative (fail-closed = block ALL traffic) is a self-inflicted outage.

> **Q: "Can you rate limit without Redis?"**  
> A: Yes — if you use **sticky sessions** (load balancer always routes same user to same server), you can rate limit in local memory. But sticky sessions have their own problems (uneven load, failover). Redis is the standard approach.

### Design Questions

> **Q: "How would you handle different rate limits for free vs premium users?"**  
> A: The throttle key includes a **tier lookup**. On each request: (1) Extract user ID, (2) Look up user tier from cache/DB, (3) Select the rule set for that tier. Store tier-specific rules in the config. Premium users get higher limits.

> **Q: "Should rate limiting be per-endpoint or global?"**  
> A: **Both** — layered rules. `/api/login` gets a strict per-IP limit (brute force protection). `/api/search` gets a per-user limit. A global per-user limit caps total API usage. All rules apply simultaneously.

> **Q: "How do you handle rate limiting for WebSocket connections?"**  
> A: Rate limit at the **message level**, not the connection level. Each WebSocket message counts as a request. Use Token Bucket per connection — each message consumes a token.

### Edge Case Questions

> **Q: "What about clock skew across servers?"**  
> A: With Redis as the central counter, clock skew doesn't matter — Redis uses its own clock for EXPIRE. If using local timestamps, use **NTP synchronization** and design algorithms that are tolerant of small skew (Token Bucket is naturally tolerant since it uses elapsed time).

> **Q: "What if a malicious user rotates IP addresses?"**  
> A: IP-based rate limiting alone isn't enough. Layer: (1) IP rate limit, (2) API key / auth token rate limit, (3) **Fingerprinting** (device, browser, behavioral patterns), (4) CAPTCHA after threshold.

> **Q: "How do you test a rate limiter?"**  
> A: (1) Unit test each algorithm with controlled timestamps. (2) Load test with tools like `wrk` or `k6`. (3) **Chaos test** — kill Redis, verify fail-open behavior. (4) Test boundary conditions — exactly at the limit, one over, window transitions.

---

## ✅ Interview Checklist

- [x] Clarify requirements (3 min) — where it sits, throttle key, what happens on 429
- [x] Explain algorithms — mention all 5, deep dive Token Bucket + Sliding Window Counter
- [x] Code one algorithm — Token Bucket (5 min whiteboard)
- [x] Draw HLD architecture — middleware + Redis + API Gateway
- [x] Discuss distributed challenges — race conditions, Redis Lua script
- [x] Cover failure modes — fail-open vs fail-closed
- [x] Response headers — 429 + Retry-After + X-RateLimit headers
- [x] Rules configuration — layered, per-endpoint, dynamic
- [x] Cross-questions — clock skew, multi-DC, Redis failure, testing

---

> 💡 **Pro Tip**: In the interview, start with: *"I'll use Token Bucket for the core algorithm, Redis for shared state, and deploy it as API Gateway middleware."* Then let the interviewer drill into whichever area they care about. Show you know the algorithm trade-offs cold — that's what separates SDE2 from SDE3.
