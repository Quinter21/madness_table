package com.quinter.madness_table.investigator;

import com.quinter.madness_table.auth.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Investigators",
description = "Fichas de investigador. Un jugador ve y gestiona las suyas; el Guardián ve todas las de la campaña")
@RestController
@RequestMapping("/api/campaigns/{campaignId}/investigators")
public class InvestigatorController {

    private final InvestigatorService service;

    public InvestigatorController(InvestigatorService service){
        this.service = service;
    }

    @Operation(summary = "Crear investigador")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvestigatorResponse create(@AuthenticationPrincipal AuthenticatedUser user,
                                       @PathVariable Long campaignId,
                                       @Valid @RequestBody CreateInvestigatorRequest request) {
        return service.create(user.id(), campaignId, request);
    }

    @Operation(summary = "Lista de investigadores de la campaña")
    @GetMapping
    public List<InvestigatorResponse> list(@AuthenticationPrincipal AuthenticatedUser user,
                                           @PathVariable Long campaignId) {
        return service.list(user.id(), campaignId);
    }

    @Operation(summary = "Información de un investigador concreto")
    @GetMapping("/{id}")
    public InvestigatorResponse get(@AuthenticationPrincipal AuthenticatedUser user,
                                    @PathVariable Long campaignId,
                                    @PathVariable Long id) {
        return service.get(user.id(), campaignId, id);
    }

    @Operation(summary = "Actualizar la información de un investigador")
    @PutMapping("/{id}")
    public InvestigatorResponse update(@AuthenticationPrincipal AuthenticatedUser user,
                                       @PathVariable Long campaignId,
                                       @PathVariable Long id,
                                       @Valid @RequestBody UpdateInvestigatorRequest request) {
        return service.update(user.id(), campaignId, id, request);
    }

    @Operation(summary = "Eliminar un investigador")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AuthenticatedUser user,
                       @PathVariable Long campaignId,
                       @PathVariable Long id) {
        service.delete(user.id(), campaignId, id);
    }


}
