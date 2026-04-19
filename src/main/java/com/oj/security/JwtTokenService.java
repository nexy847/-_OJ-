package com.oj.security;

import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtTokenService {
    private final SecretKey secretKey;
    private final long expirationMinutes;

    public JwtTokenService(SecretKey secretKey, long expirationMinutes) {
        this.secretKey = secretKey;
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(String username) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirationMinutes * 60);//单位是秒 总数为分钟
        return Jwts.builder()
                .setSubject(username)//将用户名存入
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String parseUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)//由这个方法对token作校验 完整性 时效性 签名合法性 返回一个Claims对象
                .getBody()
                .getSubject();
    }

    public long getExpirationMinutes() {
        return expirationMinutes;
    }
}
