package com.tesim.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Anonymous / missing JWT hitting secured endpoints previously became HTTP 403.
 * Treat those as 401 so frontends can clear session and redirect to login.
 */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final RestAuthEntryPoint entryPoint;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean anonymous = auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken;
        if (anonymous) {
            entryPoint.write(response, HttpServletResponse.SC_UNAUTHORIZED, 401, "请先登录");
            return;
        }
        entryPoint.write(response, HttpServletResponse.SC_FORBIDDEN, 403, "无权限");
    }
}
