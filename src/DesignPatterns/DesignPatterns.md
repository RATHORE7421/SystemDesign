# 🎨 Design Patterns — Complete Interview Revision Guide

> **Target**: SDE2/SDE3 | **Total Patterns**: 10  
> **Rule**: Every pattern here is mapped to real LLD/HLD problems you've solved.

---

## 🗺️ Pattern Quick Map — Where Each Pattern Appears

| Pattern | Category | Your LLD/HLD Problems | One-Line Purpose |
|---------|----------|----------------------|-----------------|
| **Strategy** | Behavioral | Parking Lot, Rate Limiter, Booking System | Swap algorithms at runtime |
| **Observer** | Behavioral | Booking System, Chat Messenger, Uber | Notify multiple listeners on state change |
| **State** | Behavioral | Hospital System, Elevator, Booking System | Object changes behavior based on internal state |
| **Command** | Behavioral | Text Editor (Undo/Redo), Chess | Encapsulate actions as objects for undo/redo/queue |
| **Factory** | Creational | Parking Lot (Spots), Booking System (Tickets) | Create objects without exposing instantiation logic |
| **Singleton** | Creational | Parking Lot, Rate Limiter (Redis connection), Cache | Ensure only one instance exists |
| **Builder** | Creational | URL Shortener (Config), Complex query objects | Build complex objects step by step |
| **Facade** | Structural | Hospital System, Shopping, Booking System | One simple interface hiding complex subsystems |
| **Adapter** | Structural | Payment Gateway integration, Media Player | Make incompatible interfaces work together |
| **Chain of Responsibility** | Behavioral | Rate Limiter (middleware), API Gateway, Logging | Pass request along a chain — each handler decides to process or forward |

---

## 1️⃣ Strategy Pattern ⭐ (Most Used in Interviews)

### One-Liner
> *"Define a family of algorithms, encapsulate each one, and make them interchangeable."*

### When to Use
When you have **multiple ways to do the same thing** and want to switch at runtime.

### The Pattern

```
Context (has-a Strategy) ──────▶ Strategy Interface
                                    ▲
                              ┌─────┼─────┐
                              ▼     ▼     ▼
                          AlgoA  AlgoB  AlgoC
```

### Where It Appears in Your LLD Problems

| Problem | Strategy Interface | Concrete Strategies | What It Swaps |
|---------|-------------------|---------------------|---------------|
| **Parking Lot** | `ParkingStrategy` | `LowestFloorFirst`, `NearestToExit` | Slot assignment algorithm |
| **Rate Limiter** | `RateLimitAlgorithm` | `TokenBucket`, `SlidingWindowCounter` | Rate limiting algorithm |
| **Booking System** | `PricingStrategy` | `RegularPricing`, `WeekendPricing`, `HolidayPricing` | Ticket pricing calculation |
| **Uber** | `MatchingStrategy` | `NearestDriver`, `HighestRated`, `LowestFare` | Driver matching algorithm |
| **Payment** | `PaymentStrategy` | `UPI`, `CreditCard`, `Wallet` | Payment processing method |

### LLD Example — Payment Strategy (Use in ANY problem)

```java
// Step 1: Define the strategy interface
interface PaymentStrategy {
    boolean pay(double amount);
}

// Step 2: Concrete strategies
class UPIPayment implements PaymentStrategy {
    private String upiId;
    public UPIPayment(String upiId) { this.upiId = upiId; }
    public boolean pay(double amount) {
        System.out.println("Paid ₹" + amount + " via UPI: " + upiId);
        return true;
    }
}

class CreditCardPayment implements PaymentStrategy {
    private String cardNumber;
    public CreditCardPayment(String cardNumber) { this.cardNumber = cardNumber; }
    public boolean pay(double amount) {
        System.out.println("Paid ₹" + amount + " via Card: ****" + cardNumber.substring(12));
        return true;
    }
}

class WalletPayment implements PaymentStrategy {
    private double balance;
    public WalletPayment(double balance) { this.balance = balance; }
    public boolean pay(double amount) {
        if (balance < amount) return false;
        balance -= amount;
        System.out.println("Paid ₹" + amount + " via Wallet. Remaining: ₹" + balance);
        return true;
    }
}

// Step 3: Context uses the strategy
class PaymentService {
    private PaymentStrategy strategy;
    
    public void setStrategy(PaymentStrategy strategy) { this.strategy = strategy; }
    
    public boolean processPayment(double amount) {
        return strategy.pay(amount);
    }
}
```

