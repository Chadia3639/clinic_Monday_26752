package clinicmanagement1.clinicmanagement.service;

import clinicmanagement1.clinicmanagement.entity.Office;
import clinicmanagement1.clinicmanagement.exception.BadRequestException;
import clinicmanagement1.clinicmanagement.exception.ConflictException;
import clinicmanagement1.clinicmanagement.exception.ResourceNotFoundException;
import clinicmanagement1.clinicmanagement.repository.OfficeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OfficeService {

    private OfficeRepository officeRepository;
    public List<Office> getFreeOffices() {
        return officeRepository.findFreeOffices();
    }

    public OfficeService(OfficeRepository officeRepository) {
        this.officeRepository = officeRepository;
    }

    @Transactional
    public Office create(Office input) {
        if (input.getRoomNumber() == null || input.getRoomNumber().isBlank()) {
            throw new BadRequestException("roomNumber is required");
        }
        input.setId(null);
        return officeRepository.save(input);
    }

    @Transactional(readOnly = true)
    public List<Office> findAll() {
        return officeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Office findById(Long id) {
        return officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office", id));
    }

    @Transactional
    public Office update(Long id, Office input) {
        Office existing = findById(id);
        if (input.getRoomNumber() != null) existing.setRoomNumber(input.getRoomNumber());
        if (input.getFloorNumber() != null) existing.setFloorNumber(input.getFloorNumber());
        if (input.getBuilding() != null) existing.setBuilding(input.getBuilding());
        return officeRepository.save(existing);
    }

    /** An office that still belongs to a doctor cannot be deleted (each doctor needs exactly one office). */
    @Transactional
    public void delete(Long id) {
        Office existing = findById(id);
        if (existing.getDoctor() != null) {
            throw new ConflictException("Office " + id + " is assigned to a doctor and cannot be deleted");
        }
        officeRepository.delete(existing);
    }
}
