package clinicmanagement1.clinicmanagement.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import clinicmanagement1.clinicmanagement.entity.Office;
import clinicmanagement1.clinicmanagement.service.OfficeService;
import org.springframework.beans.factory.annotation.Autowired;

import clinicmanagement1.clinicmanagement.exception.ResourceNotFoundException;
import clinicmanagement1.clinicmanagement.repository.OfficeRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Offices are created/updated/deleted together with their doctor, so this is read-only. */
@RestController
@RequestMapping("/api/offices")
public class OfficeController {
    @Autowired
    private OfficeService officeService;

    @PostMapping
    public ResponseEntity<Office> createOffice(@RequestBody Office office) {
        Office savedOffice = officeRepository.save(office);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedOffice);
    }

    @GetMapping("/free")
    public ResponseEntity<List<Office>> getFreeOffices() {
        List<Office> freeOffices = officeService.getFreeOffices();
        return ResponseEntity.ok(freeOffices);
    }

    private final OfficeRepository officeRepository;

    public OfficeController(OfficeRepository officeRepository) {
        this.officeRepository = officeRepository;
    }

    @GetMapping
    public List<Office> getAll() {
        return officeRepository.findAll();
    }

    @GetMapping("/{id}")
    public Office getById(@PathVariable Long id) {
        return officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office", id));
    }
}
