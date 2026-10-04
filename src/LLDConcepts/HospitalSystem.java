package src.LLDConcepts;

import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;

// ==================== ENUMS ====================

enum Priority {
    CRITICAL(1),   // Highest priority (lowest number = highest priority in PQ)
    HIGH(2),
    MEDIUM(3),
    LOW(4);

    private final int level;

    Priority(int level) { this.level = level; }
    public int getLevel() { return level; }
}

enum Specialization {
    CARDIOLOGY, NEUROLOGY, ORTHOPEDICS, GENERAL, DERMATOLOGY, ENT, PEDIATRICS
}

enum PatientStatus {
    REGISTERED,          // Just registered at reception
    WAITING_IN_QUEUE,    // In priority queue, waiting for initial consultation
    IN_CONSULTATION,     // Being seen by initial consultation doctor
    ASSIGNED_SPECIALIST, // Assigned to specialist, waiting
    IN_TREATMENT,        // Being treated by specialist
    DISCHARGED           // Treatment complete
}

// ==================== CORE ENTITIES ====================

// --- Patient ---
class Patient {
    private final String patientId;
    private final String name;
    private final int age;
    private Priority priority;
    private PatientStatus status;
    private final LocalDateTime registrationTime;

    public Patient(String patientId, String name, int age, Priority priority) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.priority = priority;
        this.status = PatientStatus.REGISTERED;
        this.registrationTime = LocalDateTime.now();
    }

    // Getters
    public String getPatientId() { return patientId; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public Priority getPriority() { return priority; }
    public PatientStatus getStatus() { return status; }
    public LocalDateTime getRegistrationTime() { return registrationTime; }

    // Setters
    public void setStatus(PatientStatus status) { this.status = status; }
    public void setPriority(Priority priority) { this.priority = priority; }

    @Override
    public String toString() {
        return "[" + patientId + "] " + name + " (Priority: " + priority + ", Status: " + status + ")";
    }
}

// --- Doctor ---
class Doctor {
    private final String doctorId;
    private final String name;
    private final Specialization specialization;
    private boolean isAvailable;

    public Doctor(String doctorId, String name, Specialization specialization) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialization = specialization;
        this.isAvailable = true;
    }

    public String getDoctorId() { return doctorId; }
    public String getName() { return name; }
    public Specialization getSpecialization() { return specialization; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }

    @Override
    public String toString() {
        return "Dr. " + name + " (" + specialization + ")" + (isAvailable ? " ✅" : " 🔴 Busy");
    }
}

// --- Treatment Record (SRP — holds treatment data only) ---
class TreatmentRecord {
    private final String recordId;
    private final Patient patient;
    private final Doctor consultationDoctor;   // Initial doctor
    private Doctor specialistDoctor;           // Assigned specialist
    private String diagnosis;
    private String treatment;
    private final LocalDateTime consultationTime;
    private LocalDateTime treatmentStartTime;
    private LocalDateTime treatmentEndTime;

    public TreatmentRecord(String recordId, Patient patient, Doctor consultationDoctor) {
        this.recordId = recordId;
        this.patient = patient;
        this.consultationDoctor = consultationDoctor;
        this.consultationTime = LocalDateTime.now();
    }

    public void assignSpecialist(Doctor specialist, String diagnosis) {
        this.specialistDoctor = specialist;
        this.diagnosis = diagnosis;
    }

    public void startTreatment(String treatment) {
        this.treatment = treatment;
        this.treatmentStartTime = LocalDateTime.now();
    }

    public void endTreatment() {
        this.treatmentEndTime = LocalDateTime.now();
    }

    // Getters
    public String getRecordId() { return recordId; }
    public Patient getPatient() { return patient; }
    public Doctor getConsultationDoctor() { return consultationDoctor; }
    public Doctor getSpecialistDoctor() { return specialistDoctor; }
    public String getDiagnosis() { return diagnosis; }

    @Override
    public String toString() {
        return "Record[" + recordId + "] Patient: " + patient.getName()
            + " | Consulted: Dr." + consultationDoctor.getName()
            + " | Diagnosis: " + (diagnosis != null ? diagnosis : "Pending")
            + " | Specialist: " + (specialistDoctor != null ? "Dr." + specialistDoctor.getName() : "Not assigned");
    }
}

