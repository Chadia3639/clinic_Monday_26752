package clinicmanagement1.clinicmanagement.service;

import clinicmanagement1.clinicmanagement.entity.Appointment;
import clinicmanagement1.clinicmanagement.entity.Patient;
import clinicmanagement1.clinicmanagement.exception.ConflictException;
import clinicmanagement1.clinicmanagement.exception.ResourceNotFoundException;
import clinicmanagement1.clinicmanagement.repository.AppointmentRepository;
import clinicmanagement1.clinicmanagement.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    public PatientService(PatientRepository patientRepository, AppointmentRepository appointmentRepository) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public Patient create(Patient input) {
        input.setId(null);
        return patientRepository.save(input);
    }

    @Transactional(readOnly = true)
    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Patient findById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
    }

    @Transactional(readOnly = true)
    public List<Patient> searchByLastName(String lastName) {
        return patientRepository.findByLastNameContainingIgnoreCase(lastName);
    }

    @Transactional
    public Patient update(Long id, Patient input) {
        Patient existing = findById(id);
        if (input.getFirstName() != null) existing.setFirstName(input.getFirstName());
        if (input.getLastName() != null) existing.setLastName(input.getLastName());
        if (input.getEmail() != null) existing.setEmail(input.getEmail());
        if (input.getPhone() != null) existing.setPhone(input.getPhone());
        if (input.getDateOfBirth() != null) existing.setDateOfBirth(input.getDateOfBirth());
        return patientRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Patient existing = findById(id);
        if (appointmentRepository.existsByPatientId(id)) {
            throw new ConflictException("Patient " + id + " still has appointments and cannot be deleted");
        }
        patientRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAppointments(Long patientId) {
        findById(patientId);
        return appointmentRepository.findByPatientId(patientId);
    }
}
