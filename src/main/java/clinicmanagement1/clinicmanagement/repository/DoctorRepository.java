package clinicmanagement1.clinicmanagement.repository;

import clinicmanagement1.clinicmanagement.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    @Query("SELECT d FROM Doctor d LEFT JOIN FETCH d.specializations WHERE d.id = :doctorID")
    Optional<Doctor> findByIdWithSpecializations(@Param("doctorID") Long doctorID);
    
   @Query("SELECT COUNT(d) > 0 FROM Doctor d WHERE d.office.id = :officeId")
    boolean existsByOfficeId(@Param("officeId") Long officeId);

    /** Query: all doctors having a given specialization (N-N navigation). */
    List<Doctor> findBySpecializations_NameIgnoreCase(String name);

    /** Query: all doctors having a specialization, by its id. */
    List<Doctor> findBySpecializations_Id(Long specializationId);

    /** Query: search doctors by last name. */
    List<Doctor> findByLastNameContainingIgnoreCase(String lastName);

    /** D3 – search by first name OR last name, case-insensitive, sorted by last name. */
    @Query("SELECT d FROM Doctor d WHERE LOWER(d.firstName) LIKE LOWER(CONCAT('%',:name,'%')) OR LOWER(d.lastName) LIKE LOWER(CONCAT('%',:name,'%')) ORDER BY d.lastName")
    List<Doctor> searchByName(@Param("name") String name);

    /** Query: find the doctor sitting in a given office (1-1 navigation). */
    Optional<Doctor> findByOffice_RoomNumber(String roomNumber);

    /** Query: doctors holding at least N specializations. */
    @Query("select d from Doctor d where size(d.specializations) >= :n")
    List<Doctor> findWithAtLeastSpecializations(@Param("n") int n);
}
