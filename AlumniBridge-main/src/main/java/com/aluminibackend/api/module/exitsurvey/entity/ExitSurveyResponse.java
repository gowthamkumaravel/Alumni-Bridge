package com.aluminibackend.api.module.exitsurvey.entity;

import com.aluminibackend.api.module.offboarding.entity.OffboardingCase;
import com.aluminibackend.api.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "exit_survey_responses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExitSurveyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long responseId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false, unique = true)
    private OffboardingCase offboardingCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    private LocalDateTime submittedDate;

    private Integer managerSatisfactionScore;

    private Integer teamCultureScore;

    private Integer careerGrowthScore;

    private Integer compensationScore;

    private Integer overallScore;

    @Column(columnDefinition = "TEXT")
    private String openTextFeedback;

    private Boolean anonymised;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (submittedDate == null) {
            submittedDate = LocalDateTime.now();
        }

        if (anonymised == null) {
            anonymised = false;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}