package com.aluminibackend.api.module.alumnireport.entity;

import com.aluminibackend.api.module.alumnireport.enums.ReportMetric;
import com.aluminibackend.api.module.alumnireport.enums.ReportScope;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alumni_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumniReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportScope scope;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportMetric metric;

    @Column(columnDefinition = "TEXT")
    private String metricValue;

    private LocalDateTime generatedDate;

    @PrePersist
    public void onCreate() {
        if (generatedDate == null) {
            generatedDate = LocalDateTime.now();
        }
    }
}