### Cross-Questions

> **Q: "Strategy vs if-else — why not just use if-else?"**  
> A: (1) **OCP** — adding a new strategy doesn't modify existing code. (2) **Runtime swap** — call `setStrategy()` to change behavior. (3) **Testability** — inject mock strategy in tests.

> **Q: "When would you NOT use Strategy?"**  
> A: When there are only 2 options and they'll never change. Over-engineering for a simple if-else.

---

## 2️⃣ Observer Pattern ⭐ (Second Most Used)

### One-Liner
> *"When one object changes state, all its dependents are notified automatically."*

### When to Use
When **multiple objects need to react** to a state change, and you don't want tight coupling.

### The Pattern

```
Subject (maintains list of observers)
    │
    │ notify()
    ├──────────▶ Observer A (EmailService)
    ├──────────▶ Observer B (SMSService)
    └──────────▶ Observer C (DashboardService)
```

### Where It Appears in Your LLD/HLD Problems

| Problem | Subject (Publisher) | Observers (Subscribers) | What Event? |
|---------|-------------------|------------------------|-------------|
| **Booking System** | `Show` | `WaitlistUsers` | Seat becomes available → notify waitlisted users |
| **Chat Messenger** | `ChatRoom` / `Group` | `Users` in the group | New message → notify all group members |
| **Uber** | `RideRequest` | `NearbyDrivers` | New ride → notify drivers |
| **Hospital System** | `PatientQueue` | `Doctors` | Critical patient added → alert doctors |
| **Stock Trading** | `Stock` | `Traders`, `Dashboards` | Price change → notify subscribers |

### LLD Example — Notification System (Use in Booking/Chat/Uber)

```java
import java.util.*;

// Step 1: Observer interface
interface NotificationObserver {
    void update(String event, String data);
}

// Step 2: Concrete observers
class EmailNotifier implements NotificationObserver {
    private String email;
    public EmailNotifier(String email) { this.email = email; }
    public void update(String event, String data) {
        System.out.println("📧 Email to " + email + ": [" + event + "] " + data);
    }
}

class SMSNotifier implements NotificationObserver {
    private String phone;
    public SMSNotifier(String phone) { this.phone = phone; }
    public void update(String event, String data) {
        System.out.println("📱 SMS to " + phone + ": [" + event + "] " + data);
    }
}

class PushNotifier implements NotificationObserver {
    private String deviceId;
    public PushNotifier(String deviceId) { this.deviceId = deviceId; }
    public void update(String event, String data) {
        System.out.println("🔔 Push to " + deviceId + ": [" + event + "] " + data);
    }
}

// Step 3: Subject
class EventPublisher {
    private Map<String, List<NotificationObserver>> listeners = new HashMap<>();
    
    public void subscribe(String eventType, NotificationObserver observer) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(observer);
    }
    
    public void unsubscribe(String eventType, NotificationObserver observer) {
        List<NotificationObserver> list = listeners.get(eventType);
        if (list != null) list.remove(observer);
    }
    
    public void publish(String eventType, String data) {
        List<NotificationObserver> list = listeners.get(eventType);
        if (list != null) {
            for (NotificationObserver observer : list) {
                observer.update(eventType, data);
            }
        }
    }
}

// Usage in Booking System:
// eventPublisher.subscribe("SEAT_AVAILABLE", emailNotifier);
// eventPublisher.subscribe("SEAT_AVAILABLE", smsNotifier);
// eventPublisher.publish("SEAT_AVAILABLE", "Show: Avengers, Seat: A5 is now free");
```

