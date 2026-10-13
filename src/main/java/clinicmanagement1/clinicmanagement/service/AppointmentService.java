package clinicmanagement1.clinicmanagement.service;

import clinicmanagement1.clinicmanagement.entity.Appointment;
import clinicmanagement1.clinicmanagement.entity.AppointmentStatus;
import clinicmanagement1.clinicmanagement.entity.Doctor;
import clinicmanagement1.clinicmanagement.entity.Patient;
import clinicmanagement1.clinicmanagement.exception.BadRequestException;
import clinicmanagement1.clinicmanagement.exception.ConflictException;
import clinicmanagement1.clinicmanagement.service.PrescriptionService;
import clinicmanagement1.clinicmanagement.exception.ResourceNotFoundException;
import clinicmanagement1.clinicmanagement.report.DoctorWorkload;
import clinicmanagement1.clinicmanagement.repository.AppointmentRepository;
import clinicmanagement1.clinicmanagement.repository.DoctorRepository;
import clinicmanagement1.clinicmanagement.repository.PatientRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private PrescriptionService prescriptionService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorRepository doctorRepository,
                              PatientRepository patientRepository) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    public void setPrescriptionService(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    /** A patient books an appointment with a doctor. */
    @Transactional
    public Appointment book(Long patientId, Long doctorId, Appointment input) {
        if (input.getAppointmentDate() == null) {
            throw new BadRequestException("appointmentDate is required");
        }
        if (input.getReason() == null || input.getReason().isBlank()) {
            throw new BadRequestException("reason is required");
        }
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        if (appointmentRepository.existsByDoctorIdAndAppointmentDateAndStatusNot(
                doctorId, input.getAppointmentDate(), AppointmentStatus.CANCELLED)) {
            throw new ConflictException("Doctor " + doctorId + " is already booked at " + input.getAppointmentDate());
        }

        input.setId(null);
        input.setPatient(patient);
        input.setDoctor(doctor);
        if (input.getStatus() == null) {
            input.setStatus(AppointmentStatus.SCHEDULED);
        }
        return appointmentRepository.save(input);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
    }

    @Transactional
    public Appointment update(Long id, Appointment input) {
        Appointment existing = findById(id);
        if (input.getAppointmentDate() != null) existing.setAppointmentDate(input.getAppointmentDate());
        if (input.getReason() != null) existing.setReason(input.getReason());
        if (input.getStatus() != null) existing.setStatus(input.getStatus());
        return appointmentRepository.save(existing);
    }

    @Transactional
    public Appointment updateStatus(Long id, AppointmentStatus status) {
        Appointment existing = findById(id);
        existing.setStatus(status);
        return appointmentRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Appointment existing = findById(id);
        if (prescriptionService != null && prescriptionService.hasPresciptions(id)) {
            throw new ConflictException("That appointment has prescriptions, delete them first");
        }
        appointmentRepository.delete(existing);
    }


    @Transactional(readOnly = true)
    public List<Appointment> findByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findByDoctorAndStatus(Long doctorId, AppointmentStatus status) {
        return appointmentRepository.findByDoctorIdAndStatus(doctorId, status);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findBetween(LocalDateTime from, LocalDateTime to) {
        return appointmentRepository.findByAppointmentDateBetweenOrderByAppointmentDate(from, to);
    }

    @Transactional(readOnly = true)
    public List<DoctorWorkload> workloadPerDoctor() {
        return appointmentRepository.countAppointmentsPerDoctor();
    }

    // D1 – doctor's agenda for a day
    @Transactional(readOnly = true)
    public List<Appointment> findAgenda(Long doctorId, LocalDate date) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new ResourceNotFoundException("The doctor with that id does not exist");
        }
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();
        return appointmentRepository
                .findByDoctorIdAndAppointmentDateBetweenAndStatusNotOrderByAppointmentDate(
                        doctorId, from, to, AppointmentStatus.CANCELLED);
    }

    // D2 – upcoming appointments of a patient
    @Transactional(readOnly = true)
    public List<Appointment> findUpcoming(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("That patient with that ssnumber does not exist");
        }
        return appointmentRepository
                .findByPatientIdAndAppointmentDateGreaterThanEqualAndStatusInOrderByAppointmentDate(
                        patientId,
                        LocalDate.now().atStartOfDay(),
                        Arrays.asList(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED));
    }

    // Bonus – doctor workload for a date range
    @Transactional(readOnly = true)
    public List<Object[]> doctorWorkload(LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.countCompletedByDoctor(start, end);
    }
}
