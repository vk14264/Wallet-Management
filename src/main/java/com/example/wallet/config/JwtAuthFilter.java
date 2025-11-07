package com.example.wallet.config;

import com.example.wallet.service.TokenService;
import com.example.wallet.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserService userDetailsService;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        log.info("Incoming request: {} {}", request.getMethod(), path);

        // Let public endpoints pass
        if (path.startsWith("/auth/") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            log.debug("Skipping JWT check for public endpoint");
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        String token = header.substring(7);
        try {
            if (tokenService.isAccessTokenBlacklisted(token)) {
                log.warn("Token is blacklisted");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token is blacklisted");
                return;
            }

            Claims claims = jwtUtil.validateAndGetClaims(token);
            String username = claims.getSubject();
            log.debug("Token valid for user: {}", username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var userDetails = userDetailsService.loadUserByUsername(username);
                var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("Authentication set in SecurityContext for user: {}", username);
            }

        } catch (Exception ex) {
            log.error("JWT validation failed: {}", ex.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid token");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
