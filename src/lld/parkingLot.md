# 🚗 Parking Lot System Design (LLD)

> **Interview-Ready Guide** | Covers: SOLID, Design Patterns, Concurrency

---

## 📋 Problem Statement

Design a **Parking Lot System** that can:
- Park vehicles of different sizes (Bike, Car, Truck)
- Handle multiple floors with different spot types
- Generate tickets and calculate fees
- Handle concurrent parking requests

---

## 🎯 Requirements

| Requirement | Description |
|-------------|-------------|
| Multi-floor support | Multiple floors, each with various spot types |
| Vehicle types | Bike, Car, Truck |
| Spot types | Compact (bikes), Regular (cars), Large (trucks), Handicapped |
| Ticketing | Generate ticket on entry, calculate fee on exit |
| Spot allocation | Smart allocation based on vehicle size |

---

## 🏗️ Class Diagram

```mermaid
classDiagram
    class Vehicle {
        <<abstract>>
        -licensePlate: String
        -vehicleType: VehicleType
        +getVehicleType()
        +getLicensePlate()
    }
    
    Vehicle <|-- Bike
    Vehicle <|-- Car
    Vehicle <|-- Truck
    
    class Spot {
        <<abstract>>
        -spotId: int
        -isAvailable: boolean
        +tryOccupy(vehicle): boolean
        +vacate()
    }
    
    Spot <|-- CompactSpot
    Spot <|-- RegularSpot
    Spot <|-- LargeSpot
    Spot <|-- HandicappedSpot
    
    class ParkingLot {
        -floors: List~ParkingFloor~
        -activeTickets: Map
        +parkVehicle(vehicle): Ticket
        +exitVehicle(ticketId)
    }
    
    class ParkingFloor {
        -spots: Map~SpotType, List~
        +getAvailableSpots()
    }
    
    class Ticket {
        -ticketId: String
        -entryTime: DateTime
        +printReceipt()
    }
    
    ParkingLot --> ParkingFloor
    ParkingFloor --> Spot
    Ticket --> Vehicle
    Ticket --> Spot
```

---

## 🔷 SOLID Principles

### **S - Single Responsibility**
| Class | Responsibility |
|-------|---------------|
| `Vehicle` | Store vehicle data only |
| `Spot` | Manage spot state only |
| `TicketService` | Generate tickets, calculate fees |
| `ParkingLot` | Coordinate parking operations |

### **O - Open/Closed**
```java
// Add new types WITHOUT modifying existing code
class ElectricCar extends Vehicle { }
```

### **L - Liskov Substitution**
```java
Vehicle v = new Car("ABC-123", false);
lot.parkVehicle(v);  // Works with any Vehicle subclass
```

### **I - Interface Segregation**
```java
interface IParkable { VehicleType getVehicleType(); }
interface ITicketService { Ticket generateTicket(); }
interface IParkingStrategy { Spot findSpot(); }
```

### **D - Dependency Inversion**
```java
class ParkingLot {
    private IParkingStrategy strategy;  // Depends on abstraction
}
```

---

## 🎨 Design Patterns Used

| Pattern | Where | Why |
|---------|-------|-----|
| **Singleton** | `ParkingLot` | One parking lot instance |
| **Strategy** | `IParkingStrategy` | Swap parking algorithms |
| **Factory** | `TicketService` | Create tickets |
| **Template** | `Spot.canFitVehicle()` | Subclasses define behavior |

---

## ⚡ Concurrency Handling

### Problem: Race Condition
```
Thread 1: Spot 101 available? ✓
Thread 2: Spot 101 available? ✓  // Both see available!
Thread 1: Occupy Spot 101
Thread 2: Occupy Spot 101  // 💥 Conflict!
```

### Solution 1: Atomic tryOccupy()
```java
public boolean tryOccupy(Vehicle vehicle) {
    synchronized (lock) {
        if (!isAvailable) return false;
        this.isAvailable = false;
        return true;
    }
}
```

