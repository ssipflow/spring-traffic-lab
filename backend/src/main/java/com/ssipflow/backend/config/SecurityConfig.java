package com.ssipflow.backend.config;

import com.ssipflow.backend.jwt.JwtAuthenticationFilter;
import com.ssipflow.backend.jwt.JwtUtil;
import com.ssipflow.backend.service.CustomUserDetailService;
import com.ssipflow.backend.service.JwtBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailService userDetailService;
    private final JwtUtil jwtUtil;
    private final JwtBlacklistService jwtBlacklistService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/api/users/signUp",
                        "/api/ads",
                        "/api/ads/**",
                        "/api/users/login").permitAll()
                .anyRequest().authenticated()
        ).addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailService, jwtBlacklistService), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement( session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) )
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }
}
