package com.quinter.madness_table.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Auth", description = "Registro, login y datos del usuario autenticado")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Registra un usuario nuevo")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request){
        return UserResponse.from(authService.register(request));
    }

    @Operation(summary = "Inicio de sesión para usuarios registrados")
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request){
        return authService.login(request);
    }

    @Operation(summary = "Muestra datos no sensibles del usuario autenticado")
    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal AuthenticatedUser user){
        return Map.of("id", user.id(), "email", user.email());
    }
}
