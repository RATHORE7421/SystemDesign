# 🏥 Hospital Management System — Low Level Design (Interview Revision Guide)

> **Target Level**: SDE2 / SDE3 (Kotak Mahindra Bar Raiser)  
> **Time**: ~45 minutes | **Patterns**: Facade, SRP | **Key Topic**: Priority Queue, State Machine

---

## 📋 Phase 1: Requirements Gathering (3–5 min)

### Functional Requirements

| # | Requirement | Notes |
|---|-------------|-------|
| 1 | Reception registers patient with priority (Critical, High, Medium, Low) | Triage at entry |
| 2 | Patients wait in a **priority queue** (Critical served first) | Core data structure |
| 3 | Same priority → **FIFO** (first registered, first served) | Fair ordering |
| 4 | Initial Consultation Doctor picks **next patient** from queue | Pull-based model |
| 5 | After consultation, patient is assigned to a **Specialist Doctor** | Routing decision |
| 6 | System maintains **treatment records** (diagnosis, treatment, doctors) | Audit trail |

### Non-Functional Requirements

| # | Requirement | Notes |
|---|-------------|-------|
| 1 | **Thread-safe**: Multiple receptionists adding patients, multiple doctors pulling | Concurrent access |
| 2 | **Extensible**: Easy to add new specializations, priority levels | OCP |
| 3 | **Clean separation** of concerns (SRP) | Each service does one thing |

### Out of Scope

- Appointment scheduling / pre-booking
- Billing and insurance
- Pharmacy / medicine dispensing
- Bed/ward management

---

## 🎤 Clarifying Questions to Ask the Interviewer

> These show structured thinking. Ask 3–4 before designing.

1. **"Can a patient's priority change while waiting?"**  
   → Yes, if condition worsens. Need priority upgrade support.  
   → ⚠️ PriorityBlockingQueue doesn't support re-ordering! Must remove & re-add.

2. **"Can multiple receptionists register patients concurrently?"**  
   → Yes → need thread-safe queue (`PriorityBlockingQueue`)

3. **"What if no consultation doctor is available?"**  
   → Patient stays in queue. Doctor picks when free.

4. **"What if no specialist of required type is available?"**  
   → Patient waits. Could add a per-specialization waiting list.

5. **"Can a patient see multiple specialists?"**  
   → Out of scope. One consultation → one specialist.

6. **"Is this pull-based (doctor picks patient) or push-based (system assigns patient to doctor)?"**  
   → Pull-based: doctor requests next patient when ready.

---

## ⚠️ Edge Cases & How to Handle

| # | Edge Case | How to Handle |
|---|-----------|---------------|
| 1 | No doctors available when patient arrives | Patient stays in queue until a doctor becomes free |
| 2 | Two CRITICAL patients at same time | FIFO within same priority (compare `registrationTime`) |
| 3 | Patient priority upgrade (LOW → CRITICAL) | Remove from queue → change priority → re-add to queue. O(N) removal |
| 4 | Specialist not available for diagnosis | Return failure. Could maintain per-specialization waiting list |
| 5 | Doctor forgets to mark available after consultation | Add timeout-based auto-release (ScheduledExecutorService) |
| 6 | Same patient registers twice | Check by unique ID (Aadhaar/phone) before registering |
| 7 | Patient leaves without treatment | Add `CANCELLED` status, remove from queue, log in records |
| 8 | Two doctors pull from queue at exact same time | `PriorityBlockingQueue.poll()` is atomic — only ONE gets the patient |

---

## 🏗️ Phase 2: Core Entities

### Entity Identification (nouns → classes, verbs → methods)

| Entity | Type | Responsibility |
|--------|------|----------------|
| `Patient` | Class | Holds patient data (name, age, priority, status) |
| `Doctor` | Class | Holds doctor info + availability flag |
| `TreatmentRecord` | Class | Links patient ↔ doctors ↔ diagnosis ↔ treatment |
| `PatientQueue` | Class | Priority queue wrapper — orders patients by priority + FIFO |
| `ReceptionService` | Class | Registers patients, assigns priority, adds to queue |
| `DoctorService` | Class | Manages doctor pool, finds available consultation/specialist docs |
| `TreatmentRecordService` | Class | Creates & queries treatment records |
| `HospitalManagementSystem` | Class (Facade) | **Orchestrator** — single entry point for all operations |

### Enums

