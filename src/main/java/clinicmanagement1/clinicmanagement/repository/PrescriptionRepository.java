package clinicmanagement1.clinicmanagement.repository;

import clinicmanagement1.clinicmanagement.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {

    // C2 – derived
    List<Prescription> findByAppointmentId(Long appointmentId);

    // C3 – JPQL path navigation, newest first
    @Query("SELECT p FROM Prescription p WHERE p.appointment.patient.id = :patientId ORDER BY p.issuedDate DESC")
    List<Prescription> findByPatientId(@Param("patientId") Long patientId);

    // C4 – guard before appointment delete
    boolean existsByAppointmentId(Long appointmentId);

    // Bonus – most prescribed medicines
    @Query("SELECT p.medicineName, COUNT(p) FROM Prescription p GROUP BY p.medicineName ORDER BY COUNT(p) DESC")
    List<Object[]> countByMedicine();
}
