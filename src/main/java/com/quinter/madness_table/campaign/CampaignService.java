package com.quinter.madness_table.campaign;

import com.quinter.madness_table.auth.user.User;
import com.quinter.madness_table.auth.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final CampaignMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final InviteCodeGenerator inviteCodeGenerator;

    public CampaignService(CampaignRepository campaignRepository, CampaignMemberRepository memberRepository, UserRepository userRepository, InviteCodeGenerator inviteCodeGenerator) {
        this.campaignRepository = campaignRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.inviteCodeGenerator = inviteCodeGenerator;
    }

    @Transactional
    public CampaignResponse create(Long userId, CreateCampaignRequest request) {
        User user = userRepository.getReferenceById(userId);

        String code;
        do {
            code = inviteCodeGenerator.generate();
        } while (campaignRepository.existsByInviteCode(code));

        Campaign campaign = campaignRepository.save(new Campaign(
                request.name().trim(),
                request.description(),
                code,
                user
        ));

        memberRepository.save(new CampaignMember(campaign, user, CampaignRole.GUARDIAN));

        return CampaignResponse.from(campaign, CampaignRole.GUARDIAN);
    }

    @Transactional(readOnly = true)
    public List<CampaignResponse> listMine(Long userId){
        return memberRepository.findByUserId(userId).stream()
                .map(m -> CampaignResponse.from(m.getCampaign(), m.getRole()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CampaignResponse getbyId(Long userId, Long campaignId){
        CampaignMember member = memberRepository
                .findByCampaignIdAndUserId(campaignId, userId)
                .orElseThrow(() -> new CampaignNotFoundException(campaignId));
        return CampaignResponse.from(member.getCampaign(), member.getRole());
    }
}
