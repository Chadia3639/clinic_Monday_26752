package clinicmanagement1.clinicmanagement.controller;

import clinicmanagement1.clinicmanagement.entity.Appointment;
import clinicmanagement1.clinicmanagement.entity.AppointmentStatus;
import clinicmanagement1.clinicmanagement.report.DoctorWorkload;
import clinicmanagement1.clinicmanagement.service.AppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /** POST /api/appointments?patientId=1&doctorId=2  body: {appointmentDate, reason, status?} */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Appointment book(@RequestParam Long patientId,
                            @RequestParam Long doctorId,
                            @RequestBody Appointment appointment) {
        return appointmentService.book(patientId, doctorId, appointment);
    }

    @GetMapping
    public List<Appointment> getAll() {
        return appointmentService.findAll();
    }

    @GetMapping("/{id}")
    public Appointment getById(@PathVariable Long id) {
        return appointmentService.findById(id);
    }

    @PutMapping("/{id}")
    public Appointment update(@PathVariable Long id, @RequestBody Appointment appointment) {
        return appointmentService.update(id, appointment);
    }

    /** PATCH /api/appointments/5/status?status=CANCELLED */
    @PatchMapping("/{id}/status")
    public Appointment updateStatus(@PathVariable Long id, @RequestParam AppointmentStatus status) {
        return appointmentService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        appointmentService.delete(id);
    }

    // ---------------- Queries ----------------

    /** GET /api/appointments/by-status?status=SCHEDULED[&doctorId=1] */
    @GetMapping("/by-status")
    public List<Appointment> byStatus(@RequestParam AppointmentStatus status,
                                      @RequestParam(required = false) Long doctorId) {
        return doctorId == null
                ? appointmentService.findByStatus(status)
                : appointmentService.findByDoctorAndStatus(doctorId, status);
    }

    /** GET /api/appointments/range?from=2026-10-01T00:00:00&to=2026-10-31T23:59:59 */
    @GetMapping("/range")
    public List<Appointment> range(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return appointmentService.findBetween(from, to);
    }

    /** GET /api/appointments/workload : number of appointments per doctor. */
    @GetMapping("/workload")
    public List<DoctorWorkload> workload() {
        return appointmentService.workloadPerDoctor();
    }
}
