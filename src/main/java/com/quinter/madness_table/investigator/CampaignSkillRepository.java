package com.quinter.madness_table.investigator;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampaignSkillRepository extends JpaRepository<CampaignSkill, Long> {

    List<CampaignSkill> findByCampaignIdOrderByNameAsc(Long campaignId);

    Optional<CampaignSkill> findByIdAndCampaignId(Long id, Long campaignId);

    Optional<CampaignSkill> findByCampaignIdAndNameIgnoreCase(Long campaignId, String name);

    boolean existsByCampaignIdAndNameIgnoreCase(Long campaignId, String name);
}