### Solution 2: ReentrantLock
```java
private final ReentrantLock parkingLock = new ReentrantLock();

public Ticket parkVehicle(Vehicle vehicle) {
    parkingLock.lock();
    try {
        return findAndOccupySpot(vehicle);
    } finally {
        parkingLock.unlock();
    }
}
```

### Solution 3: Thread-Safe Collections
```java
List<ParkingFloor> floors = new CopyOnWriteArrayList<>();
Map<String, Ticket> tickets = new ConcurrentHashMap<>();
```

---

## 💡 Interview Q&A

> **Q: Why abstract class for Vehicle?**  
> A: Share common fields, enforce structure, allow extension.

> **Q: How to add new vehicle type?**  
> A: Create subclass. No changes to existing code (Open/Closed).

> **Q: Why Strategy pattern?**  
> A: Swap algorithms (nearest, cheapest, EV priority) without changing ParkingLot.

---

## 📊 Complexity

| Operation | Time | Space |
|-----------|------|-------|
| Park | O(F × S) | O(1) |
| Exit | O(1) | O(1) |

*F = floors, S = spots per floor*

---

## 🔧 Code Implementation:

```java
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.time.LocalDateTime;

// ============================================
// ENUMS - Define constants for vehicle and spot types
// ============================================

enum VehicleType {
    BIKE,
    CAR,
    TRUCK
}

enum SpotType {
    COMPACT,      // For bikes
    REGULAR,      // For cars
    LARGE,        // For trucks
    HANDICAPPED   // Priority spots
}

enum TicketStatus {
    ACTIVE,
    PAID
}

// ============================================
// INTERFACES - Dependency Inversion Principle (D)
// Depend on abstractions, not concrete implementations
// ============================================

// Interface Segregation Principle (I) - Small, focused interfaces
interface IParkable {
    VehicleType getVehicleType();
    String getLicensePlate();
}

interface ISpot {
    int getSpotId();
    SpotType getSpotType();
    boolean isAvailable();
    void occupy(Vehicle vehicle);
    void vacate();
}

interface ITicketService {
    Ticket generateTicket(Vehicle vehicle, Spot spot);
    double calculateFee(Ticket ticket);
    void processPayment(Ticket ticket);
}

interface IParkingStrategy {
    Spot findSpot(VehicleType vehicleType, boolean isHandicapped, ParkingFloor floor);
}

// ============================================
// VEHICLE CLASSES - Liskov Substitution Principle (L)
// Subclasses can be used wherever parent class is expected
// ============================================

// Single Responsibility Principle (S) - Vehicle only represents vehicle data
abstract class Vehicle implements IParkable {
    private String licensePlate;
    private VehicleType vehicleType;
    private boolean isHandicapped;

    public Vehicle(String licensePlate, VehicleType vehicleType, boolean isHandicapped) {
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.isHandicapped = isHandicapped;
    }

    @Override
    public VehicleType getVehicleType() {
        return vehicleType;
    }

    @Override
    public String getLicensePlate() {
        return licensePlate;
    }

    public boolean isHandicapped() {
        return isHandicapped;
    }
}

// Open/Closed Principle (O) - Open for extension, closed for modification
// We can add new vehicle types without modifying existing code
class Bike extends Vehicle {
    public Bike(String licensePlate, boolean isHandicapped) {
        super(licensePlate, VehicleType.BIKE, isHandicapped);
    }
}

class Car extends Vehicle {
    public Car(String licensePlate, boolean isHandicapped) {
        super(licensePlate, VehicleType.CAR, isHandicapped);
    }
}

class Truck extends Vehicle {
    public Truck(String licensePlate, boolean isHandicapped) {
        super(licensePlate, VehicleType.TRUCK, isHandicapped);
    }
}

// ============================================
// SPOT CLASSES - Single Responsibility Principle (S)
// Spot only manages its own state
// ============================================

abstract class Spot implements ISpot {
    private int spotId;
    private SpotType spotType;
    private volatile boolean isAvailable;  // volatile for visibility across threads
    private Vehicle parkedVehicle;
    private final Object lock = new Object();  // Lock object for synchronization

    public Spot(int spotId, SpotType spotType) {
        this.spotId = spotId;
        this.spotType = spotType;
        this.isAvailable = true;
    }

    @Override
    public int getSpotId() {
        return spotId;
    }

    @Override
    public SpotType getSpotType() {
        return spotType;
    }

    @Override
    public boolean isAvailable() {
        return isAvailable;
    }

    // Thread-safe: Try to occupy the spot atomically
    // Returns true if successfully occupied, false if already taken
    public boolean tryOccupy(Vehicle vehicle) {
        synchronized (lock) {
            if (!isAvailable) {
                return false;  // Spot already taken by another thread
            }
            this.parkedVehicle = vehicle;
            this.isAvailable = false;
            return true;
        }
    }

    @Override
    public void occupy(Vehicle vehicle) {
        synchronized (lock) {
            this.parkedVehicle = vehicle;
            this.isAvailable = false;
        }
    }

    @Override
    public void vacate() {
        synchronized (lock) {
            this.parkedVehicle = null;
            this.isAvailable = true;
        }
    }

    public Vehicle getParkedVehicle() {
        return parkedVehicle;
    }

    // Template Method - each spot type defines what vehicles it can fit
    public abstract boolean canFitVehicle(VehicleType vehicleType);
}

// Open/Closed Principle (O) - New spot types can be added easily
class CompactSpot extends Spot {
    public CompactSpot(int spotId) {
        super(spotId, SpotType.COMPACT);
    }

    @Override
    public boolean canFitVehicle(VehicleType vehicleType) {
        return vehicleType == VehicleType.BIKE;
    }
}

class RegularSpot extends Spot {
    public RegularSpot(int spotId) {
        super(spotId, SpotType.REGULAR);
    }

    @Override
    public boolean canFitVehicle(VehicleType vehicleType) {
        return vehicleType == VehicleType.BIKE || vehicleType == VehicleType.CAR;
    }
}

class LargeSpot extends Spot {
    public LargeSpot(int spotId) {
        super(spotId, SpotType.LARGE);
    }

    @Override
    public boolean canFitVehicle(VehicleType vehicleType) {
        return true; // Large spots can fit any vehicle
    }
}

class HandicappedSpot extends Spot {
    public HandicappedSpot(int spotId) {
        super(spotId, SpotType.HANDICAPPED);
    }

    @Override
    public boolean canFitVehicle(VehicleType vehicleType) {
        return true; // Handicapped spots can fit any vehicle
    }
}

// ============================================
// TICKET CLASS - Single Responsibility Principle (S)
// Ticket only holds ticket data
// ============================================

class Ticket {
    private String ticketId;
    private Vehicle vehicle;
    private Spot spot;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private TicketStatus status;
    private double fee;

    public Ticket(String ticketId, Vehicle vehicle, Spot spot) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.spot = spot;
        this.entryTime = LocalDateTime.now();
        this.status = TicketStatus.ACTIVE;
    }

    // Getters
    public String getTicketId() { return ticketId; }
    public Vehicle getVehicle() { return vehicle; }
    public Spot getSpot() { return spot; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public TicketStatus getStatus() { return status; }
    public double getFee() { return fee; }

    // Setters
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }
    public void setStatus(TicketStatus status) { this.status = status; }
    public void setFee(double fee) { this.fee = fee; }

    public void printReceipt() {
        System.out.println("========== PARKING RECEIPT ==========");
        System.out.println("Ticket ID: " + ticketId);
        System.out.println("Vehicle: " + vehicle.getLicensePlate());
        System.out.println("Spot: " + spot.getSpotId() + " (" + spot.getSpotType() + ")");
        System.out.println("Entry: " + entryTime);
        System.out.println("Exit: " + exitTime);
        System.out.println("Fee: $" + fee);
        System.out.println("=====================================");
    }
}

// ============================================
// TICKET SERVICE - Single Responsibility Principle (S)
// Handles only ticket-related operations
// ============================================

class TicketService implements ITicketService {
    private static int ticketCounter = 0;
    private Map<String, Double> hourlyRates;

    public TicketService() {
        hourlyRates = new HashMap<>();
        hourlyRates.put("BIKE", 1.0);
        hourlyRates.put("CAR", 2.0);
        hourlyRates.put("TRUCK", 3.0);
    }

    @Override
    public Ticket generateTicket(Vehicle vehicle, Spot spot) {
        String ticketId = "TKT-" + (++ticketCounter);
        return new Ticket(ticketId, vehicle, spot);
    }

    @Override
    public double calculateFee(Ticket ticket) {
        ticket.setExitTime(LocalDateTime.now());
        
        // Calculate hours parked (minimum 1 hour)
        long hours = java.time.Duration.between(
            ticket.getEntryTime(), 
            ticket.getExitTime()
        ).toHours();
        hours = Math.max(1, hours);

        double rate = hourlyRates.getOrDefault(
            ticket.getVehicle().getVehicleType().toString(), 
            2.0
        );
        
        return hours * rate;
    }

    @Override
    public void processPayment(Ticket ticket) {
        double fee = calculateFee(ticket);
        ticket.setFee(fee);
        ticket.setStatus(TicketStatus.PAID);
    }
}

// ============================================
// PARKING STRATEGY - Strategy Pattern + Open/Closed (O)
// Different strategies can be plugged in
// ============================================

class DefaultParkingStrategy implements IParkingStrategy {
    
    @Override
    public Spot findSpot(VehicleType vehicleType, boolean isHandicapped, ParkingFloor floor) {
        // Priority 1: Handicapped spots for handicapped vehicles
        if (isHandicapped) {
            for (Spot spot : floor.getSpotsByType(SpotType.HANDICAPPED)) {
                if (spot.isAvailable() && spot.canFitVehicle(vehicleType)) {
                    return spot;
                }
            }
        }

        // Priority 2: Find appropriate spot based on vehicle type
        List<SpotType> preferredSpots = getPreferredSpots(vehicleType);
        
        for (SpotType spotType : preferredSpots) {
            for (Spot spot : floor.getSpotsByType(spotType)) {
                if (spot.isAvailable() && spot.canFitVehicle(vehicleType)) {
                    return spot;
                }
            }
        }

        return null; // No spot available
    }

    private List<SpotType> getPreferredSpots(VehicleType vehicleType) {
        switch (vehicleType) {
            case BIKE:
                return Arrays.asList(SpotType.COMPACT, SpotType.REGULAR, SpotType.LARGE);
            case CAR:
                return Arrays.asList(SpotType.REGULAR, SpotType.LARGE);
            case TRUCK:
                return Arrays.asList(SpotType.LARGE);
            default:
                return Arrays.asList(SpotType.REGULAR);
        }
    }
}

// ============================================
// PARKING FLOOR - Manages spots on one floor
// ============================================

class ParkingFloor {
    private int floorNumber;
    private Map<SpotType, List<Spot>> spotsByType;

    public ParkingFloor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.spotsByType = new HashMap<>();
        for (SpotType type : SpotType.values()) {
            spotsByType.put(type, new ArrayList<>());
        }
    }

    public void addSpot(Spot spot) {
        spotsByType.get(spot.getSpotType()).add(spot);
    }

    public List<Spot> getSpotsByType(SpotType spotType) {
        return spotsByType.getOrDefault(spotType, new ArrayList<>());
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public int getAvailableSpots() {
        int count = 0;
        for (List<Spot> spots : spotsByType.values()) {
            for (Spot spot : spots) {
                if (spot.isAvailable()) count++;
            }
        }
        return count;
    }
}

// ============================================
// PARKING LOT - Main Facade Class
// Single Responsibility: Coordinates all parking operations
// ============================================

class ParkingLot {
    private static volatile ParkingLot instance; // volatile for thread-safe singleton
    private static final Object singletonLock = new Object();
    
    private String name;
    private List<ParkingFloor> floors;
    private Map<String, Ticket> activeTickets; // ticketId -> Ticket
    private ITicketService ticketService;
    private IParkingStrategy parkingStrategy;
    
    // ReentrantLock for thread-safe parking operations
    private final ReentrantLock parkingLock = new ReentrantLock();

    // Private constructor for Singleton
    private ParkingLot(String name) {
        this.name = name;
        this.floors = new CopyOnWriteArrayList<>();  // Thread-safe list
        this.activeTickets = new ConcurrentHashMap<>();  // Thread-safe map
        this.ticketService = new TicketService();
        this.parkingStrategy = new DefaultParkingStrategy();
    }

    // Thread-Safe Singleton with Double-Checked Locking
    public static ParkingLot getInstance(String name) {
        if (instance == null) {
            synchronized (singletonLock) {
                if (instance == null) {
                    instance = new ParkingLot(name);
                }
            }
        }
        return instance;
    }

    // Dependency Inversion (D) - Allow injecting different strategies
    public void setParkingStrategy(IParkingStrategy strategy) {
        this.parkingStrategy = strategy;
    }

    public void addFloor(ParkingFloor floor) {
        floors.add(floor);
    }

    // ========== ENTRY OPERATIONS (THREAD-SAFE) ==========
    
    /**
     * Thread-safe parking method.
     * Uses ReentrantLock to ensure only one vehicle can attempt parking at a time.
     * If 2 vehicles come at same time, one will wait for the other to finish.
     */
    public Ticket parkVehicle(Vehicle vehicle) {
        parkingLock.lock();  // Acquire lock - other threads will wait here
        try {
            Spot availableSpot = findAndOccupySpot(vehicle);
            
            if (availableSpot == null) {
                System.out.println("[Thread: " + Thread.currentThread().getName() + 
                    "] Sorry, no parking spot available for " + vehicle.getVehicleType());
                return null;
            }

            // Generate ticket
            Ticket ticket = ticketService.generateTicket(vehicle, availableSpot);
            activeTickets.put(ticket.getTicketId(), ticket);

            System.out.println("[Thread: " + Thread.currentThread().getName() + 
                "] Vehicle " + vehicle.getLicensePlate() + 
                " parked at Spot " + availableSpot.getSpotId());
            System.out.println("Ticket ID: " + ticket.getTicketId());

            return ticket;
        } finally {
            parkingLock.unlock();  // Always release lock, even if exception occurs
        }
    }

    /**
     * Find and atomically occupy a spot.
     * Uses tryOccupy() to prevent race conditions.
     */
    private Spot findAndOccupySpot(Vehicle vehicle) {
        for (ParkingFloor floor : floors) {
            Spot spot = findAndTryOccupy(vehicle, floor);
            if (spot != null) return spot;
        }
        return null;
    }

    private Spot findAndTryOccupy(Vehicle vehicle, ParkingFloor floor) {
        // Get preferred spot types for this vehicle
        List<SpotType> preferredSpots = getPreferredSpots(vehicle);
        
        for (SpotType spotType : preferredSpots) {
            for (Spot spot : floor.getSpotsByType(spotType)) {
                // tryOccupy is atomic - only one thread can succeed
                if (spot.isAvailable() && spot.canFitVehicle(vehicle.getVehicleType())) {
                    if (spot.tryOccupy(vehicle)) {
                        return spot;  // Successfully occupied!
                    }
                    // If tryOccupy returns false, another thread got it first
                }
            }
        }
        return null;
    }

    private List<SpotType> getPreferredSpots(Vehicle vehicle) {
        if (vehicle.isHandicapped()) {
            return Arrays.asList(SpotType.HANDICAPPED, SpotType.REGULAR, SpotType.LARGE);
        }
        switch (vehicle.getVehicleType()) {
            case BIKE:
                return Arrays.asList(SpotType.COMPACT, SpotType.REGULAR, SpotType.LARGE);
            case CAR:
                return Arrays.asList(SpotType.REGULAR, SpotType.LARGE);
            case TRUCK:
                return Arrays.asList(SpotType.LARGE);
            default:
                return Arrays.asList(SpotType.REGULAR);
        }
    }

    // ========== EXIT OPERATIONS ==========

    public void exitVehicle(String ticketId) {
        Ticket ticket = activeTickets.get(ticketId);
        
        if (ticket == null) {
            System.out.println("Invalid ticket ID!");
            return;
        }

        // Process payment
        ticketService.processPayment(ticket);

        // Vacate the spot
        ticket.getSpot().vacate();

        // Remove from active tickets
        activeTickets.remove(ticketId);

        // Print receipt
        ticket.printReceipt();
    }

    // ========== STATUS OPERATIONS ==========

    public void displayAvailability() {
        System.out.println("\n===== " + name + " - Availability =====");
        for (ParkingFloor floor : floors) {
            System.out.println("Floor " + floor.getFloorNumber() + 
                ": " + floor.getAvailableSpots() + " spots available");
        }
        System.out.println("==========================================\n");
    }
}

// ============================================
// MAIN - Demo the Parking Lot System
// ============================================

public class ParkingLot {
    public static void main(String[] args) {
        // Create parking lot (Singleton)
        ParkingLot lot = ParkingLot.getInstance("Downtown Parking");

        // Create and add floors with spots
        ParkingFloor floor1 = new ParkingFloor(1);
        floor1.addSpot(new CompactSpot(101));
        floor1.addSpot(new CompactSpot(102));
        floor1.addSpot(new RegularSpot(103));
        floor1.addSpot(new RegularSpot(104));
        floor1.addSpot(new LargeSpot(105));
        floor1.addSpot(new HandicappedSpot(106));

        ParkingFloor floor2 = new ParkingFloor(2);
        floor2.addSpot(new RegularSpot(201));
        floor2.addSpot(new RegularSpot(202));
        floor2.addSpot(new LargeSpot(203));

        lot.addFloor(floor1);
        lot.addFloor(floor2);

        // Display initial availability
        lot.displayAvailability();

        // Park some vehicles
        System.out.println("--- Parking Vehicles ---");
        
        Vehicle bike = new Bike("BIKE-001", false);
        Ticket ticket1 = lot.parkVehicle(bike);

        Vehicle car = new Car("CAR-001", false);
        Ticket ticket2 = lot.parkVehicle(car);

        Vehicle handicappedCar = new Car("CAR-002", true);
        Ticket ticket3 = lot.parkVehicle(handicappedCar);

        Vehicle truck = new Truck("TRUCK-001", false);
        Ticket ticket4 = lot.parkVehicle(truck);

        // Display availability after parking
        lot.displayAvailability();

        // Exit a vehicle
        System.out.println("--- Vehicle Exiting ---");
        if (ticket2 != null) {
            lot.exitVehicle(ticket2.getTicketId());
        }

        // Display final availability
        lot.displayAvailability();
    }
}
```

---

## ✅ Interview Checklist

- [ ] Clarify requirements
- [ ] Draw class diagram
- [ ] Mention SOLID principles
- [ ] Explain design patterns
- [ ] Discuss concurrency
- [ ] Analyze complexity

---

> 💡 **Pro Tip**: Start simple, iterate. Show you can handle follow-ups!