### Cross-Questions

> **Q: "Observer vs direct method calls — why not just call emailService.send() directly?"**  
> A: Coupling. If you add PushNotification tomorrow, you'd modify the booking code. With Observer, just add a new observer — OCP.

> **Q: "What about async notifications?"**  
> A: In production, observers push to a **message queue** (Kafka/RabbitMQ) instead of calling synchronously. This prevents a slow SMS service from blocking the booking flow.

---

## 3️⃣ State Pattern

### One-Liner
> *"An object changes its behavior when its internal state changes — as if it changed its class."*

### When to Use
When an object has **multiple states** and **different behavior in each state**.

### State vs If-Else

```java
// ❌ WITHOUT State Pattern (if-else hell):
void handleRequest() {
    if (state == "PENDING") { ... }
    else if (state == "CONFIRMED") { ... }
    else if (state == "CANCELLED") { ... }
    // 20 more states = unreadable mess
}

// ✅ WITH State Pattern:
currentState.handle(this);  // State object decides behavior
```

### Where It Appears

| Problem | Context | States | Transitions |
|---------|---------|--------|-------------|
| **Elevator** | `Elevator` | `IdleState`, `MovingUpState`, `MovingDownState`, `DoorOpenState` | Button press triggers transition |
| **Hospital System** | `Patient` | `Registered`, `InQueue`, `InConsultation`, `InTreatment`, `Discharged` | Each step transitions to next |
| **Booking System** | `Booking` | `Pending`, `Confirmed`, `Cancelled`, `Completed` | Payment/cancellation changes state |
| **Uber** | `Ride` | `Requested`, `DriverAssigned`, `InProgress`, `Completed`, `Cancelled` | Driver accept/ride events |
| **Vending Machine** | `Machine` | `Idle`, `CoinInserted`, `ItemSelected`, `Dispensing` | Classic interview example |

### LLD Example — Order State (Use in Booking/Uber/E-commerce)

```java
// Step 1: State interface
interface OrderState {
    void next(OrderContext context);
    void cancel(OrderContext context);
    String getStatus();
}

// Step 2: Concrete states
class PendingState implements OrderState {
    public void next(OrderContext ctx) {
        System.out.println("Order confirmed! Moving to processing...");
        ctx.setState(new ConfirmedState());
    }
    public void cancel(OrderContext ctx) {
        System.out.println("Order cancelled.");
        ctx.setState(new CancelledState());
    }
    public String getStatus() { return "PENDING"; }
}

class ConfirmedState implements OrderState {
    public void next(OrderContext ctx) {
        System.out.println("Order shipped!");
        ctx.setState(new ShippedState());
    }
    public void cancel(OrderContext ctx) {
        System.out.println("Cannot cancel — already confirmed. Contact support.");
    }
    public String getStatus() { return "CONFIRMED"; }
}

class ShippedState implements OrderState {
    public void next(OrderContext ctx) {
        System.out.println("Order delivered!");
        ctx.setState(new DeliveredState());
    }
    public void cancel(OrderContext ctx) {
        System.out.println("Cannot cancel — already shipped.");
    }
    public String getStatus() { return "SHIPPED"; }
}

class DeliveredState implements OrderState {
    public void next(OrderContext ctx) {
        System.out.println("Order already delivered. No next state.");
    }
    public void cancel(OrderContext ctx) {
        System.out.println("Cannot cancel delivered order. Initiate return.");
    }
    public String getStatus() { return "DELIVERED"; }
}

class CancelledState implements OrderState {
    public void next(OrderContext ctx) {
        System.out.println("Order is cancelled. No next state.");
    }
    public void cancel(OrderContext ctx) {
        System.out.println("Already cancelled.");
    }
    public String getStatus() { return "CANCELLED"; }
}

// Step 3: Context
class OrderContext {
    private OrderState state;
    
    public OrderContext() { this.state = new PendingState(); }
    
    public void setState(OrderState state) { this.state = state; }
    public void next() { state.next(this); }
    public void cancel() { state.cancel(this); }
    public String getStatus() { return state.getStatus(); }
}

// Usage:
// OrderContext order = new OrderContext();  // PENDING
// order.next();    // → CONFIRMED
// order.next();    // → SHIPPED
// order.cancel();  // "Cannot cancel — already shipped"
// order.next();    // → DELIVERED
```

