package com.sinohealth.common.filter;

import com.sinohealth.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Repeatable 过滤器
 */
@Slf4j
public class RepeatableFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            ServletRequest requestWrapper = null;
            if (request instanceof HttpServletRequest
                    && StringUtils.startsWithIgnoreCase(request.getContentType(), MediaType.APPLICATION_JSON_VALUE)) {
                requestWrapper = new RepeatedlyRequestWrapper((HttpServletRequest) request, response);
            }
            if (null == requestWrapper) {
                chain.doFilter(request, response);
            } else {
                chain.doFilter(requestWrapper, response);
            }
        } catch (Exception e) {
            if (request instanceof HttpServletRequest) {
                HttpServletRequest sr = (HttpServletRequest) request;
                log.error("uri={}", sr.getRequestURI());
            }
            throw e;
        }
    }

    @Override
    public void destroy() {

    }
}
