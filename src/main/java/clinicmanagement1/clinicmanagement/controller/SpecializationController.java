package clinicmanagement1.clinicmanagement.controller;

import clinicmanagement1.clinicmanagement.entity.Doctor;
import clinicmanagement1.clinicmanagement.entity.Specialization;
import clinicmanagement1.clinicmanagement.service.SpecializationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/specializations")
public class SpecializationController {

    private final SpecializationService specializationService;

    public SpecializationController(SpecializationService specializationService) {
        this.specializationService = specializationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Specialization create(@RequestBody Specialization specialization) {
        return specializationService.create(specialization);
    }

    @GetMapping
    public List<Specialization> getAll() {
        return specializationService.findAll();
    }

    @GetMapping("/{id}")
    public Specialization getById(@PathVariable Long id) {
        return specializationService.findById(id);
    }

    /** GET /api/specializations/search?keyword=cardio */
    @GetMapping("/search")
    public List<Specialization> search(@RequestParam String keyword) {
        return specializationService.search(keyword);
    }

    @PutMapping("/{id}")
    public Specialization update(@PathVariable Long id, @RequestBody Specialization specialization) {
        return specializationService.update(id, specialization);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        specializationService.delete(id);
    }

    /** Other side of the N-N: every doctor having this specialization. */
    @GetMapping("/{id}/doctors")
    public List<Doctor> getDoctors(@PathVariable Long id) {
        return specializationService.findDoctors(id);
    }
}
