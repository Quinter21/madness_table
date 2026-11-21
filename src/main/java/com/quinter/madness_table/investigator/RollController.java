package com.quinter.madness_table.investigator;

import com.quinter.madness_table.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "",
description = "")
@RestController
@RequestMapping("/api/campaigns/{campaignId}/investigators/{investigatorId}/rolls")
public class RollController {

    private final RollService service;

    public RollController(RollService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RollResponse roll(@AuthenticationPrincipal AuthenticatedUser user,
                             @PathVariable Long campaignId,
                             @PathVariable Long investigatorId,
                             RollRequest request) {
        return service.roll(user.id(), campaignId, investigatorId, request);
    }

    @GetMapping
    public List<RollResponse> history(@AuthenticationPrincipal AuthenticatedUser user,
                                      @PathVariable Long campaignId,
                                      @PathVariable Long investigatorId) {
        return service.history(user.id(), campaignId, investigatorId);
    }
}
