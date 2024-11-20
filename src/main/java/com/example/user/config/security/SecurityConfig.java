package com.example.user.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final String urlPrefix = "/api/v1/users";
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource()) // 커스텀 CORS 설정
                )
                .csrf(csrf -> csrf.disable())  // CSRF 비활성화
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                            urlPrefix+"/sign-in",
                            urlPrefix+"/sign-up",
                            urlPrefix+"/duplicateid",
                            urlPrefix+"/duplicatenickname",
                            urlPrefix+"/home",
                            "/error"
                            ).permitAll()
                    .anyRequest().authenticated();
                })
                .formLogin(form -> {
                    form.loginProcessingUrl(urlPrefix+"/sign-in")
                            .successHandler((request, response, authentication) -> {
                                response.setStatus(HttpStatus.OK.value());
                                response.setContentType("application/json");  // 응답 형식을 JSON으로 설정
                                response.getWriter().write("{\"message\": \"Successfully logged in\"}");  // 메시지 반환
                                response.flushBuffer();
                            })
                            .failureHandler((request, response, exception) -> {
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                response.setContentType("application/json");  // 응답 형식을 JSON으로 설정
                                response.getWriter().write("{\"message\": \"Log in Failed\"}");  // 메시지 반환
                                response.flushBuffer();
                            });
                })
                .logout(logout -> {
                    logout.logoutUrl(urlPrefix+"/sign-out")
                            .logoutSuccessHandler((request, response, authentication) -> {
                                response.setStatus(HttpStatus.OK.value());
                                response.setContentType("application/json");  // 응답 형식을 JSON으로 설정
                                response.getWriter().write("{\"message\": \"Successfully logged out\"}");  // 메시지 반환
                                response.flushBuffer();
                            })
                            .permitAll();
                });
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE")); // 허용할 메서드
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type")); // 허용할 헤더
        configuration.setAllowCredentials(true); // 인증 정보 포함 여부
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}