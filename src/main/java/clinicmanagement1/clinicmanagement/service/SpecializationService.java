package clinicmanagement1.clinicmanagement.service;

import clinicmanagement1.clinicmanagement.entity.Doctor;
import clinicmanagement1.clinicmanagement.entity.Specialization;
import clinicmanagement1.clinicmanagement.exception.BadRequestException;
import clinicmanagement1.clinicmanagement.exception.ConflictException;
import clinicmanagement1.clinicmanagement.exception.ResourceNotFoundException;
import clinicmanagement1.clinicmanagement.repository.DoctorRepository;
import clinicmanagement1.clinicmanagement.repository.SpecializationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SpecializationService {

    private final SpecializationRepository specializationRepository;
    private final DoctorRepository doctorRepository;

    public SpecializationService(SpecializationRepository specializationRepository,
                                 DoctorRepository doctorRepository) {
        this.specializationRepository = specializationRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public Specialization create(Specialization input) {
        if (input.getName() == null || input.getName().isBlank()) {
            throw new BadRequestException("Specialization name is required");
        }
        if (specializationRepository.existsByNameIgnoreCase(input.getName())) {
            throw new ConflictException("Specialization '" + input.getName() + "' already exists");
        }
        input.setId(null);
        return specializationRepository.save(input);
    }

    @Transactional(readOnly = true)
    public List<Specialization> findAll() {
        return specializationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Specialization findById(Long id) {
        return specializationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialization", id));
    }

    @Transactional(readOnly = true)
    public List<Specialization> search(String keyword) {
        return specializationRepository.findByNameContainingIgnoreCase(keyword);
    }

    @Transactional
    public Specialization update(Long id, Specialization input) {
        Specialization existing = findById(id);
        if (input.getName() != null && !input.getName().isBlank()) {
            specializationRepository.findByNameIgnoreCase(input.getName())
                    .filter(other -> !other.getId().equals(id))
                    .ifPresent(other -> {
                        throw new ConflictException("Specialization '" + input.getName() + "' already exists");
                    });
            existing.setName(input.getName());
        }
        if (input.getDescription() != null) {
            existing.setDescription(input.getDescription());
        }
        return specializationRepository.save(existing);
    }

    /**
     * Doctor owns the join table, so before deleting a specialization we detach it
     * from every doctor that has it. Doctors themselves are NOT deleted.
     */
    @Transactional
    public void delete(Long id) {
        Specialization existing = findById(id);
        for (Doctor doctor : new ArrayList<>(existing.getDoctors())) {
            doctor.getSpecializations().remove(existing);
        }
        existing.getDoctors().clear();
        specializationRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public List<Doctor> findDoctors(Long id) {
        findById(id); // 404 if the specialization does not exist
        return doctorRepository.findBySpecializations_Id(id);
    }
}
