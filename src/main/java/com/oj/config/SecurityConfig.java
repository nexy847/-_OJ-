package com.oj.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.oj.security.JwtAuthenticationFilter;
import com.oj.security.JwtTokenService;

import io.jsonwebtoken.security.Keys;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //作为下方jwtTokenService方法的参数
    @Bean
    public SecretKey jwtSecretKey(OjProperties properties) {
        byte[] secret = properties.getSecurity().getJwtSecret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            try {
                java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                secret = digest.digest(secret);//获取sha-256实例并进行杂凑计算
            } catch (java.security.NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-256 not available", e);
            }
        }
        return Keys.hmacShaKeyFor(secret);//包装成secretKey对象
    }

    //在generateToken和parseUsername方法中被调用
    @Bean
    public JwtTokenService jwtTokenService(SecretKey secretKey, OjProperties properties) {
        return new JwtTokenService(secretKey, properties.getSecurity().getJwtExpirationMinutes());
    }

    //先走corsfilter，再走csrfFilter，接着走jwtFilter(JwtAuthenticationFilter类里的过滤链)，接着执行下面的权限判断(基于身份的url进入与否)，/login可进入，然后走UsernamePasswordAuthenticationFilter(controller层的login方法)
    //所有请求 先做权限校验(authorizeHttpRequests) 再进入具体的controller
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            .cors(cors -> {})//允许跨域请求
            .csrf(csrf -> csrf.disable())//不使用cookie(session id)，不担心csrf
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))//不用session(无状态)，每次要带上token(比较方便)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/users").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()//除此之外的请求 须登录才能访问
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);//处理账号密码之前，先解析请求头里的token

        return http.build();
    }

    //用于filterChain方法的cors
    @Bean
    public CorsConfigurationSource corsConfigurationSource(OjProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.getSecurity().getAllowedOrigins());//允许前端url访问后端接口
        configuration.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()//预检请求要放行
        ));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));//允许前端传送和jwt有关的请求头
        configuration.setAllowCredentials(true);//允许携带凭证 Authorization token(因为jwt是手动加在headers里的 false也行)
        configuration.setExposedHeaders(List.of("Authorization"));//暴露响应头 好让前端看到这个凭证

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);//将这些规则应用到所有后端接口上
        return source;
    }

    //没被使用 让项目看着完整些
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
