package com.quinter.madness_table.investigator;

import com.quinter.madness_table.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Campaign skills",
description = "Habilidades de una campaña; creación, actualización, consulta y eliminación")
@RestController
@RequestMapping("/api/campaigns/{campaignId}/skills")
public class CampaignSkillController {

    private final CampaignSkillService service;

    public CampaignSkillController(CampaignSkillService service) {
        this.service = service;
    }

    @Operation(summary = "Listar las habilidades; cualquier integrante puede hacerlo")
    @GetMapping
    public List<SkillResponse> list(@AuthenticationPrincipal AuthenticatedUser user,
                                    @PathVariable Long campaignId) {
        return service.list(user.id(), campaignId);
    }

    @Operation(summary = "Crear una nueva habilidad; solo el guardián tiene permiso")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SkillResponse create(@AuthenticationPrincipal AuthenticatedUser user,
                                @PathVariable Long campaignId,
                                @Valid @RequestBody SkillRequest request) {
        return service.create(user.id(), campaignId, request);
    }

    @Operation(summary = "Actualizar una habilidad, si no existe se crea; solo el guardián tiene permiso")
    @PutMapping("/{skillId}")
    public SkillResponse update(@AuthenticationPrincipal AuthenticatedUser user,
                                @PathVariable Long campaignId,
                                @PathVariable Long skillId,
                                @Valid @RequestBody SkillRequest request) {
        return service.update(user.id(), campaignId, skillId, request);
    }

    @Operation(summary = "Eliminar una habilidad; solo el guardián tiene permiso")
    @DeleteMapping("/{skillId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthenticatedUser user,
                       @PathVariable Long campaignId,
                       @PathVariable Long skillId) {
        service.delete(user.id(), campaignId, skillId);
    }
}
