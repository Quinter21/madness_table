package com.quinter.madness_table.campaign;

import com.quinter.madness_table.auth.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/campaigns/{campaignId}")
public class CampaignManagementController {

    private final CampaignManagementService service;

    public CampaignManagementController(CampaignManagementService service) {
        this.service = service;
    }

    @GetMapping("/members")
    public List<MemberResponse> members(@AuthenticationPrincipal AuthenticatedUser user,
                                        @PathVariable Long campaignId){
        return service.listMenbers(user.id(), campaignId);
    }

    @GetMapping("/requests")
    public List<PendingRequestResponse> pendingRequests(@AuthenticationPrincipal AuthenticatedUser user,
                                                         @PathVariable Long campaignId){
        return service.listPendingRequests(user.id(), campaignId);
    }

    @PostMapping("/requests/{requestId}/approve")
    public MemberResponse approve(@AuthenticationPrincipal AuthenticatedUser user,
                                   @PathVariable Long campaignId,
                                   @PathVariable Long requestId){
        return service.approve(user.id(), campaignId, requestId);
    }

    @PostMapping("/requests/{requestId}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(@AuthenticationPrincipal AuthenticatedUser user,
                                   @PathVariable Long campaignId,
                                   @PathVariable Long requestId){
        service.reject(user.id(), campaignId, requestId);
    }

    @PostMapping("/invite-code/regenerate")
    public Map<String, String> regenerateInviteCode(@AuthenticationPrincipal AuthenticatedUser user,
                                                     @PathVariable Long campaignId){
        return Map.of("inviteCode", service.regenerateInviteCode(user.id(), campaignId));
    }

    @DeleteMapping("/members/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@AuthenticationPrincipal AuthenticatedUser user,
                             @PathVariable Long campaignId,
                             @PathVariable Long targetUserId){
        service.removeMember(user.id(), campaignId, targetUserId);
    }
}
