package com.financetracker.controller;

import com.financetracker.dto.*;
import com.financetracker.security.UserPrincipal;
import com.financetracker.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ApiResponse<AuthResponse> signup(@Valid @RequestBody SignupRequest req) {
        return ApiResponse.ok("Account created successfully", authService.signup(req));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok("Logged in successfully", authService.login(req));
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(authService.getMe(principal.getId()));
    }

    @PutMapping("/profile")
    public ApiResponse<UserResponse> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                     @Valid @RequestBody UpdateProfileRequest req) {
        return ApiResponse.ok("Profile updated", authService.updateProfile(principal.getId(), req));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                             @Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(principal.getId(), req);
        return ApiResponse.ok("Password updated", null);
    }

    @GetMapping("/stats")
    public ApiResponse<AccountStatsResponse> stats(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(authService.getAccountStats(principal.getId()));
    }
}
