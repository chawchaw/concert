package com.chaw.concert.app.infrastructure.web.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ReqResLoggingFilter extends OncePerRequestFilter {

    private final static String URI_API_ROOT = "/api";
    private final static String MDC_EVENT_ID = "eventId";
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain chain)
            throws IOException, ServletException {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(MDC_EVENT_ID, requestId);

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpServletRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpServletResponse);

        Long startTime = System.currentTimeMillis();
        try {
            chain.doFilter(requestWrapper, responseWrapper);
        } catch (Exception e) {
            log.error("Exception occurred while processing request", e);
            throw e;
        } finally {
            Long endTime = System.currentTimeMillis();
            log.info(HttpLogMessage.create(requestWrapper, responseWrapper, endTime - startTime));

            if (!responseWrapper.isCommitted()) {
                responseWrapper.copyBodyToResponse();
            }

            MDC.clear();
        }
    }

    public record HttpLogMessage(String method, String uri, String requestBody, String responseBody,
                                 int status, long durationMs, String params, String headers, String clientIp) {

        public static String create(ContentCachingRequestWrapper requestWrapper, ContentCachingResponseWrapper responseWrapper, long durationMs) {
            String method = requestWrapper.getMethod();
            String uri = requestWrapper.getRequestURI();
            String requestBody = getPrettyJson(getRequestBody(requestWrapper));
            String responseBody = getPrettyJson(getResponseBody(responseWrapper));
            int status = responseWrapper.getStatus();
            String params = getRequestParams(requestWrapper);
            String headers = getRequestHeaders(requestWrapper);
            String clientIp = getClientIp(requestWrapper);

            return new HttpLogMessage(method, uri, requestBody, responseBody, status, durationMs, params, headers, clientIp).toString();
        }

        private static String getPrettyJson(String json) {
            try {
                Object jsonObject = objectMapper.readValue(json, Object.class);
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
            } catch (JsonProcessingException e) {
                return json;
            }
        }

        private static String convertToJson(Map<?, ?> map) {
            try {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(map); // 예쁘게 출력
            } catch (JsonProcessingException e) {
                return "{}"; // 변환 실패 시 빈 JSON 객체 반환
            }
        }

        private static String getRequestBody(ContentCachingRequestWrapper requestWrapper) {
            byte[] buf = requestWrapper.getContentAsByteArray();
            if (buf.length > 0) {
                return new String(buf, 0, buf.length, StandardCharsets.UTF_8);
            }
            return "";
        }

        private static String getResponseBody(ContentCachingResponseWrapper responseWrapper) {
            byte[] buf = responseWrapper.getContentAsByteArray();
            if (buf.length > 0) {
                return new String(buf, 0, buf.length, StandardCharsets.UTF_8);
            }
            return "";
        }

        private static String getRequestParams(ContentCachingRequestWrapper requestWrapper) {
            Map<String, String[]> parameterMap = requestWrapper.getParameterMap();
            if (parameterMap.isEmpty()) {
                return "";
            }

            Map<String, Object> jsonMap = new HashMap<>();
            parameterMap.forEach((key, value) -> jsonMap.put(key, String.join(",", value)));

            return convertToJson(jsonMap);
        }

        private static String getRequestHeaders(HttpServletRequest request) {
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames == null) {
                return "";
            }

            Map<String, String> headersMap = new HashMap<>();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                headersMap.put(headerName, request.getHeader(headerName));
            }

            return convertToJson(headersMap);
        }

        private static String getClientIp(HttpServletRequest request) {
            String clientIp = request.getHeader("X-Forwarded-For");
            if (clientIp == null || clientIp.isEmpty()) {
                clientIp = request.getRemoteAddr();
            }
            return clientIp;
        }

        @Override
        public String toString() {
            StringBuilder logMessage = new StringBuilder("");

            logMessage.append(MessageFormat.format("({0}) [{1}] {2} {3} {4}ms", clientIp, method, uri, status, durationMs));

            if (!params.isEmpty()) {
                logMessage.append("\nrequestParams = " + params);
            }

            if (!requestBody.isEmpty()) {
                logMessage.append("\nrequestBody = " + requestBody);
            }

            if (uri.startsWith(URI_API_ROOT) && !responseBody.isEmpty()) {
                logMessage.append("\nresponseBody = " + responseBody);
            }

            return logMessage.toString();
        }
    }
}
