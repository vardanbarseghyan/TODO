package com.vardan.todo.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//This class simply reads values from application.yml:
//jwt secret key
//access token expiration
//refresh token expiration
//Instead of hardcoding values in code
//for But for multiple related fields we always use`
@ConfigurationProperties(prefix = "app.jwt")//Tells Spring:"Bind everything under jwt: in application.yml into this clas
@Component
@Getter
@Setter
public class JwtProperties {
//    @Value("${app.jwt.secret}")//Value kogtagorceinq ete mihatik field liner.
    private String secret;
    private long accessTokenExpiration;
    private long refreshTokenExpiration;
}
