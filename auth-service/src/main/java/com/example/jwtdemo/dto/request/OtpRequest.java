package com.example.jwtdemo.dto.request;

import com.example.jwtdemo.entity.OtpPurpose;
import jakarta.validation.constraints.*;

public record OtpRequest(@NotBlank @Email String email, @NotNull OtpPurpose purpose) { }