### Cross-Questions

> **Q: "State pattern vs enum with switch?"**  
> A: Enum + switch works for simple cases. State pattern shines when (1) behavior per state is complex, (2) transitions have side effects, (3) you need to add states without modifying existing code (OCP).

> **Q: "Can you transition from any state to any other?"**  
> A: No! Each state controls its own valid transitions. `ShippedState.cancel()` refuses — this prevents invalid transitions at compile-time rather than with if-else checks.

---

## 4️⃣ Command Pattern

### One-Liner
> *"Encapsulate a request as an object — so you can queue it, undo it, log it, or replay it."*

### When to Use
When you need **undo/redo**, **action history**, or **queuing operations**.

### Where It Appears

| Problem | Command | Receiver | Why |
|---------|---------|----------|-----|
| **Text Editor** (your code) | `AppendTextCommand` | `TextDocument` | Undo/Redo typing |
| **Chess** | `MoveCommand` | `ChessBoard` | Undo moves, replay game history |
| **Booking System** | `BookSeatCommand`, `CancelBookingCommand` | `BookingService` | Log all booking actions, rollback on failure |
| **Smart Home / IoT** | `TurnOnLightCommand`, `SetThermostatCommand` | `Light`, `Thermostat` | Remote control with undo |

### Cross-Questions

> **Q: "Command vs direct method call?"**  
> A: Direct call is fire-and-forget. Command objects can be **stored** (for undo), **queued** (for batch processing), **serialized** (for remote execution), and **logged** (for audit trail).

---

## 5️⃣ Factory Pattern

### One-Liner
> *"Create objects without exposing the creation logic — client calls a factory method, gets the right subclass."*

### When to Use
When **object creation depends on input/config** and you don't want the client to know which concrete class is created.

### Where It Appears

| Problem | Factory | Creates | Based On |
|---------|---------|---------|----------|
| **Parking Lot** | `SpotFactory` | `CompactSpot`, `RegularSpot`, `LargeSpot` | `SpotType` enum |
| **Booking System** | `TicketFactory` | `RegularTicket`, `VIPTicket`, `StudentTicket` | User type |
| **Rate Limiter** | `RateLimiterFactory` | `TokenBucket`, `SlidingWindow`, `LeakyBucket` | Config rule |
| **Notification System** | `NotifierFactory` | `EmailNotifier`, `SMSNotifier`, `PushNotifier` | Channel type |

### LLD Example — Notification Factory (Use in any problem)

```java
// Factory that creates the right notifier based on channel
class NotifierFactory {
    public static NotificationObserver create(String channel, String target) {
        switch (channel.toLowerCase()) {
            case "email": return new EmailNotifier(target);
            case "sms":   return new SMSNotifier(target);
            case "push":  return new PushNotifier(target);
            default: throw new IllegalArgumentException("Unknown channel: " + channel);
        }
    }
}

// Usage:
// NotificationObserver notifier = NotifierFactory.create("email", "priya@gmail.com");
// notifier.update("BOOKING", "Your ticket is confirmed!");
```

### Cross-Questions

> **Q: "Factory vs direct `new`?"**  
> A: (1) Client doesn't know concrete class. (2) Adding new types = modify only factory, not all callers. (3) Can return cached/pooled objects.

> **Q: "Factory Method vs Abstract Factory?"**  
> A: Factory Method = one method creates one type. Abstract Factory = a factory that creates **families** of related objects (e.g., `UIFactory` creates `Button + TextField + Menu` for Windows vs Mac).

