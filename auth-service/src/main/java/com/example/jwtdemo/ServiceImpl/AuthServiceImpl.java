package com.example.jwtdemo.ServiceImpl;

import com.example.jwtdemo.service.AuthService;
import com.example.jwtdemo.service.OtpService;
import com.example.jwtdemo.service.RefreshTokenService;

import com.example.jwtdemo.dto.response.AuthResponse;
import com.example.jwtdemo.dto.request.LoginRequest;
import com.example.jwtdemo.dto.request.RegisterRequest;
import com.example.jwtdemo.dto.response.OtpResponse;
import com.example.jwtdemo.entity.OtpPurpose;
import org.springframework.security.authentication.DisabledException;
import com.example.jwtdemo.entity.RefreshToken;
import com.example.jwtdemo.entity.Role;
import com.example.jwtdemo.entity.User;
import com.example.jwtdemo.exception.TokenRefreshException;
import com.example.jwtdemo.exception.UserAlreadyExistsException;
import com.example.jwtdemo.repository.UserRepository;
import com.example.jwtdemo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final OtpService otpService;

    @Transactional
    @Override
    public OtpResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email đã tồn tại: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .enabled(true)
                .emailVerified(false)
                .build();

        userRepository.save(user);

        otpService.issue(user, OtpPurpose.REGISTER);
        return new OtpResponse("Đăng ký thành công. Vui lòng kiểm tra email để xác minh OTP.");
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Nếu sai email/password, AuthenticationManager sẽ ném BadCredentialsException
        // -> được xử lý ở GlobalExceptionHandler
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User không tồn tại sau khi authenticate"));

        return generateAuthResponse(user);
    }

    @Transactional
    @Override
    public AuthResponse refreshToken(String requestRefreshToken) {
        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token không tồn tại"));

        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();
        if (!user.isEnabled()) {
            throw new DisabledException("Tài khoản chưa xác minh hoặc đã bị vô hiệu hóa.");
        }
        String newAccessToken = jwtService.generateAccessToken(user);

        // Rotate refresh token: xoá token cũ, cấp token mới để giảm rủi ro nếu token bị lộ
        refreshTokenService.deleteByUser(user);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .build();
    }

    @Transactional
    @Override
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User không tồn tại"));
        refreshTokenService.deleteByUser(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }
}