// ==================== SERVICES ====================

// --- Reception Service (registers patients, assigns priority, adds to queue) ---
class ReceptionService {
    private int patientCounter = 0;
    private final PatientQueue patientQueue;

    public ReceptionService(PatientQueue patientQueue) {
        this.patientQueue = patientQueue;
    }

    public Patient registerPatient(String name, int age, Priority priority) {
        String patientId = "PAT-" + (++patientCounter);
        Patient patient = new Patient(patientId, name, age, priority);
        patient.setStatus(PatientStatus.WAITING_IN_QUEUE);
        patientQueue.addPatient(patient);
        System.out.println("✅ Registered: " + patient);
        return patient;
    }
}

// --- Patient Priority Queue (core data structure) ---
//
// WHY PriorityBlockingQueue?
// 1. Automatically orders by priority (Critical first)
// 2. Thread-safe — multiple receptionists can add, multiple doctors can pull
// 3. poll() returns null if empty (no exception)
//
// Comparator logic:
// - First compare by priority level (Critical=1 < Low=4)
// - If same priority, compare by registration time (FIFO — first registered, first served)
//
class PatientQueue {
    private final PriorityBlockingQueue<Patient> queue;

    public PatientQueue() {
        this.queue = new PriorityBlockingQueue<>(100, (p1, p2) -> {
            // Lower priority level number = higher priority
            int priorityCompare = Integer.compare(
                p1.getPriority().getLevel(),
                p2.getPriority().getLevel()
            );
            if (priorityCompare != 0) return priorityCompare;
            // Same priority → FIFO (earlier registration time first)
            return p1.getRegistrationTime().compareTo(p2.getRegistrationTime());
        });
    }

    public void addPatient(Patient patient) {
        queue.offer(patient);
    }

    // Doctor calls this to get next highest-priority patient
    public Patient getNextPatient() {
        return queue.poll();  // Returns null if empty
    }

    public int getQueueSize() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void displayQueue() {
        System.out.println("\n📋 Current Queue (" + queue.size() + " patients):");
        // Create a sorted copy for display (PQ iterator doesn't guarantee order)
        List<Patient> sorted = new ArrayList<>(queue);
        sorted.sort((p1, p2) -> {
            int cmp = Integer.compare(p1.getPriority().getLevel(), p2.getPriority().getLevel());
            return cmp != 0 ? cmp : p1.getRegistrationTime().compareTo(p2.getRegistrationTime());
        });
        for (Patient p : sorted) {
            System.out.println("   " + p);
        }
    }
}

// --- Doctor Service (manages doctor pool, finds available specialists) ---
class DoctorService {
    private final Map<Specialization, List<Doctor>> doctorsBySpecialization;
    private final List<Doctor> consultationDoctors;  // General doctors for initial consultation

    public DoctorService() {
        this.doctorsBySpecialization = new HashMap<>();
        this.consultationDoctors = new ArrayList<>();
        for (Specialization spec : Specialization.values()) {
            doctorsBySpecialization.put(spec, new ArrayList<>());
        }
    }

    public void addDoctor(Doctor doctor) {
        doctorsBySpecialization.get(doctor.getSpecialization()).add(doctor);
        // General doctors can do initial consultations
        if (doctor.getSpecialization() == Specialization.GENERAL) {
            consultationDoctors.add(doctor);
        }
    }

    // Find any available consultation doctor (GENERAL)
    public Doctor getAvailableConsultationDoctor() {
        for (Doctor doc : consultationDoctors) {
            if (doc.isAvailable()) return doc;
        }
        return null;  // No doctor available
    }

    // Find available specialist by specialization
    public Doctor getAvailableSpecialist(Specialization specialization) {
        List<Doctor> specialists = doctorsBySpecialization.get(specialization);
        if (specialists == null) return null;
        for (Doctor doc : specialists) {
            if (doc.isAvailable()) return doc;
        }
        return null;
    }

    public void displayDoctors() {
        System.out.println("\n👨‍⚕️ Doctor Status:");
        for (Map.Entry<Specialization, List<Doctor>> entry : doctorsBySpecialization.entrySet()) {
            for (Doctor doc : entry.getValue()) {
                System.out.println("   " + doc);
            }
        }
    }
}