---

## 6️⃣ Singleton Pattern

### One-Liner
> *"Ensure a class has only one instance and provide global access to it."*

### Where It Appears

| Problem | Singleton Class | Why Single Instance? |
|---------|----------------|---------------------|
| **Parking Lot** | `ParkingLot` | One physical lot = one orchestrator |
| **Rate Limiter** | `RedisConnection` | One connection pool to Redis |
| **Cache** (your code) | `Cache` | One shared cache across the app |
| **Config** | `AppConfig` | One config object, loaded once |
| **Logger** | `Logger` | One log stream, don't want multiple loggers |

### The Three Implementations (You Already Have These!)

```
1. Eager:     private static final Instance INST = new Instance();  // Simple, safe
2. Lazy:      if (inst == null) inst = new Instance();              // Not thread-safe!
3. DCL:       if (inst == null) { synchronized { if (null) ... } }  // Thread-safe ✅
```

### Cross-Questions

> **Q: "When should you NOT use Singleton?"**  
> A: When you need multiple instances (hospital chain, multiple parking lots). Singleton makes testing hard (global state). Prefer dependency injection.

---

## 7️⃣ Builder Pattern

### One-Liner
> *"Build complex objects step by step — especially when constructor has too many parameters."*

### When to Use
When constructor would have **5+ parameters**, many optional.

### Where It Appears

| Problem | What's Built | Why Builder? |
|---------|-------------|-------------|
| **URL Shortener** | `URLConfig(expiry, customAlias, maxClicks, ...)` | Many optional settings |
| **Booking System** | `Ticket(show, seat, user, price, discount, ...)` | Complex ticket with optional fields |
| **Notification** | `Notification(title, body, channel, priority, image, ...)` | Many optional fields |
| **Search Query** | `SearchQuery(keyword, filters, sort, page, limit)` | Flexible query building |

### LLD Example — Search Query Builder

```java
class SearchQuery {
    private String keyword;
    private String category;
    private double minPrice;
    private double maxPrice;
    private String sortBy;
    private int page;
    private int limit;

    private SearchQuery(Builder builder) {
        this.keyword = builder.keyword;
        this.category = builder.category;
        this.minPrice = builder.minPrice;
        this.maxPrice = builder.maxPrice;
        this.sortBy = builder.sortBy;
        this.page = builder.page;
        this.limit = builder.limit;
    }

    public static class Builder {
        private String keyword;            // Required
        private String category;           // Optional
        private double minPrice = 0;       // Optional with default
        private double maxPrice = Double.MAX_VALUE;
        private String sortBy = "relevance";
        private int page = 1;
        private int limit = 20;

        public Builder(String keyword) { this.keyword = keyword; }  // Required param
        
        public Builder category(String c) { this.category = c; return this; }
        public Builder priceRange(double min, double max) { this.minPrice = min; this.maxPrice = max; return this; }
        public Builder sortBy(String s) { this.sortBy = s; return this; }
        public Builder page(int p) { this.page = p; return this; }
        public Builder limit(int l) { this.limit = l; return this; }

        public SearchQuery build() { return new SearchQuery(this); }
    }
}

// Usage — clean, readable:
// SearchQuery query = new SearchQuery.Builder("laptop")
//     .category("electronics")
//     .priceRange(30000, 80000)
//     .sortBy("price_asc")
//     .page(1)
//     .build();
```

### Cross-Questions

> **Q: "Builder vs constructor with optional params?"**  
> A: Constructor with 10 params is unreadable — `new Ticket(show, null, user, 500, null, null, true, false, ...)`. Builder makes it clear which params are set.

---

## 8️⃣ Facade Pattern

### One-Liner
> *"Provide a simple interface to a complex subsystem."*

### Where It Appears

