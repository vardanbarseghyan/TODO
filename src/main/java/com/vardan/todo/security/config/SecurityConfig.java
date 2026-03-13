package com.vardan.todo.security.config;

import com.vardan.todo.security.details.CustomUserDetailsService;
import com.vardan.todo.security.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration//sa partadira erb file-um ogtagorcum enq @Bean annotationy, ete sa chlini @Bean-y uxxaki kignorvi u et type-i instance chi haytnvi IOC containerum.
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    // Tool 1: The Scrambler (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Tool 2: The Manager (Handles the login logic)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Tool 3:
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        //The provider has two tools: your CustomUserDetailsService and the BCryptPasswordEncoder
        authProvider.setUserDetailsService(userDetailsService);//UserDetailsService to find the user in db
        authProvider.setPasswordEncoder(passwordEncoder());//the PasswordEncoder to verify the password.
        return authProvider;
    }

    //ABOUT CORS(Cross Origin ...)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Which frontend origins are allowed to call your backend
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));

        // Which HTTP methods are allowed
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Which headers are allowed (Authorization is important for JWT)
        configuration.setAllowedHeaders(List.of("*"));

        // Allow sending cookies and Authorization headers
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 1. Disable CSRF (not needed for JWT), Don't look for CSRF tokens in cookies; we are using JWT headers.
                .csrf(csrf -> csrf.disable())

                // 2. Define which URLs are public vs private
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/v1/auth/**").permitAll() // Public: Login/Register/refresh
                        .requestMatchers("/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()                  // Private: Everything else
                )

                // 3.Spring will now forget the user the millisecond the request ends.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4 : Tell Spring Security to use our custom handlers
                // instead of its default empty-body 403 response.
                //
                // exceptionHandling() is where you configure what happens when
                // Spring Security needs to reject a request. It has two hooks:
                //
                //   authenticationEntryPoint → called when user is NOT authenticated (no token)
                //                              → our handler returns 401 with clean JSON
                //
                //   accessDeniedHandler      → called when user IS authenticated but lacks permission
                //                              → our handler returns 403 with clean JSON
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                // 5. Tell Spring which Provider to use
                .authenticationProvider(authenticationProvider())

                // 6. Place your JWT Guard BEFORE the standard login filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
