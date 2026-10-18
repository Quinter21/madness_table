package com.quinter.madness_table.campaign;

import org.springframework.stereotype.Component;

@Component
public class CampaignAccess {

    private final CampaignMemberRepository memberRepository;

    public CampaignAccess(CampaignMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public CampaignMember requireMember(Long campaignId, Long userId){
        return memberRepository.findByCampaignIdAndUserId(campaignId, userId)
                .orElseThrow(() -> new CampaignNotFoundException(campaignId));
    }

    public CampaignMember requireGuardian(Long campaignId, Long userId){
        CampaignMember member = requireMember(campaignId, userId);
        if(member.getRole() != CampaignRole.GUARDIAN){
            throw new NotGuardianException();
        }
        return member;
    }
}
