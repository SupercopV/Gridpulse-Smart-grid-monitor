package com.gridpulse.controller;

import com.gridpulse.dto.AuthResponse;
import com.gridpulse.dto.LoginRequest;
import com.gridpulse.dto.RegisterRequest;
import com.gridpulse.entity.User;
import com.gridpulse.exception.InvalidCredentialsException;
import com.gridpulse.repository.UserRepository;
import com.gridpulse.security.JwtTokenProvider;
import com.gridpulse.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(jwt)
                    .tokenType("Bearer")
                    .userId(userPrincipal.getId())
                    .username(userPrincipal.getUsername())
                    .fullName(userPrincipal.getUser().getFullName())
                    .role(userPrincipal.getUser().getRole().replace("ROLE_", ""))
                    .build());
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Username is already taken");
            return ResponseEntity.badRequest().body(response);
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email Address already in use");
            return ResponseEntity.badRequest().body(response);
        }

        // Format role
        String rawRole = registerRequest.getRole().toUpperCase();
        String formattedRole = rawRole.startsWith("ROLE_") ? rawRole : "ROLE_" + rawRole;

        // Creating user's account
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName())
                .role(formattedRole)
                .build();

        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User registered successfully");
        return ResponseEntity.ok(response);
    }
}
