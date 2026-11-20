package com.quinter.madness_table.investigator;

import com.quinter.madness_table.campaign.CampaignAccess;
import com.quinter.madness_table.campaign.CampaignMember;
import com.quinter.madness_table.campaign.ConflictException;
import com.quinter.madness_table.campaign.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CampaignSkillService {
    private final CampaignAccess access;
    private final CampaignSkillRepository skillRepository;
    private final InvestigatorSkillRepository investigatorSkillRepository;


    public CampaignSkillService(CampaignAccess access,
                                CampaignSkillRepository skillRepository,
                                InvestigatorSkillRepository investigatorSkillRepository) {
        this.access = access;
        this.skillRepository = skillRepository;
        this.investigatorSkillRepository = investigatorSkillRepository;
    }

    @Transactional(readOnly = true)
    public List<SkillResponse> list(Long userId, Long campaignId) {
        access.requireMember(campaignId, userId);
        return skillRepository.findByCampaignIdOrderByNameAsc(campaignId).stream()
                .map(SkillResponse::from)
                .toList();
    }

    @Transactional
    public SkillResponse create(Long userId, Long campaignId, SkillRequest request) {
        CampaignMember guardian = access.requireGuardian(campaignId, userId);
        String name = request.name();

        if (skillRepository.existsByCampaignIdAndNameIgnoreCase(campaignId, name)) {
            throw new ConflictException("Ya existe una habilidad con este nombre: " + name);
        }
        CampaignSkill skill = skillRepository.save(
                new CampaignSkill(guardian.getCampaign(), name, request.baseValue())
        );
        return SkillResponse.from(skill);
    }

    @Transactional
    public SkillResponse update(Long userId, Long campaignId, Long skillId, SkillRequest request) {
        access.requireGuardian(campaignId, userId);
        CampaignSkill skill = findSkill(campaignId, skillId);
        String name = request.name();

        boolean taken = skillRepository.findByCampaignIdAndNameIgnoreCase(campaignId, name)
                .filter(other -> !other.getId().equals(skillId))
                .isPresent();

        if(taken) {
            throw new ConflictException("Ya existe una habilidad con ese nombre");
        }
        skill.setName(name);
        skill.setBaseValue(request.baseValue());
        return SkillResponse.from(skill);
    }

    @Transactional
    public void delete(Long userId, Long campaignId, Long skillId) {
        access.requireGuardian(campaignId, userId);
        CampaignSkill skill = findSkill(campaignId, skillId);

        if (investigatorSkillRepository.existsByCampaignSkillId(skillId)){
            throw new ConflictException("La habilidad está en uso en alguna ficha");
        }
        skillRepository.delete(skill);
    }

    private CampaignSkill findSkill(Long campaignId, Long skillId) {
        return skillRepository.findByIdAndCampaignId(skillId, campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Habilidad no encontrada"));
    }

}
