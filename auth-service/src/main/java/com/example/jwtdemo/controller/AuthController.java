package com.example.jwtdemo.controller;

import com.example.jwtdemo.dto.response.AuthResponse;
import com.example.jwtdemo.dto.request.LoginRequest;
import com.example.jwtdemo.dto.request.RefreshTokenRequest;
import com.example.jwtdemo.dto.request.RegisterRequest;
import com.example.jwtdemo.dto.response.OtpResponse;
import com.example.jwtdemo.dto.request.OtpRequest;
import com.example.jwtdemo.dto.request.VerifyOtpRequest;
import com.example.jwtdemo.dto.request.ResetPasswordRequest;
import com.example.jwtdemo.service.OtpService;
import com.example.jwtdemo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    @PostMapping("/register")
    public ResponseEntity<OtpResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<OtpResponse> resendOtp(
            @Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(otpService.resend(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<OtpResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(otpService.verify(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<OtpResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(otpService.resetPassword(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication authentication) {
        authService.logout(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
