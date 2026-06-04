package com.example.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSignKey() {

        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {

        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {

        return extractAllClaims(token).get("userId", Long.class);
    }

    public String extractRole(String token) {

        return extractAllClaims(token).get("role", String.class);
    }

    public Date extractExpiration(String token) {

        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {

        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token) {

        try {
            Claims claims = extractAllClaims(token);

            return !claims.getExpiration().before(new Date());
        }

        catch (Exception ex) {
            return false;
        }
    }
}
