package com.quinter.madness_table.campaign;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Optional<Campaign> findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);
}
