package com.fsa.franchise.product_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");

        if (StringUtils.hasText(userId)) {
            String roleStr = request.getHeader("X-User-Role");
            String permissionsStr = request.getHeader("X-User-Permissions");
            String userName = request.getHeader("X-User-Name");

            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            List<String> permissionList = new ArrayList<>();

            if (StringUtils.hasText(roleStr)) {
                // Roles are usually prefixed with ROLE_ in Spring Security
                String[] roles = roleStr.split(",");
                for (String role : roles) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.trim()));
                }
            }

            if (StringUtils.hasText(permissionsStr)) {
                String[] permissions = permissionsStr.split(",");
                for (String permission : permissions) {
                    authorities.add(new SimpleGrantedAuthority(permission.trim()));
                    permissionList.add(permission.trim());
                }
            }

            UserPrincipal principal = UserPrincipal.builder()
                    .userId(userId)
                    .name(userName)
                    .permissions(permissionList)
                    .build();

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
