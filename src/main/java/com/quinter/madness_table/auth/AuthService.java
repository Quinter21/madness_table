package com.quinter.madness_table.auth;

import com.quinter.madness_table.auth.user.User;
import com.quinter.madness_table.auth.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest request){
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)){
            throw new EmailAlreadyUsedException(email);
        }

        String hash = passwordEncoder.encode(request.password());
        User user = new User(email, request.displayName().trim(), hash);
        return userRepository.save(user);
    }
}
