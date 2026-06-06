package com.gghiaroni.rabbitride.carservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String ADMIN_HEADER = "X-Admin-Token";
    private static final String INTERNAL_HEADER = "X-Internal-Token";

    private final String adminToken;
    private final String internalToken;

    public ApiKeyAuthenticationFilter(
        @Value("${app.security.admin-token}") String adminToken,
        @Value("${app.security.internal-token}") String internalToken
    ) {
        this.adminToken = adminToken;
        this.internalToken = internalToken;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String adminHeader = request.getHeader(ADMIN_HEADER);
        String internalHeader = request.getHeader(INTERNAL_HEADER);

        if (adminToken.equals(adminHeader)) {
            authenticate("admin", "ROLE_ADMIN");
        } else if (internalToken.equals(internalHeader)) {
            authenticate("internal-service", "ROLE_INTERNAL");
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String principal, String role) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            List.of(new SimpleGrantedAuthority(role))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
