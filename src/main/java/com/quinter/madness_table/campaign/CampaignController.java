package com.quinter.madness_table.campaign;

import com.quinter.madness_table.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CampaignResponse create(@AuthenticationPrincipal AuthenticatedUser user,
                                   @Valid @RequestBody CreateCampaignRequest request){
        return campaignService.create(user.id(), request);
    }

    @GetMapping
    public List<CampaignResponse> listMine(@AuthenticationPrincipal AuthenticatedUser user) {
        return campaignService.listMine(user.id());
    }

    @GetMapping("/{id}")
    public CampaignResponse getById(@AuthenticationPrincipal AuthenticatedUser user,
                                    @PathVariable Long id) {
        return campaignService.getbyId(user.id(), id);
    }
}
