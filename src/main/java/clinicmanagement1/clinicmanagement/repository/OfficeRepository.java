package clinicmanagement1.clinicmanagement.repository;

import clinicmanagement1.clinicmanagement.entity.Office;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface OfficeRepository extends JpaRepository<Office, Long> {

 Optional<Office> findByRoomNumber(String roomNumber);   
@Query("SELECT o FROM Office o WHERE NOT EXISTS (SELECT d FROM Doctor d WHERE d.office = o) ORDER BY o.roomNumber ASC")
    List<Office> findFreeOffices();
}
