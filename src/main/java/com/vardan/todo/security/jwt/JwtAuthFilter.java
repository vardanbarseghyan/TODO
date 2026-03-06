package com.vardan.todo.security.jwt;
import com.vardan.todo.security.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;//to check the token.
    private final UserDetailsService userDetailsService;//to load the user if the token is good
    private final TokenBlacklistService tokenBlacklistService;

    //When a request comes to your backend
    //The request goes through the Spring Security filter chain.
    //
    //HTTP Request
    //     ↓
    //Spring Security Filter Chain
    //     ↓
    //JwtAuthFilter
    //     ↓
    //Controller

    //Spring Security calls it(doFilterInternal) automatically.
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        //the first thing we do is look at the Authorization header. If there is no token, we just let the request move to the next filter (where it will likely be blocked if the URL is private).
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 1.   If header is empty or doesn't start with "Bearer ", move to next filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2. Extract the token (substring 7 removes "Bearer ")
            jwt = authHeader.substring(7);

            // 3. Extract email from the token using your JwtService
            userEmail = jwtService.extractEmailFromToken(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {//Is the user already logged in for this specific request?
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (tokenBlacklistService.isBlacklisted(jwt)) {//check Redis if token is blacklisted
                    filterChain.doFilter(request, response);//this mean`stop authentication, continue filter chain, Spring will reject request.
                    return;
                }//
                // If token is valid, we "tell" Spring Security that this user is okay
                if (jwtService.validToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,//passwords
                            userDetails.getAuthorities()
                    );//creating the official ID card`proof of authentication, but it doesn't "tell" Spring yet.

                    // Give Spring more details about the request (like IP address)
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Update the Security Context, This is the part where you actually authenticate the user for the current request. You have to put that "ID Card" into Spring's "active pocket" (the Security Context).
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    //For the duration of this specific request, Spring will look in this "pocket" to see who is logged in. When the request is finished, Spring empties the pocket.
                }
            }

            // ALWAYS call the next filter at the end!
            filterChain.doFilter(request, response);
        }
        catch(Exception e) {
            // token is invalid, just continue to next filter
            // Spring Security will handle the 401
            filterChain.doFilter(request, response);
            return;
        }
    }
}