```java
enum Priority { CRITICAL(1), HIGH(2), MEDIUM(3), LOW(4) }
enum Specialization { CARDIOLOGY, NEUROLOGY, ORTHOPEDICS, GENERAL, DERMATOLOGY, ENT }
enum PatientStatus { REGISTERED, WAITING_IN_QUEUE, IN_CONSULTATION, ASSIGNED_SPECIALIST, IN_TREATMENT, DISCHARGED }
```

### Patient State Machine

```
REGISTERED → WAITING_IN_QUEUE → IN_CONSULTATION → ASSIGNED_SPECIALIST → IN_TREATMENT → DISCHARGED
                                                                                          │
                                                       CANCELLED ◄────────────────────────┘
                                                       (patient leaves at any point)
```

---

## 🎨 Design Patterns Used

| Pattern | Where | Why | Cross-Question |
|---------|-------|-----|----------------|
| **Facade** | `HospitalManagementSystem` | Single orchestrator hides complexity. Client calls `hospital.consultNextPatient()` — doesn't know about queue, doctor service, records | *"Why not let main() call each service directly?"* → Coupling. Facade gives a clean API. |
| **SRP** | Every service class | `ReceptionService` only registers. `DoctorService` only manages doctors. `TreatmentRecordService` only manages records. | *"Why not put registration inside HospitalSystem?"* → Violates SRP. If billing is added, ReceptionService handles it without touching DoctorService. |
| **State Pattern** (via enum) | `PatientStatus` | Clear, enforceable state transitions. Prevents invalid states (can't discharge a patient who's still waiting in queue). | *"Why enum and not boolean flags?"* → Multiple booleans (`isWaiting`, `isConsulted`, `isDischarged`) create impossible states. Enum enforces exactly one state. |

---

## 🔑 Key Design Decisions

### Why PriorityBlockingQueue?

| Alternative | Problem |
|-------------|---------|
| `ArrayList` + sort on every access | O(N log N) per poll — too expensive |
| `TreeMap<Priority, Queue<Patient>>` | Works but needs manual sync for thread safety |
| `LinkedList` (sorted insert) | O(N) insert |
| **`PriorityBlockingQueue`** ⭐ | O(log N) insert, O(log N) poll, thread-safe, battle-tested |

### The Comparator (Critical Detail)

```java
Comparator<Patient> comparator = (p1, p2) -> {
    // First: Compare by priority level (CRITICAL=1 < LOW=4)
    int priorityCompare = Integer.compare(
        p1.getPriority().getLevel(),
        p2.getPriority().getLevel()
    );
    if (priorityCompare != 0) return priorityCompare;
    
    // Second: Same priority → FIFO (earlier registration time first)
    return p1.getRegistrationTime().compareTo(p2.getRegistrationTime());
};
```

> 🎤 **Cross-question**: *"Why do you need the registration time comparison?"*  
> **Answer**: Without it, two CRITICAL patients have no defined order — the PQ could return either one first. Adding registration time ensures **fairness** — first-come-first-served within the same priority.

### Why Pull-Based (Doctor Picks) vs Push-Based (System Assigns)?

| Model | How it works | Trade-off |
|-------|-------------|-----------|
| **Pull-based** ⭐ | Doctor calls `getNextPatient()` when ready | Doctor controls pace. Simple. No wasted assignments. |
| **Push-based** | System assigns patient to doctor when available | Faster but risk: what if doctor is mid-break? Need acknowledgement + timeout. More complex. |

> Interview answer: *"Pull-based is simpler and sufficient for this scope. In a real hospital, push-based with a notification (Observer pattern) would be better — doctor gets notified when a high-priority patient arrives."*

---

## 💻 Code Structure (see HospitalSystem.java)

```
HospitalManagementSystem (Facade — orchestrates everything)
├── ReceptionService        → registerPatient(name, age, priority)
├── PatientQueue            → addPatient(), getNextPatient() [PriorityBlockingQueue]
├── DoctorService           → getAvailableConsultationDoctor(), getAvailableSpecialist()
└── TreatmentRecordService  → createRecord(), getRecordByPatientId()
```

### Flow

