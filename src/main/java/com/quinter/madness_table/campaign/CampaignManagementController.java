package com.quinter.madness_table.campaign;

import com.quinter.madness_table.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Campaign Management", description = "Gestión de una campaña: miembros, solicitudes de unión y código" +
        "de invitación")
@RestController
@RequestMapping("/api/campaigns/{campaignId}")
public class CampaignManagementController {

    private final CampaignManagementService service;

    public CampaignManagementController(CampaignManagementService service) {
        this.service = service;
    }

    @Operation(summary = "Mostrar miembros de una campaña; cualquier miembro puede hacerlo")
    @GetMapping("/members")
    public List<MemberResponse> members(@AuthenticationPrincipal AuthenticatedUser user,
                                        @PathVariable Long campaignId){
        return service.listMenbers(user.id(), campaignId);
    }

    @Operation(summary = "Mostrar peticiones para unirse a una campaña; solo el guardián tiene acceso")
    @GetMapping("/requests")
    public List<PendingRequestResponse> pendingRequests(@AuthenticationPrincipal AuthenticatedUser user,
                                                         @PathVariable Long campaignId){
        return service.listPendingRequests(user.id(), campaignId);
    }

    @Operation(summary = "Aprobar una petición de unión; solo el guardián puede hacerlo")
    @PostMapping("/requests/{requestId}/approve")
    public MemberResponse approve(@AuthenticationPrincipal AuthenticatedUser user,
                                   @PathVariable Long campaignId,
                                   @PathVariable Long requestId){
        return service.approve(user.id(), campaignId, requestId);
    }

    @Operation(summary = "Rechazar una petición de unión; solo el guardián puede hacerlo")
    @PostMapping("/requests/{requestId}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(@AuthenticationPrincipal AuthenticatedUser user,
                                   @PathVariable Long campaignId,
                                   @PathVariable Long requestId){
        service.reject(user.id(), campaignId, requestId);
    }

    @Operation(summary = "Regenerar código de invitación de una campaña; solo el guardián puede hacerlo")
    @PostMapping("/invite-code/regenerate")
    public Map<String, String> regenerateInviteCode(@AuthenticationPrincipal AuthenticatedUser user,
                                                     @PathVariable Long campaignId){
        return Map.of("inviteCode", service.regenerateInviteCode(user.id(), campaignId));
    }

    @Operation(summary = "Expulsar a un miembro de una campaña; solo el guardián puede hacerlo")
    @DeleteMapping("/members/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@AuthenticationPrincipal AuthenticatedUser user,
                             @PathVariable Long campaignId,
                             @PathVariable Long targetUserId){
        service.removeMember(user.id(), campaignId, targetUserId);
    }
}
