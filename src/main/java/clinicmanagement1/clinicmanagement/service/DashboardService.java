package clinicmanagement1.clinicmanagement.service;

import clinicmanagement1.clinicmanagement.entity.AppointmentStatus;
import clinicmanagement1.clinicmanagement.repository.AppointmentRepository;
import clinicmanagement1.clinicmanagement.repository.DoctorRepository;
import clinicmanagement1.clinicmanagement.repository.OfficeRepository;
import clinicmanagement1.clinicmanagement.repository.PatientRepository;
import clinicmanagement1.clinicmanagement.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DashboardService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final OfficeRepository officeRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;

    public DashboardService(PatientRepository patientRepository,
                            DoctorRepository doctorRepository,
                            OfficeRepository officeRepository,
                            AppointmentRepository appointmentRepository,
                            PrescriptionRepository prescriptionRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.officeRepository = officeRepository;
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Long> summary() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        Map<String, Long> map = new LinkedHashMap<>();
        map.put("patients", patientRepository.count());
        map.put("doctors", doctorRepository.count());
        map.put("freeOffices", (long) officeRepository.findFreeOffices().size());
        map.put("appointmentsToday", appointmentRepository.countByAppointmentDateBetween(startOfDay, endOfDay));
        map.put("scheduled", appointmentRepository.countByStatus(AppointmentStatus.SCHEDULED));
        map.put("prescriptions", prescriptionRepository.count());
        return map;
    }
}
