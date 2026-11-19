package com.quinter.madness_table.investigator;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvestigatorRepository extends JpaRepository<Investigator, Long> {

    @EntityGraph(attributePaths = "user")
    List<Investigator> findByCampaignIdOrderByNameAsc(Long campaignId);

    @EntityGraph(attributePaths = "user")

    List<Investigator> findByCampaignIdAndUserIdOrderByNameAsc(Long campaignId, Long userId);

    @EntityGraph(attributePaths = "user")
    Optional<Investigator> findByIdAndCampaignId(Long id, Long campaignId);
}
