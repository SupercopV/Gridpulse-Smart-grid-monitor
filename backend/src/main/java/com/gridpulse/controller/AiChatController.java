package com.gridpulse.controller;

import com.gridpulse.entity.*;
import com.gridpulse.repository.*;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiChatController {

    @Autowired
    private SubstationRepository substationRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Value("${gridpulse.groq.api-key:}")
    private String groqApiKey;

    @Value("${gridpulse.groq.model:llama-3.3-70b-versatile}")
    private String groqModel;

    @Value("${gridpulse.groq.url:https://api.groq.com/openai/v1}")
    private String groqUrl;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("response", "Please enter a message."));
        }

        
        List<Substation> allSubs = substationRepository.findAll();
        List<Substation> faultySubs = allSubs.stream().filter(s -> "FAULT".equals(s.getStatus())).collect(Collectors.toList());
        List<Alert> activeAlerts = alertRepository.findAll().stream().filter(a -> "ACTIVE".equals(a.getStatus())).collect(Collectors.toList());
        List<RepairTicket> activeTickets = ticketRepository.findAll().stream().filter(t -> !"COMPLETED".equals(t.getStatus())).collect(Collectors.toList());
        List<Technician> freeTechs = technicianRepository.findByAvailability("AVAILABLE");

        
        String aiResponse;
        if (groqApiKey == null || groqApiKey.trim().isEmpty() || groqApiKey.equals("${GROQ_API_KEY}")) {
            aiResponse = generateLocalHeuristicResponse(userMessage.toLowerCase(), allSubs, faultySubs, activeAlerts, activeTickets, freeTechs);
        } else {
            aiResponse = callGroqChat(userMessage, allSubs, faultySubs, activeAlerts, activeTickets, freeTechs);
        }

        return ResponseEntity.ok(Map.of("response", aiResponse));
    }

    private String callGroqChat(String userPrompt, List<Substation> allSubs, List<Substation> faultySubs,
                                List<Alert> activeAlerts, List<RepairTicket> activeTickets, List<Technician> freeTechs) {
        try {
            
            String systemContext = String.format(
                    "You are GridPulse AI Assistant, an expert smart-grid control center operator.\n" +
                    "Here is the CURRENT live state of the grid:\n" +
                    "- Total Grids/Substations: %d\n" +
                    "- Substations in FAULT state: %d (Grids: %s)\n" +
                    "- Active Electrical Alerts: %d (%s)\n" +
                    "- Open Repair Tickets: %d (%s)\n" +
                    "- Available Field Technicians: %d (%s)\n\n" +
                    "Respond to the user prompt concisely. Be helpful, professional, and reference the live state parameters above when answering.",
                    allSubs.size(),
                    faultySubs.size(),
                    faultySubs.stream().map(Substation::getName).collect(Collectors.joining(", ")),
                    activeAlerts.size(),
                    activeAlerts.stream().map(Alert::getMessage).collect(Collectors.joining("; ")),
                    activeTickets.size(),
                    activeTickets.stream().map(t -> t.getSubstationName() + ":" + t.getProbableFault()).collect(Collectors.joining("; ")),
                    freeTechs.size(),
                    freeTechs.stream().map(Technician::getName).collect(Collectors.joining(", "))
            );

            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .apiKey(groqApiKey)
                    .baseUrl(groqUrl)
                    .modelName(groqModel)
                    .temperature(0.5)
                    .build();

            return chatModel.generate(systemContext + "\nUser Question: " + userPrompt);
        } catch (Exception e) {
            System.err.println("Groq Chat call failed: " + e.getMessage());
            return generateLocalHeuristicResponse(userPrompt.toLowerCase(), allSubs, faultySubs, activeAlerts, activeTickets, freeTechs);
        }
    }

    private String generateLocalHeuristicResponse(String prompt, List<Substation> allSubs, List<Substation> faultySubs,
                                                 List<Alert> activeAlerts, List<RepairTicket> activeTickets, List<Technician> freeTechs) {
        
        
        if (prompt.contains("why did") || prompt.contains("failed") || prompt.contains("fail")) {
            
            for (Substation sub : allSubs) {
                String subName = sub.getName().toLowerCase();
                if (prompt.contains(subName) || prompt.contains(subName.replace(" grid", "")) || prompt.contains("substation")) {
                    if ("FAULT".equals(sub.getStatus())) {
                        Optional<RepairTicket> ticketOpt = activeTickets.stream().filter(t -> t.getSubstationId().equals(sub.getId())).findFirst();
                        if (ticketOpt.isPresent()) {
                            RepairTicket ticket = ticketOpt.get();
                            return String.format("The **%s** failed because of an anomaly in its live electrical telemetry. " +
                                    "Our AI Diagnoser identifies the probable cause as: **%s** (Confidence: %.1f%%). " +
                                    "The recommended action is: *%s*. A repair ticket is currently assigned to **%s**.",
                                    sub.getName(), ticket.getProbableFault(), ticket.getConfidenceScore(),
                                    ticket.getRecommendedRepair(), ticket.getTechnicianName() != null ? ticket.getTechnicianName() : "a queue");
                        }
                        return String.format("The **%s** is currently in a FAULT state due to telemetry crossing threshold limits. We are generating diagnostics.", sub.getName());
                    } else {
                        return String.format("The **%s** is currently **HEALTHY** and running within normal parameters.", sub.getName());
                    }
                }
            }
            if (!faultySubs.isEmpty()) {
                Substation firstFaulty = faultySubs.get(0);
                Optional<RepairTicket> ticketOpt = activeTickets.stream().filter(t -> t.getSubstationId().equals(firstFaulty.getId())).findFirst();
                if (ticketOpt.isPresent()) {
                    RepairTicket ticket = ticketOpt.get();
                    return String.format("The grid **%s** is currently failed due to **%s**. " +
                            "Root cause is diagnosed as: *%s*. The ticket is assigned to **%s**.",
                            firstFaulty.getName(), ticket.getProbableFault(), ticket.getRecommendedRepair(),
                            ticket.getTechnicianName() != null ? ticket.getTechnicianName() : "unassigned");
                }
            }
            return "No grid failure details found for your query. All systems are operating normally.";
        }

        
        if (prompt.contains("summarize") || prompt.contains("faults") || prompt.contains("incidents")) {
            if (activeTickets.isEmpty()) {
                return "There are no active faults or outages reported on the Indian State Grid today! All grids are functioning perfectly.";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("### Live Fault Summary:\n");
            sb.append(String.format("There are currently **%d active fault(s)** requiring maintenance:\n\n", activeTickets.size()));
            for (int i = 0; i < activeTickets.size(); i++) {
                RepairTicket t = activeTickets.get(i);
                sb.append(String.format("%d. **%s**: %s (Severity: *%s*). Assigned to: **%s** (ETA: %d Hours).\n",
                        i + 1, t.getSubstationName(), t.getProbableFault(), t.getPriority(),
                        t.getTechnicianName() != null ? t.getTechnicianName() : "Unassigned", t.getEtaHours()));
            }
            return sb.toString();
        }

        
        if (prompt.contains("critical") || prompt.contains("show substations") || prompt.contains("grids")) {
            if (faultySubs.isEmpty()) {
                return "All Indian State Grid substations are currently **HEALTHY** (Green) and stable.";
            }
            String names = faultySubs.stream().map(s -> "**" + s.getName() + "** (" + s.getLocation() + ")").collect(Collectors.joining(", "));
            return String.format("The following substations are currently in a **FAULT** state and require attention: %s.", names);
        }

        
        if (prompt.contains("technician") || prompt.contains("free") || prompt.contains("available")) {
            if (freeTechs.isEmpty()) {
                return "All field technicians are currently dispatched on active repairs (`ON_JOB`). No technicians are free right now.";
            }
            String names = freeTechs.stream().map(t -> "**" + t.getName() + "** (" + t.getSkills() + ")").collect(Collectors.joining("\n- "));
            return "The following technicians are currently **AVAILABLE** for dispatch:\n- " + names;
        }

        
        if (prompt.contains("report") || prompt.contains("generate")) {
            double uptimePercentage = ((double) (allSubs.size() - faultySubs.size()) / allSubs.size()) * 100.0;
            return String.format("### GridPulse Daily Operations Report\n" +
                    "- **Grid Uptime**: %.1f%%\n" +
                    "- **Total State Grids Monitored**: %d\n" +
                    "- **Healthy Grids**: %d\n" +
                    "- **Active Outages**: %d\n" +
                    "- **Open Maintenance Jobs**: %d\n" +
                    "- **Technicians Available**: %d\n\n" +
                    "Operational Status: *%s*",
                    uptimePercentage, allSubs.size(), allSubs.size() - faultySubs.size(),
                    faultySubs.size(), activeTickets.size(), freeTechs.size(),
                    faultySubs.isEmpty() ? "Nominal. No actions required." : "Degraded. Critical repairs pending.");
        }

        
        return String.format("Hello! I am the **GridPulse Intelligent Assistant**. " +
                "I am monitoring all %d Indian State Grids. Currently, **%d grids** are in a FAULT state. " +
                "We have **%d free technician(s)** available. " +
                "Ask me anything about today's faults, reports, or free technicians!",
                allSubs.size(), faultySubs.size(), freeTechs.size());
    }
}
