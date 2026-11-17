package com.quinter.madness_table.investigator;

import com.quinter.madness_table.auth.user.User;
import com.quinter.madness_table.rules.SuccessLevel;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "roll_logs")
public class RollLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investigator_id", nullable = false)
    private Investigator investigator;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rolled_by", nullable = false)
    private User rolledBy;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    @Column(name = "skill_value", nullable = false)
    private int skillValue;

    @Column(name = "roll_result", nullable = false)
    private int rollResult;

    @Column(name = "net_dice", nullable = false)
    private int netDice;

    @Enumerated(EnumType.STRING)
    @Column(name = "success_level", nullable = false, length = 20)
    private SuccessLevel successLevel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected RollLog() {}

    public RollLog(Investigator investigator, User rolledBy, String skillName, int skillValue,
                   int rollResult, int netDice, SuccessLevel successLevel) {
        this.investigator = investigator;
        this.rolledBy = rolledBy;
        this.skillName = skillName;
        this.skillValue = skillValue;
        this.rollResult = rollResult;
        this.netDice = netDice;
        this.successLevel = successLevel;
    }

    public Long getId() { return id; }
    public Investigator getInvestigator() { return investigator; }
    public User getRolledBy() { return rolledBy; }
    public String getSkillName() { return skillName; }
    public int getSkillValue() { return skillValue; }
    public int getRollResult() { return rollResult; }
    public int getNetDice() { return netDice; }
    public SuccessLevel getSuccessLevel() { return successLevel; }
    public Instant getCreatedAt() { return createdAt; }

}
