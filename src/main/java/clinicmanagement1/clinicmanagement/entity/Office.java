package clinicmanagement1.clinicmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 1-1 with Doctor. The foreign key (office_id) lives in the DOCTORS table,
 * so Doctor is the owning side and Office is the inverse (mappedBy) side.
 */
@Entity
@Table(name = "offices")
@Getter
@Setter
@NoArgsConstructor
public class Office {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number", nullable = false, unique = true, length = 20)
    private String roomNumber;

    @Column(name = "floor_number")
    private Integer floorNumber;

    @Column(length = 100)
    private String building;

    @OneToOne(mappedBy = "office")
    @JsonIgnore
    private Doctor doctor;
}
