// package src.HLDProblems;
// // Requirements
// // 1. Multi floor parkig lot
// // 2. Should have multiple entry & exit points
// // 3. Multiple kinds of vehicle are allowed - 2 wheeler, 4 wheeler, electric vehicles
// // 4. There should be a max limit, if vehicles came after max limit is reached, then it should display like - "Out of Space"
// // 5. Charge will be per hour basis - if 1st hr - x, 2nd & 3rd hr - y, for subsequent hr - z
// // 6. Will pay at the time of exit
// // 7. For electric vehicles, need to assign some slots & charge them with different price
// // 8. On each floor, there should be display which will display, slots empty
// // 9. Can pay ticket at the automated exit panel or to the parking attendant
// // 10. Can pay via both cash/credit cards

// // Actors: User, Parking attendant, Admin, System

// // Main classes of parking lot:

// import java.util.*;
// import java.util.concurrent.locks.ReentrantLock;

// public enum VehicleType {
//     CAR, TRUCK, ELECTRIC, VAN, MOTORBIKE
// }

// public enum ParkingSpotType {
//     HANDICAPPED, COMPACT, LARGE, MOTORBIKE, ELECTRIC
// }

// public enum AccountStatus {
//     ACTIVE, BLOCKED, BANNED, COMPROMISED, ARCHIVED, UNKNOWN
// }

// public enum ParkingTicketStatus {
//     ACTIVE, PAID, LOST
// }

// class ParkingRate {}
// class EntrancePanel {}
// class ExitPanel {}

// public class Address {
//     private String streetAddress;
//     private String city;
//     private String state;
//     private String zipCode;
//     private String country;

//     public Address(String street, String city, String state, String zipCode, String country) {
//         this.streetAddress = street;
//         this.city = city;
//         this.state = state;
//         this.zipCode = zipCode;
//         this.country = country;
//     }
// }

// public class Person {
//     private String name;
//     private Address address;
//     private String email;
//     private String phone;

//     public Person(String name, Address address, String email, String phone) {
//         this.name = name;
//         this.address = address;
//         this.email = email;
//         this.phone = phone;
//     }
// }

// public class Account {
//     private String userName;
//     private String password;
//     private Person person;
//     private AccountStatus status;

//     public Account(String userName, String password, Person person, AccountStatus status) {
//         this.userName = userName;
//         this.password = password;
//         this.person = person;
//         this.status = status;
//     }

//     public void resetPassword() {
//         // Implementation goes here
//     }
// }

// public class Admin extends Account {
//     public Admin(String userName, String password, Person person, AccountStatus status) {
//         super(userName, password, person, status);
//     }

//     public void addParkingFloor(String floor) {}
//     public void addParkingSpot(String floorName, Object spot) {}
//     public void addParkingDisplayBoard(String floorName, Object displayBoard) {}
//     public void addCustomerInfoPanel(String floorName, Object infoPanel) {}
//     public void addEntrancePanel(Object entrancePanel) {}
//     public void addExitPanel(Object exitPanel) {}
// }

// public class ParkingAttendant extends Account {
//     public ParkingAttendant(String userName, String password, Person person, AccountStatus status) {
//         super(userName, password, person, status);
//     }

//     public void processTicket(String ticketNumber) {
//         // Implementation goes here
//     }
// }

// public abstract class ParkingSpot {
//     private String number;
//     private boolean free;
//     private final ParkingSpotType type;
//     private Vehicle vehicle;

//     public ParkingSpot(ParkingSpotType type, String number) {
//         this.type = type;
//         this.number = number;
//         this.free = true;
//     }

//     public boolean isFree() {
//         return free;
//     }

//     public void assignVehicle(Vehicle vehicle) {
//         this.vehicle = vehicle;
//         this.free = false;
//     }

//     public void removeVehicle() {
//         this.vehicle = null;
//         this.free = true;
//     }

//     public ParkingSpotType getType() {
//         return type;
//     }

//     public String getNumber() {
//         return number;
//     }

//     public Vehicle getVehicle() {
//         return vehicle;
//     }
// }

// public class HandicappedSpot extends ParkingSpot {
//     public HandicappedSpot(String number) {
//         super(ParkingSpotType.HANDICAPPED, number);
//     }
// }

// public class CompactSpot extends ParkingSpot {
//     public CompactSpot(String number) {
//         super(ParkingSpotType.COMPACT, number);
//     }
// }

