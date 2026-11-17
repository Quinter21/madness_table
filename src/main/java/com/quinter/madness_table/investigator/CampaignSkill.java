package com.quinter.madness_table.investigator;

import com.quinter.madness_table.campaign.Campaign;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "campaign_skills")
public class CampaignSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "base_value", nullable = false)
    private int baseValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected CampaignSkill() {}

    public CampaignSkill(Campaign campaign, String name, int baseValue) {
        this.campaign = campaign;
        this.name = name;
        this.baseValue = baseValue;
    }

    public Long getId() {
        return id;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public String getName() {
        return name;
    }

    public int getBaseValue() {
        return baseValue;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBaseValue(int baseValue) {
        this.baseValue = baseValue;
    }
}
