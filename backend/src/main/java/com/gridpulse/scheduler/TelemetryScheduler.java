package com.gridpulse.scheduler;

import com.gridpulse.entity.Substation;
import com.gridpulse.entity.Telemetry;
import com.gridpulse.repository.SubstationRepository;
import com.gridpulse.service.TelemetryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Component
public class TelemetryScheduler {

    @Autowired
    private SubstationRepository substationRepository;

    @Autowired
    private TelemetryService telemetryService;

    private final Random random = new Random();

    // Map to keep track of substations that are temporarily forced into anomaly state
    private final java.util.concurrent.ConcurrentHashMap<Long, String> forcedAnomalies = new java.util.concurrent.ConcurrentHashMap<>();

    public void forceAnomaly(Long substationId, String type) {
        forcedAnomalies.put(substationId, type);
    }

    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void generateTelemetry() {
        List<Substation> substations = substationRepository.findAll();
        if (substations.isEmpty()) {
            return;
        }

        for (Substation sub : substations) {
            double voltage;
            double current;
            double temperature;
            double frequency = 49.5 + (random.nextDouble() * 1.0); // 49.5 - 50.5 Hz

            boolean triggerAnomaly = false;
            String anomalyType = "";

            // Check if manual anomaly is injected, or 5% random chance if substation is currently healthy
            if (forcedAnomalies.containsKey(sub.getId())) {
                triggerAnomaly = true;
                anomalyType = forcedAnomalies.remove(sub.getId());
            } else if ("HEALTHY".equals(sub.getStatus()) && random.nextInt(100) < 5) {
                triggerAnomaly = true;
                // Randomly choose an anomaly type
                int choice = random.nextInt(3);
                if (choice == 0) anomalyType = "VOLTAGE_SAG";
                else if (choice == 1) anomalyType = "OVERCURRENT";
                else anomalyType = "OVERHEATING";
            }

            if (triggerAnomaly) {
                // Generate anomalous readings
                if ("VOLTAGE_SAG".equals(anomalyType)) {
                    voltage = 100.0 + (random.nextDouble() * 40.0); // 100-140V
                    current = 10.0 + (random.nextDouble() * 10.0);  // Normal current
                    temperature = 40.0 + (random.nextDouble() * 10.0); // Normal temp
                } else if ("OVERCURRENT".equals(anomalyType)) {
                    voltage = 220.0 + (random.nextDouble() * 20.0); // Normal voltage
                    current = 35.0 + (random.nextDouble() * 15.0);  // 35-50A
                    temperature = 45.0 + (random.nextDouble() * 15.0); // Normal-ish temp
                } else { // OVERHEATING
                    voltage = 220.0 + (random.nextDouble() * 20.0); // Normal voltage
                    current = 12.0 + (random.nextDouble() * 8.0);   // Normal current
                    temperature = 80.0 + (random.nextDouble() * 20.0); // 80-100°C
                }
                frequency = 46.0 + (random.nextDouble() * 2.0); // 46 - 48 Hz
            } else {
                // Generate normal readings
                voltage = 220.0 + (random.nextDouble() * 20.0);    // 220-240V
                current = 5.0 + (random.nextDouble() * 15.0);       // 5-20A
                temperature = 30.0 + (random.nextDouble() * 20.0);   // 30-50°C
            }

            double power = (voltage * current * 0.9) / 1000.0; // kW (assuming 0.9 power factor)

            Telemetry telemetry = Telemetry.builder()
                    .substationId(sub.getId())
                    .voltage(Math.round(voltage * 10.0) / 10.0)
                    .current(Math.round(current * 10.0) / 10.0)
                    .temperature(Math.round(temperature * 10.0) / 10.0)
                    .frequency(Math.round(frequency * 10.0) / 10.0)
                    .power(Math.round(power * 10.0) / 10.0)
                    .timestamp(LocalDateTime.now())
                    .build();

            try {
                telemetryService.saveTelemetry(telemetry);
            } catch (Exception e) {
                System.err.println("Scheduler failed to save telemetry: " + e.getMessage());
            }
        }
    }
}
