package com.systelm.controller;

import com.systelm.dto.LoginRequest;
import com.systelm.dto.LoginResponse;
import com.systelm.entity.UserAccount;
import com.systelm.repository.UserAccountRepository;
import com.systelm.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository userAccountRepository;
    private final JwtService jwtService;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserAccountRepository userAccountRepository,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userAccountRepository = userAccountRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        UserAccount user = userAccountRepository.findByUsername(req.username())
            .orElseThrow();

        List<Long> warehouseIds = user.getWarehouses().stream()
            .map(w -> w.getId())
            .toList();

        return new LoginResponse(
            jwtService.generateToken(user.getUsername()),
            user.getUsername(),
            user.getDisplayName(),
            user.getRole().getName(),
            warehouseIds
        );
    }
}