// public class LargeSpot extends ParkingSpot {
//     public LargeSpot(String number) {
//         super(ParkingSpotType.LARGE, number);
//     }
// }

// public class MotorbikeSpot extends ParkingSpot {
//     public MotorbikeSpot(String number) {
//         super(ParkingSpotType.MOTORBIKE, number);
//     }
// }

// public class ElectricSpot extends ParkingSpot {
//     public ElectricSpot(String number) {
//         super(ParkingSpotType.ELECTRIC, number);
//     }
// }

// public class ParkingFloor {
//     private String name;
//     private Map<String, ParkingSpot> handicappedSpots = new HashMap<>();
//     private Map<String, ParkingSpot> compactSpots = new HashMap<>();
//     private Map<String, ParkingSpot> largeSpots = new HashMap<>();
//     private Map<String, ParkingSpot> motorbikeSpots = new HashMap<>();
//     private Map<String, ParkingSpot> electricSpots = new HashMap<>();
//     private ParkingDisplayBoard displayBoard = new ParkingDisplayBoard();

//     public ParkingFloor(String name) {
//         this.name = name;
//     }

//     public void addParkingSpot(ParkingSpot spot) {
//         switch (spot.getType()) {
//             case HANDICAPPED -> handicappedSpots.put(spot.getNumber(), spot);
//             case COMPACT -> compactSpots.put(spot.getNumber(), spot);
//             case LARGE -> largeSpots.put(spot.getNumber(), spot);
//             case MOTORBIKE -> motorbikeSpots.put(spot.getNumber(), spot);
//             case ELECTRIC -> electricSpots.put(spot.getNumber(), spot);
//             default -> System.out.println("Wrong parking spot type");
//         }
//     }

//     public void assignVehicleToSpot(Vehicle vehicle, ParkingSpot spot) {
//         spot.assignVehicle(vehicle);
//         switch (spot.getType()) {
//             case HANDICAPPED -> updateDisplayBoardForHandicapped(spot);
//             case COMPACT -> updateDisplayBoardForCompact(spot);
//             case LARGE -> updateDisplayBoardForLarge(spot);
//             case MOTORBIKE -> updateDisplayBoardForMotorbike(spot);
//             case ELECTRIC -> updateDisplayBoardForElectric(spot);
//             default -> System.out.println("Wrong parking spot type!");
//         }
//     }

//     private void updateDisplayBoardForHandicapped(ParkingSpot spot) {
//         if (displayBoard.getHandicappedFreeSpot().getNumber().equals(spot.getNumber())) {
//             for (ParkingSpot s : handicappedSpots.values()) {
//                 if (s.isFree()) {
//                     displayBoard.setHandicappedFreeSpot(s);
//                     break;
//                 }
//             }
//             displayBoard.showEmptySpotNumber();
//         }
//     }

//     private void updateDisplayBoardForCompact(ParkingSpot spot) {
//         if (displayBoard.getCompactFreeSpot().getNumber().equals(spot.getNumber())) {
//             for (ParkingSpot s : compactSpots.values()) {
//                 if (s.isFree()) {
//                     displayBoard.setCompactFreeSpot(s);
//                     break;
//                 }
//             }
//             displayBoard.showEmptySpotNumber();
//         }
//     }

//     private void updateDisplayBoardForLarge(ParkingSpot spot) {}
//     private void updateDisplayBoardForMotorbike(ParkingSpot spot) {}
//     private void updateDisplayBoardForElectric(ParkingSpot spot) {}
// }

// class ParkingDisplayBoard {
//     private String id;
//     private ParkingSpot handicappedFreeSpot;
//     private ParkingSpot compactFreeSpot;
//     private ParkingSpot largeFreeSpot;
//     private ParkingSpot motorbikeFreeSpot;
//     private ParkingSpot electricFreeSpot;

//     public void showEmptySpotNumber() {
//         StringBuilder message = new StringBuilder();

//         message.append(handicappedFreeSpot.isFree() ? "Free Handicapped: " + handicappedFreeSpot.getNumber() : "Handicapped is full").append("\n");
//         message.append(compactFreeSpot.isFree() ? "Free Compact: " + compactFreeSpot.getNumber() : "Compact is full").append("\n");
//         message.append(largeFreeSpot.isFree() ? "Free Large: " + largeFreeSpot.getNumber() : "Large is full").append("\n");
//         message.append(motorbikeFreeSpot.isFree() ? "Free Motorbike: " + motorbikeFreeSpot.getNumber() : "Motorbike is full").append("\n");
//         message.append(electricFreeSpot.isFree() ? "Free Electric: " + electricFreeSpot.getNumber() : "Electric is full");

