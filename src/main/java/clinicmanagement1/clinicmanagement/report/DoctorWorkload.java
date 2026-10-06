package clinicmanagement1.clinicmanagement.report;

/** Result row of the "appointments per doctor" query (not a DTO for CRUD, just a query result). */
public record DoctorWorkload(Long doctorId, String doctorName, Long totalAppointments) {
}
