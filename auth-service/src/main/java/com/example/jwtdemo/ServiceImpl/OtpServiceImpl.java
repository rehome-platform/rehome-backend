package com.example.jwtdemo.ServiceImpl;

import com.example.jwtdemo.service.OtpService;
import com.example.jwtdemo.service.OtpEmailService;
import com.example.jwtdemo.service.RefreshTokenService;

import com.example.jwtdemo.dto.request.OtpRequest;
import com.example.jwtdemo.dto.request.VerifyOtpRequest;
import com.example.jwtdemo.dto.request.ResetPasswordRequest;
import com.example.jwtdemo.dto.response.OtpResponse;
import com.example.jwtdemo.entity.*;
import com.example.jwtdemo.exception.OtpException;
import com.example.jwtdemo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final UserRepository users;
    private final EmailOtpRepository otps;
    private final OtpEmailService otpEmailService;
    private final PasswordEncoder encoder;
    private final RefreshTokenService refreshTokens;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.otp.ttl-seconds:300}") private long ttl;
    @Value("${app.otp.resend-seconds:60}") private long resendSeconds;
    @Value("${app.otp.max-attempts:5}") private int maxAttempts;

    // Caller holds the user row lock (or is registering a new user).
    @Transactional
    @Override
    public void issue(User user, OtpPurpose purpose) {
        String key = user.getId() + ":" + purpose;
        EmailOtp otp = otps.findById(key).orElseGet(EmailOtp::new);
        Instant now = Instant.now();
        if (otp.getSentAt() != null && now.isBefore(otp.getSentAt().plusSeconds(resendSeconds))) {
            throw new OtpException(HttpStatus.TOO_MANY_REQUESTS, "Vui lòng chờ trước khi gửi lại OTP.");
        }
        String code = String.format(Locale.ROOT, "%06d", random.nextInt(1_000_000));
        otp.setId(key);
        otp.setCodeHash(encoder.encode(code));
        otp.setExpiresAt(now.plusSeconds(ttl));
        otp.setSentAt(now);
        otp.setAttempts(0);
        otp.setConsumed(false);
        otp.setResetTokenHash(null);
        otp.setResetExpiresAt(null);
        otps.save(otp);

        otpEmailService.send(user.getEmail(), code, purpose, ttl);
    }

    @Transactional
    @Override
    public OtpResponse resend(OtpRequest request) {
        users.findByEmailForUpdate(request.email()).ifPresent(user -> {
            if ((request.purpose() == OtpPurpose.REGISTER && !user.isEmailVerified())
                    || (request.purpose() == OtpPurpose.FORGOT_PASSWORD && user.isEnabled())) {
                issue(user, request.purpose());
            }
        });
        return new OtpResponse("Nếu tài khoản phù hợp, OTP đã được gửi đến email.");
    }

    // Preserve failed-attempt counters even when returning an invalid-code error.
    @Transactional(noRollbackFor = OtpException.class)
    @Override
    public OtpResponse verify(VerifyOtpRequest request) {
        User user = users.findByEmailForUpdate(request.email()).orElseThrow(this::invalid);
        EmailOtp otp = otps.findById(user.getId() + ":" + request.purpose()).orElseThrow(this::invalid);
        if (otp.isConsumed() || !Instant.now().isBefore(otp.getExpiresAt()) || otp.getAttempts() >= maxAttempts) {
            throw invalid();
        }
        if (!encoder.matches(request.otp(), otp.getCodeHash())) {
            otp.setAttempts(otp.getAttempts() + 1);
            throw invalid();
        }
        if (request.purpose() == OtpPurpose.REGISTER) {
            otp.setConsumed(true);
            user.setEmailVerified(true);
            return new OtpResponse("Xác minh email thành công. Vui lòng đăng nhập.");
        }
        if (!user.isEnabled()) throw invalid();
        otp.setConsumed(true);
        String resetToken = UUID.randomUUID().toString();
        otp.setResetTokenHash(encoder.encode(resetToken));
        otp.setResetExpiresAt(Instant.now().plusSeconds(ttl));
        return new OtpResponse("OTP hợp lệ. Sử dụng resetToken để đặt lại mật khẩu.", resetToken);
    }

    @Transactional
    @Override
    public OtpResponse resetPassword(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new OtpException(HttpStatus.BAD_REQUEST, "Mật khẩu xác nhận không khớp.");
        }
        User user = users.findByEmailForUpdate(request.email()).orElseThrow(this::invalid);
        EmailOtp otp = otps.findById(user.getId() + ":" + OtpPurpose.FORGOT_PASSWORD).orElseThrow(this::invalid);
        if (!user.isEnabled() || otp.getResetTokenHash() == null || otp.getResetExpiresAt() == null
                || !Instant.now().isBefore(otp.getResetExpiresAt())
                || !encoder.matches(request.resetToken(), otp.getResetTokenHash())) throw invalid();
        user.setPassword(encoder.encode(request.newPassword()));
        otp.setResetTokenHash(null);
        otp.setResetExpiresAt(null);
        refreshTokens.deleteByUser(user);
        return new OtpResponse("Đặt lại mật khẩu thành công. Vui lòng đăng nhập.");
    }

    private OtpException invalid() {
        return new OtpException(HttpStatus.BAD_REQUEST, "Mã xác thực không hợp lệ, hết hạn hoặc đã sử dụng.");
    }
}
