package com.chaw.concert.app.domain.common.auth.util;

import com.chaw.concert.app.domain.common.auth.entity.CustomUserDetails;
import com.chaw.concert.app.infrastructure.exception.common.BaseException;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    public static CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }

        throw new BaseException(ErrorType.UNAUTHORIZED, "인증정보가 없습니다.");
    }

    public static Long getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }
}