```
Step 1: reception.registerPatient("Priya", 30, CRITICAL)
            → Creates Patient, sets status = WAITING_IN_QUEUE
            → Adds to PriorityBlockingQueue

Step 2: hospital.consultNextPatient()
            → patientQueue.poll() → gets highest priority patient (Priya)
            → doctorService.getAvailableConsultationDoctor() → Dr. Sharma
            → Dr. Sharma marked busy, Priya status → IN_CONSULTATION
            → TreatmentRecord created

Step 3: hospital.assignSpecialist(record, CARDIOLOGY, "Chest pain")
            → doctorService.getAvailableSpecialist(CARDIOLOGY) → Dr. Gupta
            → Dr. Sharma released (available again)
            → Dr. Gupta marked busy, Priya status → ASSIGNED_SPECIALIST
            → Record updated with diagnosis + specialist

Step 4: hospital.startTreatment(record, "Angioplasty")
            → Priya status → IN_TREATMENT

Step 5: hospital.dischargePatient(record)
            → Dr. Gupta released, Priya status → DISCHARGED
```

---

## ❓ Interview Cross-Questions & Answers

### Design Questions

> **Q: "Why PriorityBlockingQueue and not a TreeMap or sorted list?"**  
> A: PBQ gives O(log N) insert + O(log N) poll. It's thread-safe out of the box — no external synchronization needed. TreeMap works but needs manual synchronization. Sorted list is O(N) insert.

> **Q: "What if the patient's condition worsens while waiting — can you change priority?"**  
> A: `PriorityBlockingQueue` doesn't support re-ordering in place. You must: (1) Remove the patient from the queue — O(N). (2) Update their priority. (3) Re-add them — O(log N). If priority changes are frequent, consider a custom **indexed priority queue** with a decrease-key operation.

> **Q: "How would you handle concurrent access — 2 doctors pulling at the same time?"**  
> A: `PriorityBlockingQueue.poll()` is **atomic** — only ONE doctor gets the patient. The other gets the next one. Same principle as `AtomicBoolean.compareAndSet()` in the parking lot problem. No race condition.

> **Q: "What if you need VIP patients who skip the queue?"**  
> A: Two options: (1) Add a `VIP` priority level above `CRITICAL` (simple). (2) Use a **separate VIP queue** that doctors check first — `VIPQueue.poll()`, if null, then `regularQueue.poll()`. Option 2 is cleaner if VIP logic is very different.

> **Q: "Why not use Observer pattern to notify doctors when a critical patient arrives?"**  
> A: Great extension! `PatientQueue` could be a Subject. When a `CRITICAL` patient is added, it notifies all available doctors. The first doctor to respond (accept) gets the patient. This is push-based and more realistic.

### Scalability Questions

> **Q: "How would you scale this to a hospital chain?"**  
> A: Each branch gets its own `HospitalManagementSystem` instance. Add a `HospitalRegistry` to route patients to the nearest hospital with availability. Remove any singleton patterns.

> **Q: "How would you persist this data?"**  
> A: Replace in-memory collections with a database. `Patient`, `Doctor`, `TreatmentRecord` → DB tables. Queue → could be a **Redis sorted set** (score = priority × 1000000 + timestamp) for O(log N) operations with persistence.

> **Q: "What if the system crashes mid-consultation?"**  
> A: In production, wrap `consultNextPatient()` in a database transaction: BEGIN → poll queue + create record → COMMIT. If crash occurs, transaction rolls back. Patient stays in queue.

### Comparison to Parking Lot

| Aspect | Parking Lot | Hospital |
|--------|-------------|----------|
| Queue type | Linear scan / TreeMap + Queue | **PriorityBlockingQueue** |
| Concurrency | AtomicBoolean on slots | PBQ.poll() is atomic |
| Assignment | Strategy Pattern (LowestFloorFirst) | Pull-based (doctor picks next) |
| State machine | Slot: Available/Occupied | Patient: 6 states |
| Orchestrator | ParkingLot (Singleton) | HospitalSystem (Facade, NOT singleton) |

---

## ✅ Interview Checklist

- [x] Clarify requirements (3 min) — priority types, pull vs push, concurrency
- [x] Identify core entities — Patient, Doctor, TreatmentRecord, Queue, Services
- [x] Key data structure — PriorityBlockingQueue with Comparator (priority + FIFO)
- [x] Design patterns — Facade, SRP, State (via enum)
- [x] Code the core — `registerPatient()`, `consultNextPatient()`, `assignSpecialist()`
- [x] Edge cases — priority upgrade, no doctors available, concurrent access
- [x] Cross-questions — PBQ vs TreeMap, Observer extension, persistence, scaling
