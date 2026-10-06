package clinicmanagement1.clinicmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * N-N with Doctor (inverse side). The join table "doctor_specialization" is owned by Doctor.
 */
@Entity
@Table(name = "specializations")
@Getter
@Setter
@NoArgsConstructor
public class Specialization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @ManyToMany(mappedBy = "specializations")
    @JsonIgnore
    private Set<Doctor> doctors = new HashSet<>();
}
