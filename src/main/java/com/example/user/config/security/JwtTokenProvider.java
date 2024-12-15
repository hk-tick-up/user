package com.example.user.config.security;

import com.example.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {
    private final String secretKey = "mysecretkeyhavetomovedoutsideofcodeandhavetochangedtorandomstring";
    private final long validityInMilliseconds = 90L * 24 * 60 * 60 * 1000; // 90일

    public String createToken(String username, Set<User.UserRole> roles, boolean deletionRequested) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);
        claims.put("deletionRequested", deletionRequested);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // JWT에서 권한 추출
    @SuppressWarnings("unchecked")
    public List<GrantedAuthority> getAuthorities(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        List<String> roles = (List<String>) claims.get("roles");

        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public boolean isDeletionRequested(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("deletionRequested", Boolean.class); // deletionRequested 값을 boolean으로 읽음
    }
}
