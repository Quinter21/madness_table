package com.quinter.madness_table.investigator;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvestigatorRepository extends JpaRepository<Investigator, Long> {

    List<Investigator> findByCampaignIdOrderByNameAsc(Long campaignId);

    List<Investigator> findByCampaignIdAndUserIdOrderByNameAsc(Long campaignId, Long userId);

    Optional<Investigator> findByIdAndCampaignId(Long id, Long campaignId);
}
