package com.quinter.madness_table.auth;

import com.quinter.madness_table.auth.user.User;
import com.quinter.madness_table.auth.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerNormalizesEmailAndStoresHashedPassword() {
        when(userRepository.existsByEmail("angel@example.com")).thenReturn(false);
        when(passwordEncoder.encode("ClaveSegura123")).thenReturn("HASH");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = authService.register(
                new RegisterRequest("  Angel@Example.com ", "Angel", "ClaveSegura123"));

        assertThat(saved.getEmail()).isEqualTo("angel@example.com");
        assertThat(saved.getPasswordHash()).isEqualTo("HASH");
    }

    @Test
    void registerRejectsDuplicatedEmail() {
        when(userRepository.existsByEmail("angel@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("angel@example.com", "Angel", "ClaveSegura123")))
                .isInstanceOf(EmailAlreadyUsedException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void loginFailsWhenEmailDoesNotExist() {
        when(userRepository.findByEmail("nadie@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("nadie@example.com", "loquesea")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginFailsWithWrongPasswordAndDoesNotIssueToken() {
        User user = new User("angel@example.com", "Angel", "HASH");
        when(userRepository.findByEmail("angel@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("mala", "HASH")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("angel@example.com", "mala")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void loginReturnsTokenWhenCredentialsAreCorrect() {
        User user = new User("angel@example.com", "Angel", "HASH");
        when(userRepository.findByEmail("angel@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("ClaveSegura123", "HASH")).thenReturn(true);
        when(jwtService.generateToken(any(), eq("angel@example.com"))).thenReturn("TOKEN");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        TokenResponse response = authService.login(
                new LoginRequest("angel@example.com", "ClaveSegura123"));

        assertThat(response.accessToken()).isEqualTo("TOKEN");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(3600L);
    }
}
