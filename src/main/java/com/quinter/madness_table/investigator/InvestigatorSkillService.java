package com.quinter.madness_table.investigator;

import com.quinter.madness_table.campaign.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InvestigatorSkillService {

    private final InvestigatorAccess investigatorAccess;
    private final CampaignSkillRepository skillRepository;
    private final InvestigatorSkillRepository investigatorSkillRepository;

    public InvestigatorSkillService(InvestigatorAccess investigatorAccess, CampaignSkillRepository skillRepository, InvestigatorSkillRepository investigatorSkillRepository) {
        this.investigatorAccess = investigatorAccess;
        this.skillRepository = skillRepository;
        this.investigatorSkillRepository = investigatorSkillRepository;
    }

    @Transactional(readOnly = true)
    public List<InvestigatorSkillResponse> list(Long userId, Long campaignId, Long investigatorId){
        investigatorAccess.requireVisible(userId, campaignId, investigatorId);

        Map<Long, Integer> custom = investigatorSkillRepository
                .findByInvestigatorId(investigatorId).stream()
                .collect(Collectors.toMap(
                        s -> s.getCampaignSkill().getId(), InvestigatorSkill::getValue
                ));
        return skillRepository.findByCampaignIdOrderByNameAsc(campaignId).stream()
                .map(skill -> InvestigatorSkillResponse.of(skill, custom.get(skill.getId())))
                .toList();
    }

    @Transactional
    public InvestigatorSkillResponse setValue(Long userId, Long campaignId, Long investigatorId,
                                              Long skillId, int value){
        Investigator investigator = investigatorAccess.requireVisible(userId, campaignId, investigatorId);
        CampaignSkill skill = skillRepository.findByIdAndCampaignId(skillId, campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Habilidad no encontrada"));

        investigatorSkillRepository.findByInvestigatorIdAndCampaignSkillId(investigatorId, skillId)
                .ifPresentOrElse(
                        existing -> existing.setValue(value),
                        () -> investigatorSkillRepository.save(
                                new InvestigatorSkill(investigator, skill, value)
                        )
                );
        return InvestigatorSkillResponse.of(skill, value);
    }

    @Transactional
    public void reset(Long userId, Long campaignId, Long investigatorId, Long skillId){
        investigatorAccess.requireVisible(userId, campaignId, investigatorId);
        investigatorSkillRepository
                .findByInvestigatorIdAndCampaignSkillId(investigatorId, skillId)
                .ifPresent(investigatorSkillRepository::delete);
    }
}
