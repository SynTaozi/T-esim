package com.tesim.security;

import com.tesim.common.BizException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long requireMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof MemberPrincipal principal)) {
            throw new BizException(401, "请先登录");
        }
        return principal.getId();
    }

    public static Long memberIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof MemberPrincipal principal) {
            return principal.getId();
        }
        return null;
    }
}
