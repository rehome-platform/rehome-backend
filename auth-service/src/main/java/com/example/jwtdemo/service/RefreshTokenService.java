package com.example.jwtdemo.service;

import com.example.jwtdemo.entity.RefreshToken;
import com.example.jwtdemo.entity.User;
import java.util.Optional;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    Optional<RefreshToken> findByToken(String token);

    RefreshToken verifyExpiration(RefreshToken token);

    void revokeAllUserTokens(User user);

    void deleteByUser(User user);
}
