package clinicmanagement1.clinicmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(unique = true, length = 120)
    private String email;

    @Column(length = 30)
    private String phone;

    /**
     * 1-1 : each doctor has EXACTLY ONE office.
     * Foreign key office_id lives here (unique + not null).
     */
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "office_id", nullable = false, unique = true)
    private Office office;

    /**
     * N-N : a doctor has many specializations, a specialization belongs to many doctors.
     * Pure link (no extra attributes) => a join table is enough, no entity needed.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "doctor_specialization",
            joinColumns = @JoinColumn(name = "doctor_id"),
            inverseJoinColumns = @JoinColumn(name = "specialization_id")
    )
    private Set<Specialization> specializations = new HashSet<>();

    /** 1-N : one doctor has many appointments (FK doctor_id lives in APPOINTMENTS). */
    @OneToMany(mappedBy = "doctor")
    @JsonIgnore
    private Set<Appointment> appointments = new HashSet<>();
}
