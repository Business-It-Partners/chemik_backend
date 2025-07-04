
// Purpose: Handles JWT token generation, validation, and username extraction for access and refresh tokens.
// Note: Uses Jwts.parserBuilder() for jjwt 0.12.6. Ensure the jjwt dependency is correctly resolved in pom.xml.
// If you encounter errors with parserBuilder(), verify the jjwt version and Maven dependency resolution.

package com.chemiki.app.config;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final SecretKey jwtSecret = Keys.hmacShaKeyFor("GHoK7gBHm5kEmBpuRmOPCGEtTG1cOyNT".getBytes());

    @Value("${jwt.accessTokenExpiration:3600000}") // 1 hour
    private long accessTokenExpiration;

    @Value("${jwt.refreshTokenExpiration:604800000}") // 7 days
    private long refreshTokenExpiration;

    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtSecret)
                .compact();
    }

    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtSecret)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
