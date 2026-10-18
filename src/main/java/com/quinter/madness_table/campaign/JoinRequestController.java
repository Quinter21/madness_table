package com.quinter.madness_table.campaign;

import com.quinter.madness_table.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Join Requests", description = "Solicitar unirse a una campaña y mostrar las solicitudes del usuario" +
        "autenticado")
@RestController
@RequestMapping("/api/campaigns")
public class JoinRequestController {

    private final JoinRequestService joinRequestService;

    public JoinRequestController(JoinRequestService joinRequestService) {
        this.joinRequestService = joinRequestService;
    }

    @Operation(summary = "Soliciar unirse a una campaña")
    @PostMapping("/join")
    @ResponseStatus(HttpStatus.CREATED)
    public JoinRequestResponse join(@AuthenticationPrincipal AuthenticatedUser user,
                                    @Valid @RequestBody JoinCampaignRequest request){
        return joinRequestService.requestToJoin(user.id(), request);
    }

    @Operation(summary = "Listar las solicitudes del usuario autenticado")
    @GetMapping("/requests/mine")
    public List<JoinRequestResponse> myRequests(@AuthenticationPrincipal AuthenticatedUser user) {
        return joinRequestService.listMine(user.id());
    }
}
