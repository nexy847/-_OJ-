package com.oj.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService tokenService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenService tokenService, UserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);//拿到Bearer 后面的token
            try {
                String username = tokenService.parseUsername(token);//解析用户名
                UserDetails details = userDetailsService.loadUserByUsername(username);//由username得出用户信息
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());//credentials通常存的是密码 此时token已过校验 无需密码 所以为null
                SecurityContextHolder.getContext().setAuthentication(auth);//往上下文里加入认证信息(用户和身份)
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();//token失效 清空上下文
            }
        }
        filterChain.doFilter(request, response);//放行
    }
}
