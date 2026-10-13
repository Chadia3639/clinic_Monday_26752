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

    // D1 – doctor's agenda for a day (between start/end of day, no CANCELLED)
    List<Appointment> findByDoctorIdAndAppointmentDateBetweenAndStatusNotOrderByAppointmentDate(
            Long doctorId, LocalDateTime from, LocalDateTime to, AppointmentStatus status);

    // D2 – upcoming appointments of a patient (SCHEDULED or CONFIRMED, today or later)
    List<Appointment> findByPatientIdAndAppointmentDateGreaterThanEqualAndStatusInOrderByAppointmentDate(
            Long patientId, LocalDateTime from, List<AppointmentStatus> statuses);

    // D4 – count today's appointments
    long countByAppointmentDateBetween(LocalDateTime from, LocalDateTime to);

    // D4 – count by status
    long countByStatus(AppointmentStatus status);

    // Bonus – doctor workload (COMPLETED) between two dates
    @Query("""
            SELECT d.firstName, d.lastName, COUNT(a)
            FROM Appointment a JOIN a.doctor d
            WHERE a.status = clinicmanagement1.clinicmanagement.entity.AppointmentStatus.COMPLETED
              AND a.appointmentDate BETWEEN :start AND :end
            GROUP BY d.id, d.firstName, d.lastName
            ORDER BY COUNT(a) DESC
            """)
    List<Object[]> countCompletedByDoctor(
            @org.springframework.data.repository.query.Param("start") LocalDateTime start,
            @org.springframework.data.repository.query.Param("end") LocalDateTime end);

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
