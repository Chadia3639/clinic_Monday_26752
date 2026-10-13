package clinicmanagement1.clinicmanagement.controller;

import clinicmanagement1.clinicmanagement.entity.Prescription;
import clinicmanagement1.clinicmanagement.service.PrescriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    // C1
    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public String save(@RequestParam Long appointmentId, @RequestBody Prescription prescription) {
        return prescriptionService.save(appointmentId, prescription);
    }

    // C2
    @GetMapping("/appointment/{appointmentId}")
    public List<Prescription> byAppointment(@PathVariable Long appointmentId) {
        return prescriptionService.findByAppointment(appointmentId);
    }

    // C3
    @GetMapping("/patient/{patientId}")
    public List<Prescription> byPatient(@PathVariable Long patientId) {
        return prescriptionService.findByPatient(patientId);
    }

    // C4 – update
    @PutMapping("/update/{id}")
    public String update(@PathVariable UUID id, @RequestBody Prescription prescription) {
        return prescriptionService.update(id, prescription);
    }

    // C4 – delete
    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable UUID id) {
        return prescriptionService.delete(id);
    }

    // Bonus – top medicines
    @GetMapping("/stats/top-medicines")
    public List<Object[]> topMedicines() {
        return prescriptionService.topMedicines();
    }
}
