package com.quinter.madness_table.investigator;

import com.quinter.madness_table.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Investigator skills",
description = "Listar, modificar y resetar las habilidades de un investigador; el guardián ve todas, otro jugador solo las de sus investigadores")
@RestController
@RequestMapping("/api/campaigns/{campaignId}/investigators/{investigatorId}/skills")
public class InvestigatorSkillController {

    private final InvestigatorSkillService service;

    public InvestigatorSkillController(InvestigatorSkillService service) {
        this.service = service;
    }

    @Operation(summary = "Listar las habilidades de un investigador")
    @GetMapping
    public List<InvestigatorSkillResponse> list(@AuthenticationPrincipal AuthenticatedUser user,
                                                @PathVariable Long campaignId,
                                                @PathVariable Long investigatorId) {
        return service.list(user.id(), campaignId, investigatorId);
    }

    @Operation(summary = "Actualizar el valor de una habilidad concreta")
    @PutMapping("/{skillId}")
    public InvestigatorSkillResponse setValue(@AuthenticationPrincipal AuthenticatedUser user,
                                            @PathVariable Long campaignId,
                                            @PathVariable Long investigatorId,
                                            @PathVariable Long skillId,
                                            @Valid @RequestBody SetSkillValueRequest request) {
        return service.setValue(user.id(), campaignId, investigatorId, skillId, request.value());
    }

    @Operation(summary = "Resetear el valor de una habilidad concreta")
    @DeleteMapping("/{skillId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset(@AuthenticationPrincipal AuthenticatedUser user,
                      @PathVariable Long campaignId,
                      @PathVariable Long investigatorId,
                      @PathVariable Long skillId) {
        service.reset(user.id(), campaignId, investigatorId, skillId);
    }
}
