package clinicmanagement1.clinicmanagement.repository;

import clinicmanagement1.clinicmanagement.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findByLastNameContainingIgnoreCase(String lastName);

    /** Query: distinct patients who have booked at least one appointment with a given doctor. */
    @Query("select distinct a.patient from Appointment a where a.doctor.id = :doctorId")
    List<Patient> findPatientsByDoctorId(@Param("doctorId") Long doctorId);
}
