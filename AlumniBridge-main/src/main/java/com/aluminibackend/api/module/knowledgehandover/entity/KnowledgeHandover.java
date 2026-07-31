package com.aluminibackend.api.module.knowledgehandover.entity;

import com.aluminibackend.api.module.knowledgehandover.enums.HandoverStatus;
import com.aluminibackend.api.module.offboarding.entity.OffboardingCase;
import com.aluminibackend.api.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_handovers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeHandover {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long handoverId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false, unique = true)
    private OffboardingCase offboardingCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private User employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "successor_id", nullable = false)
    private User successor;

    @Column(columnDefinition = "TEXT")
    private String activeProjectsSummary;

    @Column(columnDefinition = "TEXT")
    private String keyContactsRef;

    @Column(columnDefinition = "TEXT")
    private String systemsAccessDetails;

    @Column(columnDefinition = "TEXT")
    private String criticalProcessNotes;

    private LocalDate completionDeadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HandoverStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        if (status == null) {
            status = HandoverStatus.DRAFT;
        }
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}