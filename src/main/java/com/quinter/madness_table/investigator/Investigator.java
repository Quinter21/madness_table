package com.quinter.madness_table.investigator;


import com.quinter.madness_table.auth.user.User;
import com.quinter.madness_table.campaign.Campaign;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "investigators")
public class Investigator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String occupation;

    @Column(nullable = false)
    private int age;

    @Column(columnDefinition = "TEXT")
    private String backstory;

    @Embedded
    private Characteristics characteristics;

    @Column(name = "hit_points", nullable = false)
    private int hitPoints;

    @Column(name = "magic_points", nullable = false)
    private int magicPoints;

    @Column(nullable = false)
    private int sanity;

    @Column(nullable = false)
    private int luck;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Investigator() {}

    public Investigator(Campaign campaign, User user, String name, String occupation,
                        int age, String backstory, Characteristics characteristics,
                        int hitPoints, int magicPoints, int sanity, int luck) {
        this.campaign = campaign;
        this.user = user;
        this.name = name;
        this.occupation = occupation;
        this.age = age;
        this.backstory = backstory;
        this.characteristics = characteristics;
        this.hitPoints = hitPoints;
        this.magicPoints = magicPoints;
        this.sanity = sanity;
        this.luck = luck;
    }

    public Long getId() {
        return id;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public User getUser() {
        return user;
    }

    public String getName() {
        return name;
    }

    public String getOccupation() {
        return occupation;
    }

    public int getAge() {
        return age;
    }

    public String getBackstory() {
        return backstory;
    }

    public Characteristics getCharacteristics() {
        return characteristics;
    }

    public int getHitPoints() {
        return hitPoints;
    }

    public int getMagicPoints() {
        return magicPoints;
    }

    public int getSanity() {
        return sanity;
    }

    public int getLuck() {
        return luck;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
