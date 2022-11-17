package com.lingh;

import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.util.Date;
import java.util.UUID;

public class JjwtImplTests {
    @Test
    void testSignedJWTs() {
        SecretKey firstKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String firstCompactJws = Jwts.builder().setSubject("Joe").signWith(firstKey).compact();
        assert Jwts.parserBuilder().setSigningKey(firstKey).build().parseClaimsJws(firstCompactJws).getBody().getSubject().equals("Joe");
        String secretString = Encoders.BASE64.encode(firstKey.getEncoded());
        SecretKey secondKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretString));
        String secondCompactJws = Jwts.builder().setSubject("Joe").signWith(secondKey).compact();
        assert Jwts.parserBuilder().setSigningKey(secondKey).build().parseClaimsJws(secondCompactJws).getBody().getSubject().equals("Joe");
        KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
        String thirdCompactJws = Jwts.builder().setSubject("Joe").signWith(keyPair.getPrivate()).compact();
        assert Jwts.parserBuilder().setSigningKey(keyPair.getPublic()).build().parseClaimsJws(thirdCompactJws).getBody().getSubject().equals("Joe");
    }

    @Test
    void testCreatingAJWS() {
        Date firstDate = new Date();
        Date secondDate = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000L);
        String uuidString = UUID.randomUUID().toString();
        SecretKey firstKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String firstCompactJws = Jwts.builder()
                .setSubject("Joe")
                .setHeaderParam("kid", "myKeyId")
                .setIssuer("Aaron")
                .setAudience("Abel")
                .setExpiration(secondDate)
                .setNotBefore(firstDate)
                .setIssuedAt(firstDate)
                .setId(uuidString)
                .claim("exampleClaim", "Adam")
                .signWith(firstKey, SignatureAlgorithm.HS256)
                .compressWith(CompressionCodecs.GZIP)
                .compact();
        JwtParserBuilder jwtParserBuilder = Jwts.parserBuilder().setAllowedClockSkewSeconds(3 * 60);
        assert jwtParserBuilder.setSigningKey(firstKey).build().parseClaimsJws(firstCompactJws).getBody().getSubject().equals("Joe");
        jwtParserBuilder.requireSubject("Joe").setSigningKey(firstKey).build().parseClaimsJws(firstCompactJws);
        jwtParserBuilder.requireIssuer("Aaron").setSigningKey(firstKey).build().parseClaimsJws(firstCompactJws);
        jwtParserBuilder.requireAudience("Abel").setSigningKey(firstKey).build().parseClaimsJws(firstCompactJws);
        jwtParserBuilder.requireExpiration(secondDate).setSigningKey(firstKey).build().parseClaimsJws(firstCompactJws);
        jwtParserBuilder.requireNotBefore(firstDate).setSigningKey(firstKey).build().parseClaimsJws(firstCompactJws);
        jwtParserBuilder.requireIssuedAt(firstDate).setSigningKey(firstKey).build().parseClaimsJws(firstCompactJws);
        jwtParserBuilder.requireId(uuidString).setSigningKey(firstKey).build().parseClaimsJws(firstCompactJws);
        jwtParserBuilder.require("exampleClaim", "Adam").setSigningKey(firstKey).build().parseClaimsJws(firstCompactJws);
    }

    @Test
    void testCompression() {
        SecretKey firstKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String firstCompactJws = Jwts.builder().setSubject("Joe").signWith(firstKey).compressWith(CompressionCodecs.DEFLATE).compact();
        assert Jwts.parserBuilder().setSigningKey(firstKey).build().parseClaimsJws(firstCompactJws).getBody().getSubject().equals("Joe");
        SecretKey secondKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String secondCompactJws = Jwts.builder().setSubject("Joe").signWith(secondKey).compressWith(CompressionCodecs.GZIP).compact();
        assert Jwts.parserBuilder().setSigningKey(secondKey).build().parseClaimsJws(secondCompactJws).getBody().getSubject().equals("Joe");
    }
}
