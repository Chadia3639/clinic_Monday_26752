package clinicmanagement1.clinicmanagement.service;

import clinicmanagement1.clinicmanagement.entity.Appointment;
import clinicmanagement1.clinicmanagement.entity.Doctor;
import clinicmanagement1.clinicmanagement.entity.Office;
import clinicmanagement1.clinicmanagement.entity.Patient;
import clinicmanagement1.clinicmanagement.entity.Specialization;
import clinicmanagement1.clinicmanagement.exception.BadRequestException;
import clinicmanagement1.clinicmanagement.exception.ConflictException;
import clinicmanagement1.clinicmanagement.exception.ResourceNotFoundException;
import clinicmanagement1.clinicmanagement.repository.AppointmentRepository;
import clinicmanagement1.clinicmanagement.repository.DoctorRepository;
import clinicmanagement1.clinicmanagement.repository.PatientRepository;
import clinicmanagement1.clinicmanagement.repository.SpecializationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final SpecializationRepository specializationRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;

    public DoctorService(DoctorRepository doctorRepository,
                         SpecializationRepository specializationRepository,
                         AppointmentRepository appointmentRepository,
                         PatientRepository patientRepository) {
        this.doctorRepository = doctorRepository;
        this.specializationRepository = specializationRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
    }

    // ------------------------------------------------------------------ CRUD

    /**
     * Creates a doctor together with its (mandatory) office. Specializations are referenced
     * by id only, e.g. "specializations": [ {"id": 1}, {"id": 2} ].
     */
    @Transactional
    public Doctor create(Doctor input) {
        if (input.getOffice() == null) {
            throw new BadRequestException("A doctor must have exactly one office");
        }
        input.setId(null);
        input.getOffice().setId(null);
        input.setSpecializations(resolveSpecializations(input.getSpecializations()));
        return doctorRepository.save(input);
    }

    @Transactional(readOnly = true)
    public List<Doctor> findAll() {
        return doctorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Doctor findById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
    }

    @Transactional
    public Doctor update(Long id, Doctor input) {
        Doctor existing = findById(id);

        if (input.getFirstName() != null) existing.setFirstName(input.getFirstName());
        if (input.getLastName() != null) existing.setLastName(input.getLastName());
        if (input.getEmail() != null) existing.setEmail(input.getEmail());
        if (input.getPhone() != null) existing.setPhone(input.getPhone());

        // The doctor keeps the SAME office row (exactly one office): we only update its fields.
        if (input.getOffice() != null) {
            Office office = existing.getOffice();
            if (input.getOffice().getRoomNumber() != null) office.setRoomNumber(input.getOffice().getRoomNumber());
            if (input.getOffice().getFloorNumber() != null) office.setFloorNumber(input.getOffice().getFloorNumber());
            if (input.getOffice().getBuilding() != null) office.setBuilding(input.getOffice().getBuilding());
        }

        // If a specializations array is sent, it REPLACES the current set.
        if (input.getSpecializations() != null && !input.getSpecializations().isEmpty()) {
            existing.setSpecializations(resolveSpecializations(input.getSpecializations()));
        }
        return doctorRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Doctor existing = findById(id);
        if (appointmentRepository.existsByDoctorId(id)) {
            throw new ConflictException("Doctor " + id + " still has appointments and cannot be deleted");
        }
        // join-table rows are removed automatically (Doctor owns the N-N); the office is removed by cascade.
        doctorRepository.delete(existing);
    }

    // ------------------------------------------------- Many-to-Many operations

    @Transactional
    public Doctor addSpecialization(Long doctorId, Long specializationId) {
        Doctor doctor = findById(doctorId);
        Specialization spec = specializationRepository.findById(specializationId)
                .orElseThrow(() -> new ResourceNotFoundException("Specialization", specializationId));
        doctor.getSpecializations().add(spec);
        return doctorRepository.save(doctor);
    }

    @Transactional
    public Doctor removeSpecialization(Long doctorId, Long specializationId) {
        Doctor doctor = findById(doctorId);
        boolean removed = doctor.getSpecializations().removeIf(s -> s.getId().equals(specializationId));
        if (!removed) {
            throw new ResourceNotFoundException("Specialization (assigned to doctor " + doctorId + ")", specializationId);
        }
        return doctorRepository.save(doctor);
    }

    // ----------------------------------------------------------------- Queries

    @Transactional(readOnly = true)
    public List<Doctor> findBySpecializationName(String name) {
        return doctorRepository.findBySpecializations_NameIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Doctor> searchByLastName(String lastName) {
        return doctorRepository.findByLastNameContainingIgnoreCase(lastName);
    }

    @Transactional(readOnly = true)
    public Doctor findByRoomNumber(String roomNumber) {
        return doctorRepository.findByOffice_RoomNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("No doctor is assigned to office " + roomNumber));
    }

    @Transactional(readOnly = true)
    public List<Doctor> findWithAtLeastSpecializations(int n) {
        return doctorRepository.findWithAtLeastSpecializations(n);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAppointments(Long doctorId) {
        findById(doctorId);
        return appointmentRepository.findByDoctorId(doctorId);
    }

    @Transactional(readOnly = true)
    public List<Patient> findPatients(Long doctorId) {
        findById(doctorId);
        return patientRepository.findPatientsByDoctorId(doctorId);
    }

    // ----------------------------------------------------------------- helpers

    private Set<Specialization> resolveSpecializations(Set<Specialization> requested) {
        Set<Specialization> resolved = new HashSet<>();
        if (requested == null) {
            return resolved;
        }
        for (Specialization s : requested) {
            if (s.getId() == null) {
                throw new BadRequestException("Each specialization must be referenced by its id");
            }
            resolved.add(specializationRepository.findById(s.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Specialization", s.getId())));
        }
        return resolved;
    }
}
