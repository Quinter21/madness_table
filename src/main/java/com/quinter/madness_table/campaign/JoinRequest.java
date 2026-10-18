package com.quinter.madness_table.campaign;

import com.quinter.madness_table.auth.user.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "join_requests")
public class JoinRequest {

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
    private JoinRequestStatus status = JoinRequestStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected JoinRequest(){
    }

    public JoinRequest(Campaign campaign, User user){
        this.campaign = campaign;
        this.user = user;
    }

    public void approve() {
        ensurePending();
        this.status = JoinRequestStatus.APPROVED;
        this.resolvedAt = Instant.now();
    }

    public void reject() {
        ensurePending();
        this.status = JoinRequestStatus.REJECTED;
        this.resolvedAt = Instant.now();
    }

    private void ensurePending() {
        if(this.status != JoinRequestStatus.PENDING){
            throw new ConflictException("El guardián ya ha respondido a las estrellas");
        }
    }

    public Long getId() { return id; }
    public Campaign getCampaign() { return campaign; }
    public User getUser() { return user; }
    public JoinRequestStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getResolvedAt() { return resolvedAt; }
}
