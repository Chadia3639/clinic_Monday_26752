package clinicmanagement1.clinicmanagement.controller;

import clinicmanagement1.clinicmanagement.entity.Appointment;
import clinicmanagement1.clinicmanagement.entity.Patient;
import clinicmanagement1.clinicmanagement.service.PatientService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Patient create(@RequestBody Patient patient) {
        return patientService.create(patient);
    }

    @GetMapping
    public List<Patient> getAll() {
        return patientService.findAll();
    }

    @GetMapping("/{id}")
    public Patient getById(@PathVariable Long id) {
        return patientService.findById(id);
    }

    /** GET /api/patients/search?lastName=doe */
    @GetMapping("/search")
    public List<Patient> search(@RequestParam String lastName) {
        return patientService.searchByLastName(lastName);
    }

    @PutMapping("/{id}")
    public Patient update(@PathVariable Long id, @RequestBody Patient patient) {
        return patientService.update(id, patient);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        patientService.delete(id);
    }

    @GetMapping("/{id}/appointments")
    public List<Appointment> appointments(@PathVariable Long id) {
        return patientService.findAppointments(id);
    }
}
