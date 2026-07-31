package com.aluminibackend.api.module.alumniprofile.entity;

import com.aluminibackend.api.module.alumniprofile.enums.AlumniStatus;
import com.aluminibackend.api.module.alumniprofile.enums.EngagementTier;
import com.aluminibackend.api.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "alumni_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumniProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alumniId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String formerEmployeeId;

    @Column(nullable = false)
    private String name;

    private String lastRole;

    private LocalDate departureDateFrom;

    private String currentOrganisation;

    private String currentRole;

    private String linkedInRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EngagementTier engagementTier;

    private Boolean communityOptIn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlumniStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (engagementTier == null) {
            engagementTier = EngagementTier.PASSIVE;
        }

        if (communityOptIn == null) {
            communityOptIn = true;
        }

        if (status == null) {
            status = AlumniStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}