// --- Treatment Record Service (SRP — manages all records) ---
class TreatmentRecordService {
    private int recordCounter = 0;
    private final Map<String, TreatmentRecord> recordsByPatientId;
    private final List<TreatmentRecord> allRecords;

    public TreatmentRecordService() {
        this.recordsByPatientId = new HashMap<>();
        this.allRecords = new ArrayList<>();
    }

    public TreatmentRecord createRecord(Patient patient, Doctor consultationDoctor) {
        String recordId = "REC-" + (++recordCounter);
        TreatmentRecord record = new TreatmentRecord(recordId, patient, consultationDoctor);
        recordsByPatientId.put(patient.getPatientId(), record);
        allRecords.add(record);
        return record;
    }

    public TreatmentRecord getRecordByPatientId(String patientId) {
        return recordsByPatientId.get(patientId);
    }

    public void displayAllRecords() {
        System.out.println("\n📄 Treatment Records:");
        for (TreatmentRecord record : allRecords) {
            System.out.println("   " + record);
        }
    }
}

// ==================== HOSPITAL — ORCHESTRATOR (Facade Pattern) ====================

class HospitalManagementSystem {
    private final ReceptionService reception;
    private final PatientQueue patientQueue;
    private final DoctorService doctorService;
    private final TreatmentRecordService recordService;

    public HospitalManagementSystem() {
        this.patientQueue = new PatientQueue();
        this.reception = new ReceptionService(patientQueue);
        this.doctorService = new DoctorService();
        this.recordService = new TreatmentRecordService();
    }

    // --- Step 1: Reception registers patient ---
    public Patient registerPatient(String name, int age, Priority priority) {
        return reception.registerPatient(name, age, priority);
    }

    // --- Step 2: Add doctors to the system ---
    public void addDoctor(Doctor doctor) {
        doctorService.addDoctor(doctor);
    }

    // --- Step 3: Initial consultation doctor picks next patient ---
    public TreatmentRecord consultNextPatient() {
        // Get next patient from priority queue
        Patient patient = patientQueue.getNextPatient();
        if (patient == null) {
            System.out.println("⚠️  No patients in queue.");
            return null;
        }

        // Find available consultation doctor
        Doctor doctor = doctorService.getAvailableConsultationDoctor();
        if (doctor == null) {
            System.out.println("⚠️  No consultation doctors available. Patient back in queue.");
            patientQueue.addPatient(patient);  // Put patient back
            return null;
        }

        // Doctor takes patient
        doctor.setAvailable(false);
        patient.setStatus(PatientStatus.IN_CONSULTATION);

        // Create treatment record
        TreatmentRecord record = recordService.createRecord(patient, doctor);
        System.out.println("🩺 Consultation: " + patient.getName() + " → Dr. " + doctor.getName());

        return record;
    }

    // --- Step 4: After consultation, assign to specialist ---
    public boolean assignSpecialist(TreatmentRecord record, Specialization specialization, String diagnosis) {
        Doctor specialist = doctorService.getAvailableSpecialist(specialization);
        if (specialist == null) {
            System.out.println("⚠️  No " + specialization + " specialist available.");
            return false;
        }

        // Release consultation doctor
        record.getConsultationDoctor().setAvailable(true);

        // Assign specialist
        specialist.setAvailable(false);
        record.assignSpecialist(specialist, diagnosis);
        record.getPatient().setStatus(PatientStatus.ASSIGNED_SPECIALIST);

        System.out.println("🔬 Assigned: " + record.getPatient().getName()
            + " → Dr. " + specialist.getName() + " (" + specialization + ")"
            + " | Diagnosis: " + diagnosis);

        return true;
    }

    // --- Step 5: Specialist starts treatment ---
    public void startTreatment(TreatmentRecord record, String treatment) {
        record.startTreatment(treatment);
        record.getPatient().setStatus(PatientStatus.IN_TREATMENT);
        System.out.println("💊 Treatment started: " + record.getPatient().getName()
            + " → " + treatment);
    }

    // --- Step 6: Discharge patient ---
    public void dischargePatient(TreatmentRecord record) {
        record.endTreatment();
        record.getPatient().setStatus(PatientStatus.DISCHARGED);

        // Release specialist
        if (record.getSpecialistDoctor() != null) {
            record.getSpecialistDoctor().setAvailable(true);
        }

        System.out.println("🏥 Discharged: " + record.getPatient().getName());
    }

