package com.quinter.madness_table.campaign;

import com.quinter.madness_table.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
public class JoinRequestController {

    private final JoinRequestService joinRequestService;

    public JoinRequestController(JoinRequestService joinRequestService) {
        this.joinRequestService = joinRequestService;
    }

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.CREATED)
    public JoinRequestResponse join(@AuthenticationPrincipal AuthenticatedUser user,
                                    @Valid @RequestBody JoinCampaignRequest request){
        return joinRequestService.requestToJoin(user.id(), request);
    }

    @GetMapping("/requests/mine")
    public List<JoinRequestResponse> myRequests(@AuthenticationPrincipal AuthenticatedUser user) {
        return joinRequestService.listMine(user.id());
    }
}