//         System.out.println(message);
//     }

//     public ParkingSpot getHandicappedFreeSpot() { return handicappedFreeSpot; }
//     public void setHandicappedFreeSpot(ParkingSpot spot) { this.handicappedFreeSpot = spot; }
//     public ParkingSpot getCompactFreeSpot() { return compactFreeSpot; }
//     public void setCompactFreeSpot(ParkingSpot spot) { this.compactFreeSpot = spot; }
// }

// class ParkingLot {
//     private static ParkingLot instance;

//     private String name;
//     private String address;
//     private ParkingRate parkingRate = new ParkingRate();

//     private int compactSpotCount, largeSpotCount, motorbikeSpotCount, electricSpotCount;
//     private int maxCompactCount, maxLargeCount, maxMotorbikeCount, maxElectricCount;

//     private Map<String, EntrancePanel> entrancePanels = new HashMap<>();
//     private Map<String, ExitPanel> exitPanels = new HashMap<>();
//     private Map<String, ParkingFloor> parkingFloors = new HashMap<>();
//     private Map<String, ParkingTicket> activeTickets = new HashMap<>();

//     private final ReentrantLock lock = new ReentrantLock();

//     private ParkingLot(String name, String address) {
//         this.name = name;
//         this.address = address;
//     }

//     public static ParkingLot getInstance(String name, String address) {
//         if (instance == null) {
//             instance = new ParkingLot(name, address);
//         }
//         return instance;
//     }

//     public ParkingTicket getNewParkingTicket(Vehicle vehicle) throws Exception {
//         if (isFull(vehicle.getType())) {
//             throw new Exception("Parking full!");
//         }

//         lock.lock();
//         try {
//             ParkingTicket ticket = new ParkingTicket();
//             vehicle.assignTicket(ticket);
//             ticket.saveInDB();
//             incrementSpotCount(vehicle.getType());
//             activeTickets.put(ticket.getTicketNumber(), ticket);
//             return ticket;
//         } finally {
//             lock.unlock();
//         }
//     }

//     public boolean isFull(VehicleType type) {
//         return switch (type) {
//             case TRUCK, VAN -> largeSpotCount >= maxLargeCount;
//             case MOTORBIKE -> motorbikeSpotCount >= maxMotorbikeCount;
//             case CAR -> (compactSpotCount + largeSpotCount) >= (maxCompactCount + maxLargeCount);
//             case ELECTRIC -> (compactSpotCount + largeSpotCount + electricSpotCount) >= (maxCompactCount + maxLargeCount + maxElectricCount);
//         };
//     }

//     private void incrementSpotCount(VehicleType type) {
//         switch (type) {
//             case TRUCK, VAN -> largeSpotCount++;
//             case MOTORBIKE -> motorbikeSpotCount++;
//             case CAR -> {
//                 if (compactSpotCount < maxCompactCount) compactSpotCount++;
//                 else largeSpotCount++;
//             }
//             case ELECTRIC -> {
//                 if (electricSpotCount < maxElectricCount) electricSpotCount++;
//                 else if (compactSpotCount < maxCompactCount) compactSpotCount++;
//                 else largeSpotCount++;
//             }
//         }
//     }

//     public boolean isFull() {
//         for (ParkingFloor floor : parkingFloors.values()) {
//             if (!floor.isFull()) return false;
//         }
//         return true;
//     }

//     public void addParkingFloor(ParkingFloor floor) {
//         // Store in DB logic
//     }

//     public void addEntrancePanel(EntrancePanel panel) {
//         // Store in DB logic
//     }

//     public void addExitPanel(ExitPanel panel) {
//         // Store in DB logic
//     }
// }

// // Placeholder classes for dependencies
// class ParkingSpot {
//     private String number;
//     private ParkingSpotType type;
//     public boolean isFree() { return true; }
//     public void assignVehicle(Vehicle v) {}
//     public String getNumber() { return number; }
//     public ParkingSpotType getType() { return type; }
// }

// class Vehicle {
//     public void assignTicket(ParkingTicket t) {}
//     public VehicleType getType() { return VehicleType.CAR; }
// }

// class ParkingTicket {
//     public void saveInDB() {}
//     public String getTicketNumber() { return UUID.randomUUID().toString(); }
// }





