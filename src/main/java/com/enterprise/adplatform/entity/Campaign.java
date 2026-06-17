package com.enterprise.adplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "campaigns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "advertiser_id", nullable = false)
    private Long advertiserId;

    @Column(name = "campaign_name", nullable = false, length = 255)
    private String campaignName;

    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_type", nullable = false, length = 50)
    private CampaignType campaignType;

    @Column(name = "budget", nullable = false, precision = 15, scale = 2)
    private BigDecimal budget;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private CampaignStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum CampaignType {
        DISPLAY, VIDEO, SEARCH, SOCIAL, NATIVE, PROGRAMMATIC
    }

    public enum CampaignStatus {
        DRAFT, ACTIVE, PAUSED, COMPLETED, CANCELLED
    }
}
