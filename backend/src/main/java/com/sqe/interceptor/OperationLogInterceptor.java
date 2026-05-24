package com.sqe.interceptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sqe.entity.SysLog;
import com.sqe.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 记录关键写操作日志。
 */
@Component
public class OperationLogInterceptor implements HandlerInterceptor {

    public static final String START_TIME = "operationLogStartTime";
    public static final String RESULT_CODE = "operationLogResultCode";
    public static final String RESULT_MESSAGE = "operationLogResultMessage";
    private static final int MAX_TEXT_LENGTH = 500;

    @Autowired
    private LogService logService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (shouldRecord(request)) {
            request.setAttribute(START_TIME, System.currentTimeMillis());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (!shouldRecord(request)) {
            return;
        }

        Long startTime = (Long) request.getAttribute(START_TIME);
        long costTime = startTime == null ? 0 : System.currentTimeMillis() - startTime;
        OperationInfo operationInfo = resolveOperation(request);

        SysLog log = new SysLog();
        fillCurrentUser(log);
        log.setModule(operationInfo.module);
        log.setOperation(operationInfo.operation);
        log.setMethod(request.getMethod());
        log.setParams(buildParams(request));
        log.setIp(getClientIp(request));
        Integer resultCode = (Integer) request.getAttribute(RESULT_CODE);
        String resultMessage = (String) request.getAttribute(RESULT_MESSAGE);
        boolean success = ex == null && response.getStatus() < 400 && (resultCode == null || resultCode == 200);
        log.setResult(success ? "SUCCESS" : "FAIL");
        log.setErrorMsg(success ? null : truncate(ex == null ? resultMessage : ex.getMessage(), MAX_TEXT_LENGTH));
        log.setCostTime(costTime);
        logService.save(log);
    }

    private boolean shouldRecord(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        if (!Arrays.asList("POST", "PUT", "DELETE").contains(method)) {
            return false;
        }
        return uri.startsWith("/api/") && !uri.startsWith("/api/auth/");
    }

    private void fillCurrentUser(SysLog log) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.setUsername("unknown");
            return;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            log.setUserId((Long) principal);
        }
        Object details = authentication.getDetails();
        if (details instanceof String[]) {
            String[] values = (String[]) details;
            if (values.length > 0) {
                log.setUsername(values[0]);
            }
        }
        if (!StringUtils.hasText(log.getUsername())) {
            log.setUsername(authentication.getName());
        }
    }

    private OperationInfo resolveOperation(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        if (uri.startsWith("/api/user/resetPwd")) {
            return new OperationInfo("用户管理", "重置密码");
        }
        if (uri.startsWith("/api/user/status")) {
            return new OperationInfo("用户管理", "修改用户状态");
        }
        if (uri.startsWith("/api/user")) {
            return new OperationInfo("用户管理", methodAction(method) + "用户");
        }
        if (uri.startsWith("/api/student")) {
            return new OperationInfo("学生管理", methodAction(method) + "学生信息");
        }
        if (uri.startsWith("/api/class")) {
            return new OperationInfo("班级管理", methodAction(method) + "班级");
        }
        if (uri.startsWith("/api/evaluation")) {
            return new OperationInfo("评价管理", methodAction(method) + resolveEvaluationName(uri));
        }
        if (uri.startsWith("/api/import")) {
            return new OperationInfo("数据导入", "导入评价数据");
        }
        if (uri.startsWith("/api/cluster")) {
            return new OperationInfo("聚类分析", "执行聚类分析");
        }
        if (uri.startsWith("/api/notice")) {
            return new OperationInfo("通知管理", methodAction(method) + "通知");
        }
        if (uri.startsWith("/api/log/clean")) {
            return new OperationInfo("操作日志", "清理操作日志");
        }
        return new OperationInfo("系统操作", methodAction(method) + "数据");
    }

    private String methodAction(String method) {
        if ("POST".equals(method)) {
            return "新增";
        }
        if ("PUT".equals(method)) {
            return "修改";
        }
        if ("DELETE".equals(method)) {
            return "删除";
        }
        return "操作";
    }

    private String resolveEvaluationName(String uri) {
        if (uri.contains("/moral")) {
            return "德育评价";
        }
        if (uri.contains("/academic")) {
            return "智育评价";
        }
        if (uri.contains("/physical")) {
            return "体育评价";
        }
        if (uri.contains("/art")) {
            return "美育评价";
        }
        if (uri.contains("/practice")) {
            return "劳动评价";
        }
        return "评价";
    }

    private String buildParams(HttpServletRequest request) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("path", request.getRequestURI());
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (!parameterMap.isEmpty()) {
            Map<String, Object> params = new LinkedHashMap<>();
            parameterMap.forEach((key, value) -> params.put(key, sanitizeValue(key, String.join(",", value))));
            summary.put("query", params);
        }

        if (isMultipart(request)) {
            summary.put("body", "[multipart file omitted]");
        } else {
            String body = getCachedBody(request);
            if (StringUtils.hasText(body)) {
                summary.put("body", sanitizeBody(body));
            }
        }
        try {
            return truncate(objectMapper.writeValueAsString(summary), 1000);
        } catch (Exception e) {
            return "{}";
        }
    }

    private boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    private String getCachedBody(HttpServletRequest request) {
        if (!(request instanceof ContentCachingRequestWrapper)) {
            return "";
        }
        byte[] content = ((ContentCachingRequestWrapper) request).getContentAsByteArray();
        if (content.length == 0) {
            return "";
        }
        return new String(content, StandardCharsets.UTF_8);
    }

    private Object sanitizeBody(String body) {
        try {
            JsonNode jsonNode = objectMapper.readTree(body);
            return sanitizeJson(jsonNode);
        } catch (Exception e) {
            return truncate(body, MAX_TEXT_LENGTH);
        }
    }

    private JsonNode sanitizeJson(JsonNode node) {
        if (node.isObject()) {
            ObjectNode copy = objectMapper.createObjectNode();
            node.fields().forEachRemaining(entry -> {
                if (isSensitive(entry.getKey())) {
                    copy.put(entry.getKey(), "***");
                } else {
                    copy.set(entry.getKey(), sanitizeJson(entry.getValue()));
                }
            });
            return copy;
        }
        if (node.isArray()) {
            ArrayNode copy = objectMapper.createArrayNode();
            node.forEach(item -> copy.add(sanitizeJson(item)));
            return copy;
        }
        if (node.isTextual()) {
            return objectMapper.getNodeFactory().textNode(truncate(node.asText(), MAX_TEXT_LENGTH));
        }
        return node;
    }

    private Object sanitizeValue(String key, String value) {
        return isSensitive(key) ? "***" : truncate(value, MAX_TEXT_LENGTH);
    }

    private boolean isSensitive(String key) {
        String lowerKey = key.toLowerCase();
        return lowerKey.contains("password")
                || lowerKey.contains("token")
                || lowerKey.contains("secret")
                || lowerKey.contains("authorization")
                || lowerKey.contains("private")
                || lowerKey.contains("credential");
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        return StringUtils.hasText(ip) ? ip : request.getRemoteAddr();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    private static class OperationInfo {
        private final String module;
        private final String operation;

        private OperationInfo(String module, String operation) {
            this.module = module;
            this.operation = operation;
        }
    }
}
