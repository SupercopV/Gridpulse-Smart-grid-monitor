package com.gridpulse.controller;

import com.gridpulse.entity.RepairTicket;
import com.gridpulse.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @GetMapping
    public List<RepairTicket> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepairTicket> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @PostMapping
    public RepairTicket createTicket(@Valid @RequestBody RepairTicket ticket) {
        return ticketService.createTicket(ticket);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RepairTicket> updateTicket(@PathVariable Long id, @Valid @RequestBody RepairTicket ticketDetails) {
        return ResponseEntity.ok(ticketService.updateTicket(id, ticketDetails));
    }
}
