package clinicmanagement1.clinicmanagement.controller;

import clinicmanagement1.clinicmanagement.entity.Appointment;
import clinicmanagement1.clinicmanagement.entity.Doctor;
import clinicmanagement1.clinicmanagement.entity.Patient;
import clinicmanagement1.clinicmanagement.service.DoctorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Doctor create(@RequestBody Doctor doctor) {
        return doctorService.create(doctor);
    }

    @GetMapping
    public List<Doctor> getAll() {
        return doctorService.findAll();
    }

    @GetMapping("/{id}")
    public Doctor getById(@PathVariable Long id) {
        return doctorService.findById(id);
    }

    @PutMapping("/{id}")
    public Doctor update(@PathVariable Long id, @RequestBody Doctor doctor) {
        return doctorService.update(id, doctor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        doctorService.delete(id);
    }

    // ---------------- Many-to-Many (Doctor <-> Specialization) ----------------

    @PutMapping("/{doctorId}/specializations/{specializationId}")
    public Doctor addSpecialization(@PathVariable Long doctorId, @PathVariable Long specializationId) {
        return doctorService.addSpecialization(doctorId, specializationId);
    }

    @DeleteMapping("/{doctorId}/specializations/{specializationId}")
    public Doctor removeSpecialization(@PathVariable Long doctorId, @PathVariable Long specializationId) {
        return doctorService.removeSpecialization(doctorId, specializationId);
    }

    // ---------------- Queries ----------------

    /** GET /api/doctors/by-specialization?name=Cardiology */
    @GetMapping("/by-specialization")
    public List<Doctor> bySpecialization(@RequestParam String name) {
        return doctorService.findBySpecializationName(name);
    }

    /** GET /api/doctors/search?lastName=smi */
    @GetMapping("/search")
    public List<Doctor> search(@RequestParam String lastName) {
        return doctorService.searchByLastName(lastName);
    }

    /** GET /api/doctors/by-office?room=A-101 */
    @GetMapping("/by-office")
    public Doctor byOffice(@RequestParam("room") String roomNumber) {
        return doctorService.findByRoomNumber(roomNumber);
    }

    /** GET /api/doctors/multi-specialized?min=2 */
    @GetMapping("/multi-specialized")
    public List<Doctor> multiSpecialized(@RequestParam(defaultValue = "2") int min) {
        return doctorService.findWithAtLeastSpecializations(min);
    }

    @GetMapping("/{id}/appointments")
    public List<Appointment> appointments(@PathVariable Long id) {
        return doctorService.findAppointments(id);
    }

    @GetMapping("/{id}/patients")
    public List<Patient> patients(@PathVariable Long id) {
        return doctorService.findPatients(id);
    }
}
