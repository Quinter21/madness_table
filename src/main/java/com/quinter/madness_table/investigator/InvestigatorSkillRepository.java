package com.quinter.madness_table.investigator;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvestigatorSkillRepository extends JpaRepository<InvestigatorSkill, Long> {

    List<InvestigatorSkill> findByInvestigatorId(Long investigatorId);

    Optional<InvestigatorSkill> findByInvestigatorIdAndCampaignSkillId(Long investigatorId, Long campaignSkillId);
}
