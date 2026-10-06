package clinicmanagement1.clinicmanagement.repository;

import clinicmanagement1.clinicmanagement.entity.Appointment;
import clinicmanagement1.clinicmanagement.entity.AppointmentStatus;
import clinicmanagement1.clinicmanagement.report.DoctorWorkload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByStatus(AppointmentStatus status);

    List<Appointment> findByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    List<Appointment> findByAppointmentDateBetweenOrderByAppointmentDate(LocalDateTime from, LocalDateTime to);

    boolean existsByDoctorId(Long doctorId);

    boolean existsByPatientId(Long patientId);

    /** Used to prevent double-booking a doctor at the same time. */
    boolean existsByDoctorIdAndAppointmentDateAndStatusNot(Long doctorId,
                                                           LocalDateTime appointmentDate,
                                                           AppointmentStatus status);

    /** Query: number of appointments per doctor. */
    @Query("""
            select new clinicmanagement1.clinicmanagement.report.DoctorWorkload(
                d.id, concat(d.firstName, ' ', d.lastName), count(a))
            from Appointment a join a.doctor d
            group by d.id, d.firstName, d.lastName
            order by count(a) desc
            """)
    List<DoctorWorkload> countAppointmentsPerDoctor();
}
