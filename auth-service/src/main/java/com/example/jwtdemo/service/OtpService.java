package com.example.jwtdemo.service;

import com.example.jwtdemo.dto.request.OtpRequest;
import com.example.jwtdemo.dto.request.VerifyOtpRequest;
import com.example.jwtdemo.dto.request.ResetPasswordRequest;
import com.example.jwtdemo.dto.response.OtpResponse;
import com.example.jwtdemo.entity.User;
import com.example.jwtdemo.entity.OtpPurpose;

public interface OtpService {

    void issue(User user, OtpPurpose purpose);

    OtpResponse resend(OtpRequest request);

    OtpResponse verify(VerifyOtpRequest request);

    OtpResponse resetPassword(ResetPasswordRequest request);
}
