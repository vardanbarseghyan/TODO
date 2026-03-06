package com.vardan.todo.service;

import com.vardan.todo.entity.RefreshToken;
import com.vardan.todo.entity.User;
import com.vardan.todo.repository.RefreshTokenRepository;
import com.vardan.todo.security.jwt.JwtProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
//-----------------------------------------------------------------
@Service
@AllArgsConstructor
public class RefreshTokenService {
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    public void deleteByUser(User user)
    {
        refreshTokenRepository.deleteByUser(user);
    }
    public void createRefreshTokenEntity(String refreshToken, User user)
    {
        RefreshToken refreshTokenObj = new RefreshToken();
        refreshTokenObj.setToken(refreshToken);
        refreshTokenObj.setUser(user);
        refreshTokenObj.setExpiryDate(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration()));
        saveRefreshToken(refreshTokenObj);
    }
    public void saveRefreshToken(RefreshToken token) {
        refreshTokenRepository.save(token);
    }
}
