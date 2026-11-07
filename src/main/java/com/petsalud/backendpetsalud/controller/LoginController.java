package com.petsalud.backendpetsalud.controller;

import com.petsalud.backendpetsalud.dto.AuthRequest;
import com.petsalud.backendpetsalud.dto.JwtResponse;
import com.petsalud.backendpetsalud.security.JwtUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    private final JwtUtils jwtUtils;

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    public LoginController(JwtUtils jwtUtils, AuthenticationManager authenticationManager, @Qualifier("usuarioDetailsService") UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> login(
            @RequestBody AuthRequest request
    ) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(), request.password()
                    )
            );
            UserDetails user = userDetailsService.loadUserByUsername(request.username());
            String accessToken = jwtUtils.generateToken(user.getUsername());
            return ResponseEntity.ok(new JwtResponse(accessToken, "Bearer", jwtUtils.extractUsername(accessToken)));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
