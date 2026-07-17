package com.gridpulse.controller;

import com.gridpulse.entity.Technician;
import com.gridpulse.service.TechnicianService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/technicians")
public class TechnicianController {

    @Autowired
    private TechnicianService technicianService;

    @GetMapping
    public List<Technician> getAllTechnicians() {
        return technicianService.getAllTechnicians();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Technician> getTechnicianById(@PathVariable Long id) {
        return ResponseEntity.ok(technicianService.getTechnicianById(id));
    }

    @PostMapping
    public Technician createTechnician(@Valid @RequestBody Technician technician) {
        return technicianService.createTechnician(technician);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Technician> updateTechnician(@PathVariable Long id, @Valid @RequestBody Technician technicianDetails) {
        return ResponseEntity.ok(technicianService.updateTechnician(id, technicianDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTechnician(@PathVariable Long id) {
        technicianService.deleteTechnician(id);
        return ResponseEntity.ok().build();
    }
}
