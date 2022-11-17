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
import java.util.stream.Stream;

public class JjwtImplTests {
    @Test
    void testSignedJWTs() {
        SecretKey firstKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        assert Jwts.parserBuilder().setSigningKey(firstKey).build().parseClaimsJws(
                Jwts.builder().setSubject("Joe").signWith(firstKey).compact()
        ).getBody().getSubject().equals("Joe");
        String secretString = Encoders.BASE64.encode(firstKey.getEncoded());
        SecretKey secondKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretString));
        assert Jwts.parserBuilder().setSigningKey(secondKey).build().parseClaimsJws(
                Jwts.builder().setSubject("Joe").signWith(secondKey).compact()
        ).getBody().getSubject().equals("Joe");
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
        assert Jwts.parserBuilder().setSigningKey(firstKey).build().parseClaimsJws(
                Jwts.builder().setSubject("Joe").signWith(firstKey).compressWith(CompressionCodecs.DEFLATE).compact()
        ).getBody().getSubject().equals("Joe");
        assert Jwts.parserBuilder().setSigningKey(firstKey).build().parseClaimsJws(
                Jwts.builder().setSubject("Joe").signWith(firstKey).compressWith(CompressionCodecs.GZIP).compact()
        ).getBody().getSubject().equals("Joe");
    }

    @Test
    void testSignatureAlgorithms() {
        SecretKey firstKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        SecretKey secondKey = Keys.secretKeyFor(SignatureAlgorithm.HS384);
        SecretKey thirdKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        KeyPair firstKeyPair = Keys.keyPairFor(SignatureAlgorithm.ES256);
        KeyPair secondKeyPair = Keys.keyPairFor(SignatureAlgorithm.ES384);
        KeyPair thirdKeyPair = Keys.keyPairFor(SignatureAlgorithm.ES512);
        KeyPair fourthKeyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
        KeyPair fifthKeyPair = Keys.keyPairFor(SignatureAlgorithm.RS384);
        KeyPair sixthKeyPair = Keys.keyPairFor(SignatureAlgorithm.RS512);
        KeyPair seventhKeyPair = Keys.keyPairFor(SignatureAlgorithm.PS256);
        KeyPair eighthKeyPair = Keys.keyPairFor(SignatureAlgorithm.PS384);
        KeyPair ninthKeyPair = Keys.keyPairFor(SignatureAlgorithm.PS512);
        Stream.of(firstKey, secondKey, thirdKey).forEach(secretKey -> {
            assert Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(
                    Jwts.builder().setSubject("Joe").signWith(secretKey).compact()
            ).getBody().getSubject().equals("Joe");
        });
        Stream.of(firstKeyPair, secondKeyPair, thirdKeyPair, fourthKeyPair, fifthKeyPair,
                sixthKeyPair, seventhKeyPair, eighthKeyPair, ninthKeyPair).forEach(keyPair -> {
            assert Jwts.parserBuilder().setSigningKey(keyPair.getPublic()).build().parseClaimsJws(
                    Jwts.builder().setSubject("Joe").signWith(keyPair.getPrivate()).compact()
            ).getBody().getSubject().equals("Joe");
        });
    }
}
