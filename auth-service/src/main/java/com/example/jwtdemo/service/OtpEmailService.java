package com.example.jwtdemo.service;

import com.example.jwtdemo.entity.OtpPurpose;

public interface OtpEmailService {

    void send(String email, String code, OtpPurpose purpose, long ttlSeconds);
}
