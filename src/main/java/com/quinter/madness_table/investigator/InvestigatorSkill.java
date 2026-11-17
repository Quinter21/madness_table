package com.quinter.madness_table.investigator;

import jakarta.persistence.*;

@Entity
@Table(name = "investigator_skills")
public class InvestigatorSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investigator_id", nullable = false)
    private Investigator investigator;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_skill_id", nullable = false)
    private CampaignSkill campaignSkill;

    @Column(nullable = false)
    private int value;

    protected InvestigatorSkill() {}

    public InvestigatorSkill(Investigator investigator, CampaignSkill campaignSkill, int value) {
        this.investigator = investigator;
        this.campaignSkill = campaignSkill;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public Investigator getInvestigator() {
        return investigator;
    }

    public CampaignSkill getCampaignSkill() {
        return campaignSkill;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
