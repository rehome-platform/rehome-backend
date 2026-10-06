package com.example.jwtdemo.service;

import com.example.jwtdemo.dto.request.LoginRequest;
import com.example.jwtdemo.dto.request.RegisterRequest;
import com.example.jwtdemo.dto.response.AuthResponse;
import com.example.jwtdemo.dto.response.OtpResponse;

public interface AuthService {

    OtpResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String requestRefreshToken);

    void logout(String email);
}
