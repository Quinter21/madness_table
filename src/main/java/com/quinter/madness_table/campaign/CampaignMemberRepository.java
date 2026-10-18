package com.quinter.madness_table.campaign;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampaignMemberRepository extends JpaRepository<CampaignMember, Long> {

    List<CampaignMember> findByUserId(Long userId);

    Optional<CampaignMember> findByCampaignIdAndUserId(Long campaignId, Long userId);

    List<CampaignMember> findByCampaignId(Long campaignId);

    boolean existsByCampaignIdAndUserId(Long campaignId, Long userId);
}
