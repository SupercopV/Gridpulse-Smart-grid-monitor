package com.gridpulse.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gridpulse.dto.DiagnosisDto;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AiFaultDiagnoser {

    @Value("${gridpulse.groq.api-key:}")
    private String groqApiKey;

    @Value("${gridpulse.groq.model:llama-3.3-70b-versatile}")
    private String groqModel;

    @Value("${gridpulse.groq.url:https://api.groq.com/openai/v1}")
    private String groqUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public DiagnosisDto diagnoseFault(Double voltage, Double current, Double temperature, Double frequency, List<String> repairHistory) {
        // Fallback or validation if Groq Key is missing
        if (groqApiKey == null || groqApiKey.trim().isEmpty() || groqApiKey.equals("${GROQ_API_KEY}")) {
            System.out.println("Groq API key not configured. Using local heuristic fault diagnosis.");
            return generateHeuristicDiagnosis(voltage, current, temperature, frequency, repairHistory);
        }

        try {
            System.out.println("Diagnosing fault using Groq AI (" + groqModel + ") via LangChain4j...");
            
            // Build OpenAiChatModel pointing to Groq's endpoint
            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .apiKey(groqApiKey)
                    .baseUrl(groqUrl)
                    .modelName(groqModel)
                    .temperature(0.1)
                    .build();

            // Prepare prompt
            String systemInstructions = "You are an electrical grid engineer. Analyze telemetry and repair history to diagnose grid faults. Return response ONLY as JSON.";
            String userPrompt = String.format(
                    "Telemetry:\n" +
                    "Voltage: %.1fV\n" +
                    "Current: %.1fA\n" +
                    "Temperature: %.1f°C\n" +
                    "Frequency: %.1fHz\n\n" +
                    "Substation Repair History:\n" +
                    "%s\n\n" +
                    "Diagnose the fault and return EXACTLY this JSON format (no markdown blocks, no explanation):\n" +
                    "{\n" +
                    "  \"probableFault\": \"detailed fault description\",\n" +
                    "  \"confidenceScore\": 0.0,\n" +
                    "  \"recommendedRepair\": \"clear recommendation steps\",\n" +
                    "  \"priority\": \"LOW\" or \"MEDIUM\" or \"HIGH\" or \"CRITICAL\",\n" +
                    "  \"etaHours\": 4,\n" +
                    "  \"technicianSpecialization\": \"Cable Repair\" or \"Transformer Specialist\" or \"Grid Automation\"\n" +
                    "}",
                    voltage, current, temperature, frequency,
                    repairHistory.isEmpty() ? "No recent repairs recorded." : String.join("\n", repairHistory)
            );

            String response = chatModel.generate(userPrompt);
            System.out.println("AI Raw Response: " + response);

            // Clean response string in case LLM wraps it in ```json ... ```
            String cleanJson = response.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();

            return objectMapper.readValue(cleanJson, DiagnosisDto.class);

        } catch (Exception e) {
            System.err.println("Groq AI Call failed: " + e.getMessage() + ". Falling back to local heuristics.");
            return generateHeuristicDiagnosis(voltage, current, temperature, frequency, repairHistory);
        }
    }

    private DiagnosisDto generateHeuristicDiagnosis(Double voltage, Double current, Double temperature, Double frequency, List<String> repairHistory) {
        String fault = "Normal Operation";
        double confidence = 99.0;
        String recommendation = "No repair required.";
        String priority = "LOW";
        int eta = 0;
        String spec = "Grid Automation";

        // Check anomaly parameters
        if (temperature > 75.0) {
            fault = "Thermal Overload & Cooling Pump Seizure";
            confidence = 88.0;
            recommendation = "Inspect transformer cooling system, clean radiator fins, and replace coolant circulation pump.";
            priority = "CRITICAL";
            eta = 4;
            spec = "Transformer Specialist";
        } else if (voltage < 170.0) {
            fault = "Feeder Cable Insulation Degradation (Voltage Sag)";
            confidence = 82.0;
            recommendation = "Conduct megger testing on primary distribution lines. Inspect terminal poles for sag or tracking.";
            priority = "HIGH";
            eta = 3;
            spec = "Cable Repair";
        } else if (current > 30.0) {
            fault = "Distribution Overload & Short Circuit Threat";
            confidence = 85.0;
            recommendation = "Rebalance grid phases, investigate load surges from industrial sectors, and verify relay trip thresholds.";
            priority = "CRITICAL";
            eta = 2;
            spec = "Grid Automation";
        } else if (frequency < 48.0 || frequency > 52.0) {
            fault = "Phase Frequency Synchronisation Error";
            confidence = 90.0;
            recommendation = "Re-calibrate phase sync circuits, adjust tap changer ratios, and inspect circuit breakers.";
            priority = "MEDIUM";
            eta = 2;
            spec = "Grid Automation";
        }

        // Adjust based on history context if any
        if (!repairHistory.isEmpty()) {
            for (String hist : repairHistory) {
                if (hist.toLowerCase().contains("transformer") && fault.contains("Transformer")) {
                    fault = "Recurrent Transformer Winding Degradation";
                    confidence = 95.0;
                    recommendation = "Full transformer replacement recommended. Winding insulation has failed repeatedly.";
                    eta = 6;
                }
            }
        }

        return DiagnosisDto.builder()
                .probableFault(fault)
                .confidenceScore(confidence)
                .recommendedRepair(recommendation)
                .priority(priority)
                .etaHours(eta)
                .technicianSpecialization(spec)
                .build();
    }
}
