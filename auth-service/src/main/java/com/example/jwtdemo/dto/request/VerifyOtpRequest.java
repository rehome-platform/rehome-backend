package com.example.jwtdemo.dto.request;

import com.example.jwtdemo.entity.OtpPurpose;
import jakarta.validation.constraints.*;

public record VerifyOtpRequest(@NotBlank @Email String email, @NotNull OtpPurpose purpose,
                               @NotBlank @Pattern(regexp = "[0-9]{6}") String otp) { }
