package com.gridpulse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "technicians")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Technician {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String skills; // e.g. "Transformer Maintenance", "Substation Automation", "Overhead Cables"

    @Column(nullable = false)
    private String availability; // AVAILABLE, ON_JOB, OFF_DUTY

    @Builder.Default
    private Integer currentJobs = 0;

    private String phone;
}
