package clinicmanagement1.clinicmanagement.service;

import clinicmanagement1.clinicmanagement.entity.Appointment;
import clinicmanagement1.clinicmanagement.entity.Doctor;
import clinicmanagement1.clinicmanagement.entity.Office;
import clinicmanagement1.clinicmanagement.entity.Patient;
import clinicmanagement1.clinicmanagement.entity.Specialization;
import clinicmanagement1.clinicmanagement.exception.BadRequestException;
import clinicmanagement1.clinicmanagement.exception.ConflictException;
import clinicmanagement1.clinicmanagement.exception.ResourceNotFoundException;
import clinicmanagement1.clinicmanagement.repository.AppointmentRepository;
import clinicmanagement1.clinicmanagement.repository.DoctorRepository;
import clinicmanagement1.clinicmanagement.repository.OfficeRepository;
import clinicmanagement1.clinicmanagement.repository.PatientRepository;
import clinicmanagement1.clinicmanagement.repository.SpecializationRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final SpecializationRepository specializationRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final OfficeRepository officeRepository;

    @Autowired
    public DoctorService(DoctorRepository doctorRepository,
                         SpecializationRepository specializationRepository,
                         AppointmentRepository appointmentRepository,
                         PatientRepository patientRepository,
                         OfficeRepository officeRepository) {
        this.doctorRepository = doctorRepository;
        this.specializationRepository = specializationRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.officeRepository = officeRepository;
    }


    @Transactional
    public Doctor create(Doctor input) {
        if (input.getOffice() == null) {
            throw new BadRequestException("A doctor must have exactly one office");
        }
        input.setId(null);
        input.getOffice().setId(null);
        input.setSpecializations(resolveSpecializations(input.getSpecializations()));
        return doctorRepository.save(input);
    }

    @Transactional(readOnly = true)
    public List<Doctor> findAll() {
        return doctorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Doctor findById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
    }

    @Transactional
    public Doctor update(Long id, Doctor input) {
        Doctor existing = findById(id);

        if (input.getFirstName() != null) existing.setFirstName(input.getFirstName());
        if (input.getLastName() != null) existing.setLastName(input.getLastName());
        if (input.getEmail() != null) existing.setEmail(input.getEmail());
        if (input.getPhone() != null) existing.setPhone(input.getPhone());

        if (input.getOffice() != null) {
            Office office = existing.getOffice();
            if (input.getOffice().getRoomNumber() != null) office.setRoomNumber(input.getOffice().getRoomNumber());
            if (input.getOffice().getFloorNumber() != null) office.setFloorNumber(input.getOffice().getFloorNumber());
            if (input.getOffice().getBuilding() != null) office.setBuilding(input.getOffice().getBuilding());
        }

        if (input.getSpecializations() != null && !input.getSpecializations().isEmpty()) {
            existing.setSpecializations(resolveSpecializations(input.getSpecializations()));
        }
        return doctorRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Doctor existing = findById(id);
        if (appointmentRepository.existsByDoctorId(id)) {
            throw new ConflictException("Doctor " + id + " still has appointments and cannot be deleted");
        }
        doctorRepository.delete(existing);
    }


    @Transactional
    public String addSpecializationToDoctor(Long doctorId, Long specializationId) {
        if (!doctorRepository.existsById(doctorId)) {
            return "The doctor with that id does not exist";
        }
        if (!specializationRepository.existsById(specializationId)) {
            return "That specialization does not exist";
        }

        Doctor doctor = doctorRepository.findByIdWithSpecializations(doctorId).get();
        Specialization specialization = specializationRepository.findById(specializationId).get();

        if (doctor.getSpecializations().contains(specialization)) {
            return "The doctor already has that specialization";
        }

        doctor.getSpecializations().add(specialization);
        specialization.getDoctors().add(doctor);
        doctorRepository.save(doctor);

        return "Specialization added to the doctor";
    }

    @Transactional
    public String removeSpecializationFromDoctor(Long doctorId, Long specializationId) {
        if (!doctorRepository.existsById(doctorId)) {
            return "The doctor with that id does not exist";
        }
        if (!specializationRepository.existsById(specializationId)) {
            return "That specialization does not exist";
        }

        Doctor doctor = doctorRepository.findByIdWithSpecializations(doctorId).get();
        Specialization specialization = specializationRepository.findById(specializationId).get();

        if (!doctor.getSpecializations().contains(specialization)) {
            return "The doctor does not have that specialization";
        }

        doctor.getSpecializations().remove(specialization);
        specialization.getDoctors().remove(doctor);
        doctorRepository.save(doctor);

        return "Specialization removed from the doctor";
    }

    @Transactional
    public String moveDoctorToOffice(Long doctorId, Long officeNumber) {
        if (!doctorRepository.existsById(doctorId)) {
            return "The doctor with that id does not exist";
        }
        
       Office office = officeRepository.findByRoomNumber(String.valueOf(officeNumber)).orElse(null);
        if (office == null) {
            return "The office with that office number does not exist";
        }

       if (doctorRepository.existsByOfficeId(officeNumber)) {
            return "That office is taken by other doctor";
        }

        Doctor doctor = doctorRepository.findById(doctorId).get();
        doctor.setOffice(office);
        doctorRepository.save(doctor);

        return "Office changed successfully";
    }

    

    @Transactional(readOnly = true)
    public List<Doctor> findBySpecializationName(String name) {
        return doctorRepository.findBySpecializations_NameIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public List<Doctor> searchByLastName(String lastName) {
        return doctorRepository.findByLastNameContainingIgnoreCase(lastName);
    }

    // D3
    @Transactional(readOnly = true)
    public List<Doctor> searchByName(String name) {
        return doctorRepository.searchByName(name);
    }

    @Transactional(readOnly = true)
    public Doctor findByRoomNumber(String roomNumber) {
        return doctorRepository.findByOffice_RoomNumber(roomNumber)
                .orElseThrow(() -> new ResourceNotFoundException("No doctor is assigned to office " + roomNumber));
    }

    @Transactional(readOnly = true)
    public List<Doctor> findWithAtLeastSpecializations(int n) {
        return doctorRepository.findWithAtLeastSpecializations(n);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAppointments(Long doctorId) {
        findById(doctorId);
        return appointmentRepository.findByDoctorId(doctorId);
    }

    @Transactional(readOnly = true)
    public List<Patient> findPatients(Long doctorId) {
        findById(doctorId);
        return patientRepository.findPatientsByDoctorId(doctorId);
    }

    // ----------------------------------------------------------------- helpers

    private Set<Specialization> resolveSpecializations(Set<Specialization> requested) {
        Set<Specialization> resolved = new HashSet<>();
        if (requested == null) {
            return resolved;
        }
        for (Specialization s : requested) {
            if (s.getId() == null) {
                throw new BadRequestException("Each specialization must be referenced by its id");
            }
            resolved.add(specializationRepository.findById(s.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Specialization", s.getId())));
        }
        return resolved;
    }
}