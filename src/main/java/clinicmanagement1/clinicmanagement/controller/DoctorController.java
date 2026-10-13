package clinicmanagement1.clinicmanagement.controller;

import clinicmanagement1.clinicmanagement.entity.Appointment;
import clinicmanagement1.clinicmanagement.entity.Doctor;
import clinicmanagement1.clinicmanagement.entity.Patient;
import clinicmanagement1.clinicmanagement.service.AppointmentService;
import clinicmanagement1.clinicmanagement.service.DoctorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    public DoctorController(DoctorService doctorService, AppointmentService appointmentService) {
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
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

    @PutMapping("/{doctorId}/specializations/{specializationId}")
    public ResponseEntity<String> addSpecialization(@PathVariable Long doctorId,
                                                    @PathVariable Long specializationId) {
        String result = doctorService.addSpecializationToDoctor(doctorId, specializationId);
        if (result.equals("The doctor with that id does not exist") ||
            result.equals("That specialization does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        } else if (result.equals("The doctor already has that specialization")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{doctorId}/specializations/{specializationId}")
    public ResponseEntity<String> removeSpecialization(@PathVariable Long doctorId,
                                                       @PathVariable Long specializationId) {
        String result = doctorService.removeSpecializationFromDoctor(doctorId, specializationId);
        if (result.equals("The doctor with that id does not exist") ||
            result.equals("That specialization does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        } else if (result.equals("The doctor does not have that specialization")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/office")
    public ResponseEntity<?> moveDoctorToOffice(@PathVariable Long id,
                                                @RequestParam Long officeNumber) {
        String result = doctorService.moveDoctorToOffice(id, officeNumber);
        if (result.equals("Office changed successfully")) return ResponseEntity.ok(result);
        if (result.contains("does not exist")) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(result);
    }

    @GetMapping("/by-specialization")
    public List<Doctor> bySpecialization(@RequestParam String name) {
        return doctorService.findBySpecializationName(name);
    }

    // D3 – search by first or last name
    @GetMapping("/search")
    public List<Doctor> search(@RequestParam(required = false) String lastName,
                               @RequestParam(required = false) String name) {
        if (name != null) return doctorService.searchByName(name);
        return doctorService.searchByLastName(lastName != null ? lastName : "");
    }

    @GetMapping("/by-office")
    public Doctor byOffice(@RequestParam("room") String roomNumber) {
        return doctorService.findByRoomNumber(roomNumber);
    }

    @GetMapping("/multi-specialized")
    public List<Doctor> multiSpecialized(@RequestParam(defaultValue = "2") int min) {
        return doctorService.findWithAtLeastSpecializations(min);
    }

    @GetMapping("/{id}/appointments")
    public List<Appointment> appointments(@PathVariable Long id) {
        return doctorService.findAppointments(id);
    }

    // D1 – doctor's agenda for a day
    @GetMapping("/{doctorId}/agenda")
    public List<Appointment> agenda(@PathVariable Long doctorId,
                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return appointmentService.findAgenda(doctorId, date);
    }

    @GetMapping("/{id}/patients")
    public List<Patient> patients(@PathVariable Long id) {
        return doctorService.findPatients(id);
    }

    // Bonus – doctor workload by date range
    @GetMapping("/workload")
    public List<Object[]> workload(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return appointmentService.doctorWorkload(
                start.atStartOfDay(), end.plusDays(1).atStartOfDay());
    }
}
