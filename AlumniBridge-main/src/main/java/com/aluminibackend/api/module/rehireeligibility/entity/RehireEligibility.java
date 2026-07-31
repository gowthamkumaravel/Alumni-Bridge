package com.aluminibackend.api.module.rehireeligibility.entity;

import com.aluminibackend.api.module.alumniprofile.entity.AlumniProfile;
import com.aluminibackend.api.module.rehireeligibility.enums.EligibleForRehire;
import com.aluminibackend.api.module.rehireeligibility.enums.RehireEligibilityStatus;
import com.aluminibackend.api.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rehire_eligibilities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RehireEligibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eligibilityId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alumni_id", nullable = false, unique = true)
    private AlumniProfile alumni;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EligibleForRehire eligibleForRehire;

    private Integer coolingOffPeriodMonths;

    private LocalDate eligibilityReviewDate;

    @Column(columnDefinition = "TEXT")
    private String eligibilityReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id", nullable = false)
    private User reviewedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RehireEligibilityStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (eligibilityReviewDate == null) {
            eligibilityReviewDate = LocalDate.now();
        }

        if (status == null) {
            status = RehireEligibilityStatus.UNDER_REVIEW;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}