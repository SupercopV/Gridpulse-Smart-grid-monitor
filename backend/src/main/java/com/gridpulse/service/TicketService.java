package com.gridpulse.service;

import com.gridpulse.config.NotificationWebSocketHandler;
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
        
        
        updateSubstationStatus(ticket.getSubstationId(), "FAULT");

        return ticketRepository.save(ticket);
    }

    @Transactional
    public RepairTicket updateTicket(Long id, RepairTicket details) {
        RepairTicket ticket = getTicketById(id);
        
        
        String oldStatus = ticket.getStatus();
        String newStatus = details.getStatus();

        if (details.getRepairNotes() != null) {
            ticket.setRepairNotes(details.getRepairNotes());
        }

        
        if (details.getTechnicianId() != null && !details.getTechnicianId().equals(ticket.getTechnicianId())) {
            
            if (ticket.getTechnicianId() != null) {
                releaseTechnician(ticket.getTechnicianId());
            }

            Technician tech = technicianRepository.findById(details.getTechnicianId())
                    .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + details.getTechnicianId()));
            
            ticket.setTechnicianId(tech.getId());
            ticket.setTechnicianName(tech.getName());
            
            
            tech.setCurrentJobs(tech.getCurrentJobs() + 1);
            tech.setAvailability("ON_JOB");
            technicianRepository.save(tech);

            ticket.setStatus("ASSIGNED");
            NotificationWebSocketHandler.broadcast("TICKET_ASSIGNED", "Ticket assigned to technician " + ticket.getTechnicianName(), ticket);
        }


        if (newStatus != null && !newStatus.equals(oldStatus)) {
            ticket.setStatus(newStatus);

            if ("IN_PROGRESS".equals(newStatus)) {
                if (ticket.getTechnicianId() != null) {
                    updateSubstationStatus(ticket.getSubstationId(), "WARNING");
                    NotificationWebSocketHandler.broadcast("TECHNICIAN_ACCEPTED", "Technician " + ticket.getTechnicianName() + " has started repair.", ticket);
                }
            } else if ("COMPLETED".equals(newStatus)) {
                ticket.setCompletedAt(LocalDateTime.now());
                NotificationWebSocketHandler.broadcast("REPAIR_COMPLETED", "Repair completed successfully for " + ticket.getSubstationName(), ticket);

                
                if (ticket.getTechnicianId() != null) {
                    releaseTechnician(ticket.getTechnicianId());
                }


                
                RepairHistory history = RepairHistory.builder()
                        .substationId(ticket.getSubstationId())
                        .substationName(ticket.getSubstationName())
                        .faultResolved(ticket.getProbableFault())
                        .technicianName(ticket.getTechnicianName())
                        .completedAt(LocalDateTime.now())
                        .notes(ticket.getRepairNotes() != null ? ticket.getRepairNotes() : "Repair completed successfully.")
                        .build();
                repairHistoryRepository.save(history);

                
                List<Alert> activeAlerts = alertRepository.findBySubstationIdOrderByTimestampDesc(ticket.getSubstationId());
                for (Alert alert : activeAlerts) {
                    if ("ACTIVE".equals(alert.getStatus())) {
                        alert.setStatus("RESOLVED");
                        alertRepository.save(alert);
                    }
                }

                
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

    public List<RepairTicket> getTicketsForTechnician(Long userId) {
        Technician tech = technicianRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Technician not found for user ID: " + userId));
        return ticketRepository.findByTechnicianId(tech.getId());
    }

    @Transactional
    public RepairTicket updateTicketWorkflow(Long ticketId, Long userId, String status, String notes) {
        Technician tech = technicianRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found for user ID: " + userId));

        RepairTicket ticket = getTicketById(ticketId);
        
        if (!tech.getId().equals(ticket.getTechnicianId())) {
            throw new org.springframework.security.access.AccessDeniedException("You are not assigned to this repair ticket.");
        }

        String oldStatus = ticket.getStatus();
        String newStatus = status.toUpperCase();
        ticket.setStatus(newStatus);

        if (notes != null) {
            ticket.setRepairNotes(notes);
        }

        if ("ACCEPTED".equals(newStatus)) {
            tech.setAvailability("BUSY");
            technicianRepository.save(tech);
            NotificationWebSocketHandler.broadcast("TECHNICIAN_ACCEPTED", "Technician " + tech.getName() + " accepted repair ticket for " + ticket.getSubstationName(), ticket);
        } else if ("REJECTED".equals(newStatus)) {
            tech.setCurrentJobs(Math.max(0, tech.getCurrentJobs() - 1));
            tech.setAvailability("AVAILABLE");
            technicianRepository.save(tech);

            ticket.setStatus("OPEN");
            ticket.setTechnicianId(null);
            ticket.setTechnicianName(null);
            ticket.setRepairNotes("Ticket rejected by " + tech.getName() + ". Reason: " + (notes != null ? notes : "None"));
            NotificationWebSocketHandler.broadcast("TICKET_UPDATED", "Ticket rejected by technician " + tech.getName(), ticket);
        } else if ("TRAVELLING".equals(newStatus)) {
            NotificationWebSocketHandler.broadcast("TICKET_UPDATED", "Technician " + tech.getName() + " is travelling to " + ticket.getSubstationName(), ticket);
        } else if ("ON_SITE".equals(newStatus)) {
            NotificationWebSocketHandler.broadcast("TICKET_UPDATED", "Technician " + tech.getName() + " has arrived on site at " + ticket.getSubstationName(), ticket);
        } else if ("IN_PROGRESS".equals(newStatus)) {
            updateSubstationStatus(ticket.getSubstationId(), "WARNING");
            NotificationWebSocketHandler.broadcast("TICKET_UPDATED", "Technician " + tech.getName() + " started repair on " + ticket.getSubstationName(), ticket);
        } else if ("COMPLETED".equals(newStatus)) {
            ticket.setCompletedAt(LocalDateTime.now());
            
            tech.setCurrentJobs(Math.max(0, tech.getCurrentJobs() - 1));
            tech.setAvailability("AVAILABLE");
            technicianRepository.save(tech);

            RepairHistory history = RepairHistory.builder()
                    .substationId(ticket.getSubstationId())
                    .substationName(ticket.getSubstationName())
                    .faultResolved(ticket.getProbableFault())
                    .technicianName(ticket.getTechnicianName())
                    .completedAt(LocalDateTime.now())
                    .notes(ticket.getRepairNotes() != null ? ticket.getRepairNotes() : "Repair completed successfully.")
                    .build();
            repairHistoryRepository.save(history);

            List<Alert> activeAlerts = alertRepository.findBySubstationIdOrderByTimestampDesc(ticket.getSubstationId());
            for (Alert alert : activeAlerts) {
                if ("ACTIVE".equals(alert.getStatus())) {
                    alert.setStatus("RESOLVED");
                    alertRepository.save(alert);
                }
            }

            updateSubstationStatus(ticket.getSubstationId(), "HEALTHY");
            NotificationWebSocketHandler.broadcast("REPAIR_COMPLETED", "Repair completed successfully for " + ticket.getSubstationName(), ticket);
        }

        return ticketRepository.save(ticket);
    }
}

