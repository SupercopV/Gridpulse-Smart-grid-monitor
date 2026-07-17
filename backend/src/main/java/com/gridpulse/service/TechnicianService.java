package com.gridpulse.service;

import com.gridpulse.entity.Technician;
import com.gridpulse.exception.ResourceNotFoundException;
import com.gridpulse.repository.TechnicianRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TechnicianService {

    @Autowired
    private TechnicianRepository technicianRepository;

    public List<Technician> getAllTechnicians() {
        return technicianRepository.findAll();
    }

    public Technician getTechnicianById(Long id) {
        return technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + id));
    }

    public Technician createTechnician(Technician technician) {
        if (technician.getAvailability() == null) {
            technician.setAvailability("AVAILABLE");
        }
        if (technician.getCurrentJobs() == null) {
            technician.setCurrentJobs(0);
        }
        return technicianRepository.save(technician);
    }

    public Technician updateTechnician(Long id, Technician details) {
        Technician tech = getTechnicianById(id);
        tech.setName(details.getName());
        tech.setSkills(details.getSkills());
        tech.setAvailability(details.getAvailability());
        tech.setPhone(details.getPhone());
        if (details.getCurrentJobs() != null) {
            tech.setCurrentJobs(details.getCurrentJobs());
        }
        return technicianRepository.save(tech);
    }

    public void deleteTechnician(Long id) {
        Technician tech = getTechnicianById(id);
        technicianRepository.delete(tech);
    }
}
