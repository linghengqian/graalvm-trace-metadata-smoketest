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
import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * @author linghengqian
 */
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
        JwtParserBuilder jwtParserBuilder = Jwts.parserBuilder().setAllowedClockSkewSeconds(3 * 60).setSigningKey(firstKey);
        assert jwtParserBuilder.build().parseClaimsJws(firstCompactJws).getBody().getSubject().equals("Joe");
        jwtParserBuilder.requireSubject("Joe").build().parseClaimsJws(firstCompactJws);
        jwtParserBuilder.requireIssuer("Aaron").build().parseClaimsJws(firstCompactJws);
        jwtParserBuilder.requireAudience("Abel").build().parseClaimsJws(firstCompactJws);
        // TODO This is an error caused by GSON's own parsing
        // jwtParserBuilder.requireExpiration(secondDate).build().parseClaimsJws(firstCompactJws);
        // jwtParserBuilder.requireNotBefore(firstDate).build().parseClaimsJws(firstCompactJws);
        // jwtParserBuilder.requireIssuedAt(firstDate).build().parseClaimsJws(firstCompactJws);
        jwtParserBuilder.requireId(uuidString).build().parseClaimsJws(firstCompactJws);
        jwtParserBuilder.require("exampleClaim", "Adam").build().parseClaimsJws(firstCompactJws);
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
        Stream.of(SignatureAlgorithm.HS256, SignatureAlgorithm.HS384, SignatureAlgorithm.HS512)
                .map(Keys::secretKeyFor)
                .forEach(secretKey -> {
                    assert Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(
                            Jwts.builder().setSubject("Joe").signWith(secretKey).compact()
                    ).getBody().getSubject().equals("Joe");
                });
        Stream.of(SignatureAlgorithm.ES256, SignatureAlgorithm.ES384, SignatureAlgorithm.ES512,
                        SignatureAlgorithm.RS256, SignatureAlgorithm.RS384, SignatureAlgorithm.RS512,
                        SignatureAlgorithm.PS256, SignatureAlgorithm.PS384, SignatureAlgorithm.PS512)
                .map(Keys::keyPairFor)
                .forEach(keyPair -> {
                    assert Jwts.parserBuilder().setSigningKey(keyPair.getPublic()).build().parseClaimsJws(
                            Jwts.builder().setSubject("Joe").signWith(keyPair.getPrivate()).compact()
                    ).getBody().getSubject().equals("Joe");
                });
    }
}
