package clinicmanagement1.clinicmanagement.service;

import clinicmanagement1.clinicmanagement.entity.Appointment;
import clinicmanagement1.clinicmanagement.entity.AppointmentStatus;
import clinicmanagement1.clinicmanagement.entity.Prescription;
import clinicmanagement1.clinicmanagement.exception.BadRequestException;
import clinicmanagement1.clinicmanagement.exception.ResourceNotFoundException;
import clinicmanagement1.clinicmanagement.repository.AppointmentRepository;
import clinicmanagement1.clinicmanagement.repository.PrescriptionRepository;
import clinicmanagement1.clinicmanagement.service.AppointmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               AppointmentRepository appointmentRepository,
                               AppointmentService appointmentService) {
        this.prescriptionRepository = prescriptionRepository;
        this.appointmentRepository = appointmentRepository;
        this.appointmentService = appointmentService;
        appointmentService.setPrescriptionService(this);
    }

    // C1
    @Transactional
    public String save(Long appointmentId, Prescription input) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("The appointment with that id does not exist"));
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Prescriptions are only for COMPLETED appointments");
        }
        input.setId(null);
        input.setIssuedDate(LocalDate.now());
        input.setAppointment(appointment);
        prescriptionRepository.save(input);
        return "Prescription saved successfully";
    }

    // C2
    @Transactional(readOnly = true)
    public List<Prescription> findByAppointment(Long appointmentId) {
        if (!appointmentRepository.existsById(appointmentId)) {
            throw new ResourceNotFoundException("The appointment with that id does not exist");
        }
        return prescriptionRepository.findByAppointmentId(appointmentId);
    }

    // C3
    @Transactional(readOnly = true)
    public List<Prescription> findByPatient(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    // C4 – update
    @Transactional
    public String update(UUID id, Prescription input) {
        Prescription existing = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("The prescription with that id does not exist"));
        if (input.getMedicineName() != null) existing.setMedicineName(input.getMedicineName());
        if (input.getDosage() != null) existing.setDosage(input.getDosage());
        if (input.getDurationDays() > 0) existing.setDurationDays(input.getDurationDays());
        prescriptionRepository.save(existing);
        return "Prescription updated successfully";
    }

    // C4 – delete
    @Transactional
    public String delete(UUID id) {
        Prescription existing = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("The prescription with that id does not exist"));
        prescriptionRepository.delete(existing);
        return "Prescription deleted successfully";
    }

    // C4 – guard used by AppointmentService
    public boolean hasPresciptions(Long appointmentId) {
        return prescriptionRepository.existsByAppointmentId(appointmentId);
    }

    // Bonus – top medicines
    @Transactional(readOnly = true)
    public List<Object[]> topMedicines() {
        return prescriptionRepository.countByMedicine();
    }
}
