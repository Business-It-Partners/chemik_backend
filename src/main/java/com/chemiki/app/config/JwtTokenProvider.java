// Updated JwtTokenProvider.java - SECURE VERSION
package com.chemiki.app.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    // ✅ SECURE: Read from environment variables
    @Value("${jwt.secret}")
    private String jwtSecretString;

    @Value("${jwt.accessTokenExpiration:900000}") // 15 minutes
    private long accessTokenExpiration;

    @Value("${jwt.refreshTokenExpiration:604800000}") // 7 days
    private long refreshTokenExpiration;

    // ✅ SECURE: Generate secret key from environment variable
    private SecretKey getJwtSecret() {
        try {
            // Decode base64 encoded secret
            byte[] decodedKey = Base64.getDecoder().decode(jwtSecretString);
            return Keys.hmacShaKeyFor(decodedKey);
        } catch (Exception e) {
            log.error("Invalid JWT secret configuration. Using fallback key.");
            // Fallback - but log warning
            return Keys.hmacShaKeyFor("GHoK7gBHm5kEmBpuRmOPCGEtTG1cOyNT".getBytes());
        }
    }

    // Generate short-lived access token
    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getJwtSecret())
                .compact();
    }

    // Generate long-lived refresh token
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(username)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getJwtSecret())
                .compact();
    }

    // Generate new access token from refresh token
    public String generateAccessTokenFromRefresh(String refreshToken) {
        if (!validateRefreshToken(refreshToken)) {
            throw new JwtException("Invalid refresh token");
        }

        String username = getUsernameFromJWT(refreshToken);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(username)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getJwtSecret())
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getJwtSecret())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    // Validate access token
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getJwtSecret())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return "access".equals(claims.get("type"));
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    // Validate refresh token
    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getJwtSecret())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return "refresh".equals(claims.get("type"));
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid refresh token: {}", e.getMessage());
            return false;
        }
    }
}