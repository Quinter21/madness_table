package com.quinter.madness_table.campaign;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {

    List<JoinRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByCampaignIdAndUserIdAndStatus(Long campaignId, Long userId, JoinRequestStatus status);
}
