package com.handoff.security.util;

import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;

public class JwtKeyGenerator {
    public static void main(String[] args) {
        SecretKey key = Jwts.SIG.HS256.key().build();
        System.out.println(java.util.Base64.getEncoder().encodeToString(key.getEncoded()));
    }
}