| Problem | Facade | Hides What? |
|---------|--------|------------|
| **Hospital System** | `HospitalManagementSystem` | `ReceptionService`, `DoctorService`, `RecordService`, `PatientQueue` |
| **Booking System** | `BookingFacade` | `ShowManager`, `SeatManager`, `PaymentService`, `TicketService` |
| **Shopping** (your code) | `ShoppingFacade` | `InventoryService`, `PaymentService`, `ShippingService`, `NotificationService` |
| **Uber** | `RideService` | `MatchingService`, `PricingService`, `NotificationService`, `PaymentService` |

### Cross-Questions

> **Q: "Facade vs just calling services directly?"**  
> A: Without Facade, the client must know the order: check inventory → process payment → ship → notify. Facade encapsulates this workflow. If the order changes, only Facade is modified.

---

## 9️⃣ Adapter Pattern

### One-Liner
> *"Convert one interface into another that the client expects — makes incompatible interfaces work together."*

### When to Use
When you're integrating a **third-party library or legacy system** whose interface doesn't match yours.

### Where It Appears

| Problem | Adapter | Adapts What? | To What? |
|---------|---------|-------------|----------|
| **Payment Gateway** | `RazorpayAdapter` | Razorpay's API | Your `PaymentGateway` interface |
| **Notification** | `FCMAdapter` | Firebase Cloud Messaging API | Your `PushNotifier` interface |
| **Logging** | `Log4jAdapter` | Log4j API | Your `Logger` interface |
| **Database** | `MongoAdapter` | MongoDB driver | Your `Repository` interface |

### LLD Example — Payment Gateway Adapter

```java
// Your interface (what your system expects)
interface PaymentGateway {
    boolean processPayment(String userId, double amount);
}

// Third-party Razorpay (their interface — you can't change it)
class RazorpaySDK {
    public String createOrder(double amount, String currency) {
        return "RAZORPAY_ORDER_" + System.currentTimeMillis();
    }
    public boolean capturePayment(String orderId) {
        System.out.println("Razorpay: Payment captured for " + orderId);
        return true;
    }
}

// Adapter — bridges YOUR interface with THEIR SDK
class RazorpayAdapter implements PaymentGateway {
    private RazorpaySDK razorpay = new RazorpaySDK();
    
    public boolean processPayment(String userId, double amount) {
        String orderId = razorpay.createOrder(amount, "INR");
        return razorpay.capturePayment(orderId);
    }
}

// Tomorrow, switch to Stripe? Just write StripeAdapter.
class StripeAdapter implements PaymentGateway {
    // ... wraps Stripe's SDK to match your PaymentGateway interface
    public boolean processPayment(String userId, double amount) { return true; }
}

// Usage — your code doesn't care which payment provider:
// PaymentGateway gateway = new RazorpayAdapter();  // or new StripeAdapter()
// gateway.processPayment("user123", 999.0);
```

### Cross-Questions

> **Q: "Adapter vs Facade?"**  
> A: **Adapter** changes an interface to match what you need (1-to-1 wrapping). **Facade** simplifies a complex subsystem (many classes → one simple interface). Adapter = compatibility. Facade = simplification.

---

## 🔟 Chain of Responsibility Pattern

### One-Liner
> *"Pass a request along a chain of handlers — each handler decides to either process it or pass it to the next."*

### When to Use
When a request should go through **multiple checks/steps in sequence**, and each step can either handle it, modify it, or pass it forward.

### The Pattern

```
Request → [Handler A] → [Handler B] → [Handler C] → Final Result
              │               │               │
         "Can I handle it?"  "My turn?"    "My turn?"
              │               │               │
         Process or pass  Process or pass  Process or pass
```

### Real-World Analogy

Think of an **office leave request**:

```
Employee submits leave
       │
       ▼
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Team Lead   │────▶│   Manager    │────▶│   Director   │
│ (≤2 days)    │     │ (≤7 days)    │     │ (≤30 days)   │
└──────────────┘     └──────────────┘     └──────────────┘
     "2 days?                "5 days?            "20 days?
      I'll approve"           I'll approve"       I'll approve"
```

Each level handles what it can, forwards what it can't.

