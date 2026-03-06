package com.vardan.todo.security.jwt;

import com.vardan.todo.entity.RefreshToken;
import com.vardan.todo.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

//this class must generate access and refresh tokens
//validate tokens, extract information from token
@Service
@RequiredArgsConstructor
public class JwtService {
//    @Autowired
    private final JwtProperties jwtProperties;//because this service needs access to JWT secret key and expiration times.
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
    public String generateAccessToken(User user) {//Proves what you can access.
        SecretKey secretKey = getSigningKey();

        String token = Jwts.builder()
                .subject(user.getEmail())//subject and claim part of payload
                .claim("userId", user.getId())
                .claim("role", user.getRole())
                .issuer("my-backend-app")//just remember to validate it when parsing.
                .audience().add("api-users").and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpiration()))
                .signWith(secretKey, SignatureAlgorithm.HS256)//when we call this the library says: "Okay, the user wants to use HS256. I will automatically create a Header that says {"alg": "HS256", "typ": "JWT"}."
                .compact();//esel ese stanum abc.xyz.ddd
        return token;
    }
    public String generateRefreshToken(User user) {
//        String refreshToken = UUID.randomUUID().toString();
        //then I think in RefreshtokenService we call this method and what return this method assign it in RefreshToken token; token.setToken(returnValueOfMethodGenerateRefreshtpken());
        final SecureRandom secureRandom = new SecureRandom();
        final Base64.Encoder base64Encoder =
                Base64.getUrlEncoder().withoutPadding();
        byte[] randomBytes = new byte[64]; // 512 bits
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
    public String extractEmailFromToken(String token) {
        SecretKey secretKey = getSigningKey();
        //Claims object represents the entire payload (body) of the JWT.
        Claims claim = Jwts.parser()
                .verifyWith(secretKey)//tells in parser use this key to verify the token signature.
                .build()//create parser object, Jwt Parser
                .parseSignedClaims(token)//decode the JWT, verify signature, verify token structure, verify signature correctness
                .getPayload();//extract the payload (claims)
        String email = claim.getSubject();//returns the email stored inside the token
        return email;
    }
    public UUID extractUserIdFromToken(String token) {
        SecretKey secretKey = getSigningKey();
        Claims claim = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        UUID userId = claim.get("userId", UUID.class);
        /*Claims is basically Map<String, Object>
        You added "userId" when creating the access token
        Now you read the "userId" key
        You tell parser: convert it to a UUID object
→       because JJWT supports typed claims extraction*/
        return userId;
    }
    //Validate token (signature, expiration, user)
    public boolean validToken(String token, UserDetails user) {
        SecretKey secretKey = getSigningKey();
        Claims claim = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String email = claim.getSubject();
        if (!email.equals(user.getUsername())) {//return email
            return false;
        }
        Date date = claim.getExpiration();
        if (date.before(new Date(System.currentTimeMillis())))
            return false;
        return true;
    }
}
