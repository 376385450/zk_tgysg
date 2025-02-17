
package com.sinohealth.system.filter;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.sinohealth.common.constant.CommonConstants;
import com.sinohealth.common.constant.LogConstant;
import com.sinohealth.common.core.domain.entity.SysUser;
import com.sinohealth.common.core.domain.model.LoginUser;
import com.sinohealth.common.utils.ServletUtils;
import com.sinohealth.common.utils.SinoipaasUtils;
import com.sinohealth.common.utils.dto.SinoPassUserDTO;
import com.sinohealth.framework.web.service.TokenService;
import com.sinohealth.system.event.EventPublisher;
import com.sinohealth.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

/**
 * @Author Rudolph
 * @Date 2022-05-23 9:48
 * @Desc
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@WebFilter(filterName = "contextFilter", urlPatterns = "/*")
public class ContextFilter implements Filter {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private EventPublisher eventPublisher;


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        MDC.put(LogConstant.TRACE_ID, LogConstant.genTraceId());

        // 包裹 request
        ServletRequest requestWrapper = requestWrap(request);

        // 获取当前登录用户
        LoginUser loginUser = getLoginUser();

        // 设置当前用户信息, 企业账号信息, 部门信息
        setUserAndDepartmentInfo(loginUser);

        // 设置分页信息
        setPageInfo(request);

        // 过滤器下个节点
        doFilter(request, response, chain, requestWrapper);
    }

    private ServletRequest requestWrap(ServletRequest request) throws IOException {
        ServletRequest requestWrapper;
        HttpServletRequest req = (HttpServletRequest) request;
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("multipart/form-data")) {
            MultipartResolver resolver = new CommonsMultipartResolver(req.getSession().getServletContext());
            // 将转化后的 request 放入过滤链中
            requestWrapper = resolver.resolveMultipart(req);
            return requestWrapper;
        } else {
            // 这行代码不能删，否则 druid 的登录校验服务ResourceServlet会拿不到From表单的用户名和密码
            // 因为下面代码做了拷贝Request输入流的动作，Request对象被标记为 usingInputStream 为true
            // druid的 com.alibaba.druid.support.http.ResourceServlet.service 再去拿参数就会认为解析完了，实际上是没解析，然后拿不到参数
            request.getParameter("loginUsername");
            requestWrapper = new BodyReaderWrapper((HttpServletRequest) request);
            return requestWrapper;
        }
    }

    private LoginUser getLoginUser() {
        return tokenService.getLoginUser(ServletUtils.getRequest());
    }

    private void doFilter(ServletRequest request, ServletResponse response, FilterChain chain, ServletRequest requestWrapper) throws IOException, ServletException {
        if (requestWrapper == null) {
            chain.doFilter(request, response);
        } else {
            chain.doFilter(requestWrapper, response);
        }
    }

    private void setUserAndDepartmentInfo(LoginUser loginUser) {
        if (ObjectUtils.isNotNull(loginUser)) {
            SysUser user = userService.selectUserById(loginUser.getUser().getUserId());

            ThreadContextHolder.setSysUser(user);

            SinoPassUserDTO orgUserInfo = null;
            try {
                if (ObjectUtils.isNotNull(user.getOrgUserId())) {
                    orgUserInfo = Optional.ofNullable(SinoipaasUtils.mainEmployeeSelectbyid(user.getOrgUserId())).orElse(createEmptyOrgUser());
                }
            } catch (NullPointerException e) {
                log.error("异常捕获", e);
                orgUserInfo = createEmptyOrgUser();
            }

            ThreadContextHolder.getParams().put(CommonConstants.ORG_USER_INFO, orgUserInfo);
        }
    }


    private SinoPassUserDTO createEmptyOrgUser() {
        return new SinoPassUserDTO();
    }


    private void setPageInfo(ServletRequest request) {
        if (ObjectUtils.isNotNull(request.getParameter(CommonConstants.PAGESIZE),
                request.getParameter(CommonConstants.PAGENUM))) {
            ThreadContextHolder.getParams().put(CommonConstants.PAGESIZE, request.getParameter(CommonConstants.PAGESIZE));
            ThreadContextHolder.getParams().put(CommonConstants.PAGENUM, request.getParameter(CommonConstants.PAGENUM));
        } else {
            ThreadContextHolder.getParams().put(CommonConstants.PAGESIZE, 10);
            ThreadContextHolder.getParams().put(CommonConstants.PAGENUM, 1);
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

}