### Where It Appears in Your LLD/HLD Problems

| Problem | Chain | Handlers | What Passes Through |
|---------|-------|----------|--------------------|
| **Rate Limiter** | Middleware chain | `AuthMiddleware` → `RateLimitMiddleware` → `LoggingMiddleware` → `Controller` | HTTP Request |
| **API Gateway** | Request pipeline | `Authentication` → `Authorization` → `RateLimit` → `RouteToService` | API Request |
| **Logging** | Log level chain | `ErrorHandler` → `WarnHandler` → `InfoHandler` → `DebugHandler` | Log message |
| **Booking System** | Validation chain | `SeatAvailableCheck` → `PaymentCheck` → `UserVerifiedCheck` → `Book` | Booking request |
| **Hospital System** | Triage chain | `CriticalHandler` → `HighHandler` → `MediumHandler` → `LowHandler` | Patient |

### LLD Example — Request Validation Chain (Use in ANY API problem)

```java
// Step 1: Abstract handler
abstract class RequestHandler {
    private RequestHandler next;

    public RequestHandler setNext(RequestHandler next) {
        this.next = next;
        return next;  // Return next for chaining: a.setNext(b).setNext(c)
    }

    public boolean handle(Request request) {
        // If I can't handle or I pass, forward to next
        if (next != null) {
            return next.handle(request);
        }
        return true;  // End of chain — all checks passed
    }
}

// Step 2: Concrete handlers
class AuthenticationHandler extends RequestHandler {
    @Override
    public boolean handle(Request request) {
        if (request.getToken() == null || request.getToken().isEmpty()) {
            System.out.println("❌ Authentication failed: No token");
            return false;  // STOP the chain
        }
        System.out.println("✅ Authentication passed");
        return super.handle(request);  // Forward to next handler
    }
}

class RateLimitHandler extends RequestHandler {
    private Map<String, Integer> requestCounts = new HashMap<>();
    private final int MAX_REQUESTS = 100;

    @Override
    public boolean handle(Request request) {
        String userId = request.getUserId();
        int count = requestCounts.getOrDefault(userId, 0);
        if (count >= MAX_REQUESTS) {
            System.out.println("❌ Rate limit exceeded for user: " + userId);
            return false;  // STOP the chain
        }
        requestCounts.put(userId, count + 1);
        System.out.println("✅ Rate limit check passed (" + (count+1) + "/" + MAX_REQUESTS + ")");
        return super.handle(request);  // Forward to next
    }
}

class AuthorizationHandler extends RequestHandler {
    private Set<String> adminEndpoints = Set.of("/admin", "/config");

    @Override
    public boolean handle(Request request) {
        if (adminEndpoints.contains(request.getEndpoint()) && !request.isAdmin()) {
            System.out.println("❌ Authorization failed: Not admin");
            return false;
        }
        System.out.println("✅ Authorization passed");
        return super.handle(request);
    }
}

class LoggingHandler extends RequestHandler {
    @Override
    public boolean handle(Request request) {
        System.out.println("📝 Logging: " + request.getMethod() + " " + request.getEndpoint());
        return super.handle(request);  // Always passes through
    }
}

// Step 3: Build the chain
class APIGateway {
    private RequestHandler chain;

    public APIGateway() {
        // Build chain: Auth → RateLimit → Authorization → Logging → (Controller)
        RequestHandler auth = new AuthenticationHandler();
        auth.setNext(new RateLimitHandler())
            .setNext(new AuthorizationHandler())
            .setNext(new LoggingHandler());
        this.chain = auth;
    }

    public boolean processRequest(Request request) {
        System.out.println("\n--- Processing: " + request.getEndpoint() + " ---");
        return chain.handle(request);
    }
}
```

```
// Output for a valid request:
--- Processing: /api/users ---
✅ Authentication passed
✅ Rate limit check passed (1/100)
✅ Authorization passed
📝 Logging: GET /api/users

// Output for an unauthenticated request:
--- Processing: /api/users ---
❌ Authentication failed: No token     ← Chain STOPS here. No further checks.
```

