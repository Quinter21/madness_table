package com.quinter.madness_table.campaign;

import com.quinter.madness_table.auth.user.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "campaign_members")
public class CampaignMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CampaignRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt = Instant.now();

    protected CampaignMember(){

    }

    public CampaignMember(Campaign campaign, User user, CampaignRole role){
        this.campaign = campaign;
        this.user = user;
        this.role = role;
    }

    public Long getId() { return id; }
    public Campaign getCampaign() { return campaign; }
    public User getUser() { return user; }
    public CampaignRole getRole() { return role; }
    public Instant getJoinedAt() { return joinedAt; }
}
