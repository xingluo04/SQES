package com.sqe.config;

import com.sqe.common.Result;
import com.sqe.interceptor.OperationLogInterceptor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 将统一响应结果暴露给操作日志拦截器，用于区分业务成功和失败。
 */
@ControllerAdvice
public class OperationResultAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof Result && request instanceof ServletServerHttpRequest) {
            Result<?> result = (Result<?>) body;
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
            servletRequest.setAttribute(OperationLogInterceptor.RESULT_CODE, result.getCode());
            servletRequest.setAttribute(OperationLogInterceptor.RESULT_MESSAGE, result.getMessage());
        }
        return body;
    }
}
