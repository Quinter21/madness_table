package com.quinter.madness_table.campaign;

import com.quinter.madness_table.auth.user.User;
import com.quinter.madness_table.auth.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class JoinRequestService {

    private final CampaignRepository campaignRepository;
    private final CampaignMemberRepository memberRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final UserRepository userRepository;

    public JoinRequestService(CampaignRepository campaignRepository,
                              CampaignMemberRepository memberRepository,
                              JoinRequestRepository joinRequestRepository,
                              UserRepository userRepository){
        this.campaignRepository = campaignRepository;
        this.memberRepository = memberRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public JoinRequestResponse requestToJoin(Long userId, JoinCampaignRequest request){
        String code = request.inviteCode().trim().toUpperCase(Locale.ROOT);

        Campaign campaign = campaignRepository.findByInviteCode(code)
                .orElseThrow(InvalidInviteCodeException::new);

        if (memberRepository.existsByCampaignIdAndUserId(campaign.getId(), userId)){
            throw new JoinRequestConflictException("Ya eres miembro de esta campaña");
        }

        if (joinRequestRepository.existsByCampaignIdAndUserIdAndStatus(campaign.getId(),
                userId, JoinRequestStatus.PENDING)){
            throw new JoinRequestConflictException("Ya tienes una solicitud pendiente para esta campaña");
        }

        User user = userRepository.getReferenceById(userId);
        JoinRequest saved = joinRequestRepository.save(new JoinRequest(campaign, user));
        return JoinRequestResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<JoinRequestResponse> listMine(Long userId){
        return joinRequestRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(JoinRequestResponse::from)
                .toList();
    }
}
