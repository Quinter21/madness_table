package com.quinter.madness_table.campaign;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {

    List<JoinRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<JoinRequest> findByCampaignIdAndStatusOrderByCreatedAtAsc(Long campaignId,
                                                                   JoinRequestStatus status);

    Optional<JoinRequest> findByIdAndCampaignId(Long id, Long campaignId);

    boolean existsByCampaignIdAndUserIdAndStatus(Long campaignId, Long userId, JoinRequestStatus status);


}
