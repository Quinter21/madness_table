package com.quinter.madness_table.investigator;

import com.quinter.madness_table.auth.user.User;
import com.quinter.madness_table.auth.user.UserRepository;
import com.quinter.madness_table.campaign.ResourceNotFoundException;
import com.quinter.madness_table.rules.CheckResult;
import com.quinter.madness_table.rules.RandomSource;
import com.quinter.madness_table.rules.SkillCheck;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RollService {

    private final InvestigatorAccess investigatorAccess;
    private final CampaignSkillRepository skillRepository;
    private final InvestigatorSkillRepository investigatorSkillRepository;
    private final RollLogRepository logRepository;
    private final UserRepository userRepository;
    private final RandomSource randomSource;

    public RollService(InvestigatorAccess investigatorAccess,
                       CampaignSkillRepository skillRepository,
                       InvestigatorSkillRepository investigatorSkillRepository,
                       RollLogRepository logRepository,
                       UserRepository userRepository,
                       RandomSource randomSource) {
        this.investigatorAccess = investigatorAccess;
        this.skillRepository = skillRepository;
        this.investigatorSkillRepository = investigatorSkillRepository;
        this.logRepository = logRepository;
        this.userRepository = userRepository;
        this.randomSource = randomSource;
    }

    @Transactional
    public RollResponse roll(Long userId, Long campaignId, Long investigatorId, RollRequest request) {
        Investigator investigator = investigatorAccess.requireVisible(userId, campaignId, investigatorId);
        CampaignSkill skill = skillRepository.findByIdAndCampaignId(request.skillId(), campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Habilidad no encontrada"));

        int value = investigatorSkillRepository
                .findByInvestigatorIdAndCampaignSkillId(investigatorId, skill.getId())
                .map(InvestigatorSkill::getValue)
                .orElse(skill.getBaseValue());

        CheckResult check = SkillCheck.perform(randomSource, value,
                request.bonusDice(), request.penaltyDice());

        User roller = userRepository.getReferenceById(userId);
        RollLog saved = logRepository.save(new RollLog(
                investigator, roller, skill.getName(), value,
                check.result(), check.roll().netDice(), check.level()
        ));

        return RollResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<RollResponse> history(Long userId, Long campaignId, Long investigatorId) {
        investigatorAccess.requireVisible(userId, campaignId, investigatorId);

        return logRepository.findTop50ByInvestigatorIdOrderByCreatedAtDesc(investigatorId)
                .stream().map(RollResponse::from)
                .toList();
    }

}
