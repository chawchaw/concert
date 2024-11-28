package com.chaw.concert.app.infrastructure.web.filter;

import com.chaw.concert.app.domain.common.auth.entity.CustomUserDetails;
import com.chaw.concert.app.domain.common.auth.usecase.CustomUserDetailsService;
import com.chaw.concert.app.domain.common.auth.util.JwtUtil;
import com.chaw.concert.app.infrastructure.exception.common.ErrorType;
import com.chaw.concert.app.infrastructure.exception.handler.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final String MDC_USER_ID = "userId";

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        if (token == null) {
            handleException(response, ErrorType.UNAUTHORIZED, "인증 토큰이 필요합니다.");
            return;
        }

        if (!jwtUtil.validateToken(token)) {
            handleException(response, ErrorType.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
            return;
        }

        String username = jwtUtil.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Long userId = ((CustomUserDetails) userDetails).getId();
        MDC.put(MDC_USER_ID, userId.toString());

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        usernamePasswordAuthenticationToken
                .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        List<String> excludePaths = List.of(
                "/api/v1/auth/login",
                "/api/v1/auth/join",
                "/swagger-ui/",  // Swagger UI 경로
                "/v3/api-docs",  // OpenAPI 문서 경로
                "/swagger-ui.html",  // Swagger 기본 HTML 경로
                "/actuator"  // Prometheus 경로
        );

        return excludePaths.stream().anyMatch(path::startsWith);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void handleException(HttpServletResponse response, ErrorType errorType, String message) throws IOException {
        HttpStatus httpStatus = errorType.getHttpStatus();
        GlobalExceptionHandler.ExceptionResponse exceptionResponse = new GlobalExceptionHandler.ExceptionResponse(message, httpStatus);
        String jsonResponse = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(exceptionResponse);

        response.setStatus(httpStatus.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}