### Chain of Responsibility vs If-Else

```java
// ❌ WITHOUT Chain (all checks in one method — messy, violates SRP):
public boolean process(Request req) {
    if (req.getToken() == null) return false;        // auth
    if (rateLimitExceeded(req)) return false;         // rate limit
    if (isAdmin(req.getEndpoint()) && !req.isAdmin()) return false;  // authz
    log(req);                                         // logging
    return true;
}
// Adding a new check = modify this method. Violates OCP.

// ✅ WITH Chain (each check is a separate class — clean, extensible):
auth.setNext(rateLimit).setNext(authz).setNext(logging);
// Adding a new check = create a new handler class, insert in chain. OCP!
```

### How This Connects to Rate Limiter Middleware

Remember in our [Rate Limiter design](file:///Users/priyarath/Documents/SystemDesign/src/HLDProblems/RateLimiter.md), we said the rate limiter sits as **middleware**? That middleware IS the Chain of Responsibility:

```
Request → [Auth Middleware] → [Rate Limit Middleware] → [CORS Middleware] → Controller
               │                      │                       │
          Each middleware            Each one                Each one
          is a handler              can STOP                can modify
          in the chain              the chain               the request
```

Spring Boot's `HandlerInterceptor`, Express.js `app.use()`, Django middleware — all are Chain of Responsibility!

### Cross-Questions

> **Q: "Chain of Responsibility vs Strategy?"**  
> A: **Strategy** = pick ONE algorithm to use. **Chain** = run MULTIPLE handlers in sequence. Strategy is "which one?" Chain is "all of them, in order."

> **Q: "What if no handler processes the request?"**  
> A: Design choice. Either: (1) the last handler is a default/fallback, or (2) return an error. For middleware, reaching the end of chain = all checks passed → proceed to controller.

> **Q: "Can a handler modify the request before passing it?"**  
> A: Yes! A logging handler can add a request ID. An auth handler can inject the `userId` into the request context. The next handler sees the enriched request.

> **Q: "What about performance — isn't a chain slower than a single method?"**  
> A: Negligible. Each handler is O(1). A chain of 5 handlers is 5 method calls. The benefit (extensibility, SRP, OCP) far outweighs the nanoseconds of overhead.

---

## 🎯 Pattern Decision Cheat Sheet

```
"I need to swap algorithms at runtime"          → STRATEGY
"I need multiple objects to react to an event"   → OBSERVER
"Object behavior changes based on state"         → STATE
"I need undo/redo or action history"             → COMMAND
"I need to create objects based on input"        → FACTORY
"I need exactly one instance"                    → SINGLETON
"Constructor has too many parameters"            → BUILDER
"I need one simple API for complex internals"    → FACADE
"I need to integrate incompatible interface"     → ADAPTER
"Request goes through multiple checks in order"  → CHAIN OF RESPONSIBILITY
```

## 🔗 Patterns Often Used Together

| Combination | Example |
|-------------|---------|
| **Strategy + Factory** | Factory creates the right strategy based on config |
| **Observer + Command** | Observer notifies, Command captures the action for undo |
| **Facade + Singleton** | Facade is the single entry point (ParkingLot) |
| **State + Observer** | State changes trigger notifications to observers |
| **Builder + Factory** | Factory uses Builder internally to construct complex objects |
| **Chain + Strategy** | Chain for middleware pipeline, Strategy to pick the rate limiting algorithm within the chain |

---

## ✅ Interview Checklist

- [x] Know all 10 patterns by one-liner
- [x] For each pattern, know 2–3 LLD problems where it applies
- [x] Can code Strategy, Observer, State, Factory, Chain of Responsibility from memory
- [x] Know the cross-questions for each (especially Strategy vs if-else, State vs enum, Chain vs Strategy)
- [x] Know when NOT to use each pattern (over-engineering trap)
- [x] Know which patterns combine together
