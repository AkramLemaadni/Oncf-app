package com.oncf.oncf.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.issuer:oncf-app}") // Default issuer if not set
    private String jwtIssuer;

    @Value("${jwt.audience:oncf-users}") // Default audience if not set
    private String jwtAudience;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        // Add issuer and audience claims
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .setIssuer(jwtIssuer)
                .setAudience(jwtAudience)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userEmail = extractUsername(token);
        try {
            Claims claims = extractAllClaims(token);
            // Validate issuer and audience
            boolean issuerValid = jwtIssuer.equals(claims.getIssuer());
            boolean audienceValid = jwtAudience.equals(claims.getAudience());
            return (userEmail.equals(userDetails.getUsername())) && !isTokenExpired(token) && issuerValid && audienceValid;
        } catch (Exception e) {
            // Log error if needed, but do not leak details to client
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // Optionally log the error for debugging
            // e.g., logger.warn("Invalid JWT: " + e.getMessage());
            throw new RuntimeException("Invalid or expired JWT token");
        }
    }

    private Key getSigningKey() {
        // Ensure the secret key is strong enough for HS256 (at least 32 bytes)
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalArgumentException("JWT secret key must be at least 32 characters long and not null");
        }
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
} 