    // --- Display methods ---
    public void displayQueue() { patientQueue.displayQueue(); }
    public void displayDoctors() { doctorService.displayDoctors(); }
    public void displayRecords() { recordService.displayAllRecords(); }
}

// ==================== MAIN — DEMO ====================

public class HospitalSystem {
    public static void main(String[] args) throws InterruptedException {

        HospitalManagementSystem hospital = new HospitalManagementSystem();

        // ====== Setup: Add Doctors ======
        System.out.println("========== SETUP ==========");
        hospital.addDoctor(new Doctor("DOC-1", "Sharma", Specialization.GENERAL));
        hospital.addDoctor(new Doctor("DOC-2", "Patel", Specialization.GENERAL));
        hospital.addDoctor(new Doctor("DOC-3", "Gupta", Specialization.CARDIOLOGY));
        hospital.addDoctor(new Doctor("DOC-4", "Reddy", Specialization.NEUROLOGY));
        hospital.addDoctor(new Doctor("DOC-5", "Khan", Specialization.ORTHOPEDICS));
        hospital.displayDoctors();

        // ====== Step 1: Reception Registers Patients ======
        System.out.println("\n========== REGISTRATION ==========");
        Patient p1 = hospital.registerPatient("Amit", 45, Priority.LOW);
        Thread.sleep(10);  // Small delay so registration times differ
        Patient p2 = hospital.registerPatient("Priya", 30, Priority.CRITICAL);
        Thread.sleep(10);
        Patient p3 = hospital.registerPatient("Rahul", 60, Priority.HIGH);
        Thread.sleep(10);
        Patient p4 = hospital.registerPatient("Sneha", 25, Priority.MEDIUM);
        Thread.sleep(10);
        Patient p5 = hospital.registerPatient("Vijay", 55, Priority.CRITICAL);

        // ====== Show Queue (should be ordered by priority) ======
        hospital.displayQueue();
        // Expected order: Priya(CRITICAL) → Vijay(CRITICAL) → Rahul(HIGH) → Sneha(MEDIUM) → Amit(LOW)

        // ====== Step 2: Consultation Doctor Picks Patients ======
        System.out.println("\n========== CONSULTATIONS ==========");
        // Should pick Priya first (CRITICAL, registered earlier than Vijay)
        TreatmentRecord rec1 = hospital.consultNextPatient();
        TreatmentRecord rec2 = hospital.consultNextPatient();

        hospital.displayQueue();    // Should show 3 remaining
        hospital.displayDoctors();  // Both GENERAL docs should be busy

        // Try picking a 3rd — no consultation doctors available
        TreatmentRecord rec3 = hospital.consultNextPatient();  // Should warn: no doctors

        // ====== Step 3: Assign Specialists ======
        System.out.println("\n========== SPECIALIST ASSIGNMENT ==========");
        hospital.assignSpecialist(rec1, Specialization.CARDIOLOGY, "Chest pain, suspected angina");
        hospital.assignSpecialist(rec2, Specialization.NEUROLOGY, "Severe headaches, needs MRI");

        hospital.displayDoctors();  // GENERAL docs free, specialists busy

        // ====== Step 4: Now consultation docs are free, pick more patients ======
        System.out.println("\n========== MORE CONSULTATIONS ==========");
        rec3 = hospital.consultNextPatient();   // Should pick Rahul (HIGH)
        TreatmentRecord rec4 = hospital.consultNextPatient();  // Should pick Sneha (MEDIUM)

        hospital.displayQueue();  // Only Amit (LOW) left

        // ====== Step 5: Treatment & Discharge ======
        System.out.println("\n========== TREATMENT & DISCHARGE ==========");
        hospital.startTreatment(rec1, "Angioplasty procedure");
        hospital.dischargePatient(rec1);

        hospital.startTreatment(rec2, "MRI scan + medication");
        hospital.dischargePatient(rec2);

        // ====== Step 6: Show All Records ======
        System.out.println("\n========== ALL TREATMENT RECORDS ==========");
        hospital.displayRecords();

        // ====== Final State ======
        System.out.println("\n========== FINAL STATE ==========");
        hospital.displayQueue();
        hospital.displayDoctors();
    }
}
