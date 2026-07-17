package com.gridpulse.service;

import com.gridpulse.entity.*;
import com.gridpulse.exception.ResourceNotFoundException;
import com.gridpulse.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private RepairHistoryRepository repairHistoryRepository;

    @Autowired
    private SubstationRepository substationRepository;

    @Autowired
    private AlertRepository alertRepository;

    public List<RepairTicket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public RepairTicket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Repair ticket not found with id: " + id));
    }

    public RepairTicket createTicket(RepairTicket ticket) {
        if (ticket.getCreatedAt() == null) {
            ticket.setCreatedAt(LocalDateTime.now());
        }
        if (ticket.getStatus() == null) {
            ticket.setStatus("OPEN");
        }
        
        // Update substation status to FAULT if ticket created for it
        updateSubstationStatus(ticket.getSubstationId(), "FAULT");

        return ticketRepository.save(ticket);
    }

    @Transactional
    public RepairTicket updateTicket(Long id, RepairTicket details) {
        RepairTicket ticket = getTicketById(id);
        
        // Check status transition to handle technician allocation / completion
        String oldStatus = ticket.getStatus();
        String newStatus = details.getStatus();

        if (details.getRepairNotes() != null) {
            ticket.setRepairNotes(details.getRepairNotes());
        }

        // Technician Assignment
        if (details.getTechnicianId() != null && !details.getTechnicianId().equals(ticket.getTechnicianId())) {
            // Free old technician if existed
            if (ticket.getTechnicianId() != null) {
                releaseTechnician(ticket.getTechnicianId());
            }

            Technician tech = technicianRepository.findById(details.getTechnicianId())
                    .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + details.getTechnicianId()));
            
            ticket.setTechnicianId(tech.getId());
            ticket.setTechnicianName(tech.getName());
            
            // Assign to new tech
            tech.setCurrentJobs(tech.getCurrentJobs() + 1);
            tech.setAvailability("ON_JOB");
            technicianRepository.save(tech);

            ticket.setStatus("ASSIGNED");
        }

        if (newStatus != null && !newStatus.equals(oldStatus)) {
            ticket.setStatus(newStatus);

            if ("IN_PROGRESS".equals(newStatus)) {
                if (ticket.getTechnicianId() != null) {
                    updateSubstationStatus(ticket.getSubstationId(), "WARNING");
                }
            } else if ("COMPLETED".equals(newStatus)) {
                ticket.setCompletedAt(LocalDateTime.now());

                // Release assigned technician
                if (ticket.getTechnicianId() != null) {
                    releaseTechnician(ticket.getTechnicianId());
                }

                // 1. Add to Repair History
                RepairHistory history = RepairHistory.builder()
                        .substationId(ticket.getSubstationId())
                        .substationName(ticket.getSubstationName())
                        .faultResolved(ticket.getProbableFault())
                        .technicianName(ticket.getTechnicianName())
                        .completedAt(LocalDateTime.now())
                        .notes(ticket.getRepairNotes() != null ? ticket.getRepairNotes() : "Repair completed successfully.")
                        .build();
                repairHistoryRepository.save(history);

                // 2. Resolve related Alerts
                List<Alert> activeAlerts = alertRepository.findBySubstationIdOrderByTimestampDesc(ticket.getSubstationId());
                for (Alert alert : activeAlerts) {
                    if ("ACTIVE".equals(alert.getStatus())) {
                        alert.setStatus("RESOLVED");
                        alertRepository.save(alert);
                    }
                }

                // 3. Mark Substation Healthy again
                updateSubstationStatus(ticket.getSubstationId(), "HEALTHY");
            }
        }

        return ticketRepository.save(ticket);
    }

    private void releaseTechnician(Long techId) {
        technicianRepository.findById(techId).ifPresent(tech -> {
            tech.setCurrentJobs(Math.max(0, tech.getCurrentJobs() - 1));
            if (tech.getCurrentJobs() == 0) {
                tech.setAvailability("AVAILABLE");
            }
            technicianRepository.save(tech);
        });
    }

    private void updateSubstationStatus(Long substationId, String status) {
        substationRepository.findById(substationId).ifPresent(sub -> {
            sub.setStatus(status);
            substationRepository.save(sub);
        });
    }
}
