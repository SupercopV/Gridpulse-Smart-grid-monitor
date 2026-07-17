package com.gridpulse.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DiagnosisDto {
    private String probableFault;
    private Double confidenceScore;
    private String recommendedRepair;
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL
    private Integer etaHours; // Estimated repair duration in hours
    private String technicianSpecialization; // Specialization required for repair
}
