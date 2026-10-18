package com.quinter.madness_table.campaign;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CampaignManagementService {

    private final CampaignAccess access;
    private final CampaignRepository campaignRepository;
    private final CampaignMemberRepository memberRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final InviteCodeGenerator inviteCodeGenerator;

    public CampaignManagementService(CampaignAccess access, CampaignRepository campaignRepository, CampaignMemberRepository memberRepository, JoinRequestRepository joinRequestRepository, InviteCodeGenerator inviteCodeGenerator) {
        this.access = access;
        this.campaignRepository = campaignRepository;
        this.memberRepository = memberRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.inviteCodeGenerator = inviteCodeGenerator;
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> listMenbers(Long userId, Long campaignId){
        access.requireMember(campaignId, userId);
        return memberRepository.findByCampaignId(campaignId).stream()
                .map(MemberResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PendingRequestResponse> listPendingRequests(Long userId, Long campaignId){
        access.requireGuardian(campaignId, userId);
        return joinRequestRepository
                .findByCampaignIdAndStatusOrderByCreatedAtAsc(campaignId, JoinRequestStatus.PENDING)
                .stream()
                .map(PendingRequestResponse::from)
                .toList();
    }

    @Transactional
    public MemberResponse approve(Long userId, Long campaignId, Long requestId){
        CampaignMember guardian = access.requireGuardian(campaignId, userId);
        JoinRequest request = findRequest(campaignId, requestId);

        request.approve();

        if(memberRepository.existsByCampaignIdAndUserId(campaignId, request.getUser().getId())){
            throw new JoinRequestConflictException("El usuario ya es miembro");
        }
        CampaignMember newMember = memberRepository.save(
                new CampaignMember(guardian.getCampaign(), request.getUser(), CampaignRole.PLAYER)
        );
        return MemberResponse.from(newMember);
    }

    @Transactional
    public void reject(Long userId, Long campaignId, Long requestId){
        access.requireGuardian(campaignId, userId);
        findRequest(campaignId, requestId).reject();
    }

    @Transactional
    public String regenerateInviteCode(Long userId, Long campaignId){
        CampaignMember guardian = access.requireGuardian(campaignId, userId);
        Campaign campaign = guardian.getCampaign();

        String code;
        do{
            code = inviteCodeGenerator.generate();
        } while (campaignRepository.existsByInviteCode(code));

        campaign.setInviteCode(code);
        return code;
    }

    @Transactional
    public void removeMember(Long userId, Long campaignId, Long targetUserId){
        access.requireGuardian(campaignId, userId);

        if(targetUserId.equals(userId)){
            throw new JoinRequestConflictException("El guardian no puede dejar el plano astral");
        }
        CampaignMember target = memberRepository.findByCampaignIdAndUserId(campaignId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("El mortal no se encuentra " +
                        "en esta dimensión"));
        memberRepository.delete(target);
    }

    private JoinRequest findRequest(Long campaignId, Long requestId) {
        return joinRequestRepository.findByIdAndCampaignId(requestId, campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
    }
}
