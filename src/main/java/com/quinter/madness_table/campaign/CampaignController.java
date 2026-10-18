package com.quinter.madness_table.campaign;

import com.quinter.madness_table.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Campaign", description = "Creación y datos de las campañas de un jugador")
@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @Operation(summary = "Crea una campaña; quien la crea queda como Guardián")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CampaignResponse create(@AuthenticationPrincipal AuthenticatedUser user,
                                   @Valid @RequestBody CreateCampaignRequest request){
        return campaignService.create(user.id(), request);
    }

    @Operation(summary = "Lista las campañas en las que el usuario autenticado está presente")
    @GetMapping
    public List<CampaignResponse> listMine(@AuthenticationPrincipal AuthenticatedUser user) {
        return campaignService.listMine(user.id());
    }

    @Operation(summary = "Datos de una campaña específica si el usuario autenticado pertence a la misma")
    @GetMapping("/{id}")
    public CampaignResponse getById(@AuthenticationPrincipal AuthenticatedUser user,
                                    @PathVariable Long id) {
        return campaignService.getbyId(user.id(), id);
    }
}
