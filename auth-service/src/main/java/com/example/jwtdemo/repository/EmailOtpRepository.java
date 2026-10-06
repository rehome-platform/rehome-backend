package com.example.jwtdemo.repository;

import com.example.jwtdemo.entity.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, String> { }
