package com.github.platform.core.workflow.infra.filter;

import com.github.platform.core.auth.util.LoginUserInfoUtil;
import com.github.platform.core.common.utils.StringUtils;
import com.github.platform.core.workflow.infra.util.FlowableUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;

/**
 * 流程引擎过滤器
 * @author: yxkong
 * @date: 2023/9/21 10:29 AM
 * @version: 1.0
 */
public class FlowableWebFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // 设置工作流的用户
            String loginName = LoginUserInfoUtil.getLoginName();
            if (StringUtils.isNotEmpty(loginName)){
                FlowableUtil.setAuthenticatedLoginName(loginName);
            }
            filterChain.doFilter(request, response);
        } finally {
            // 清理
            FlowableUtil.clearAuthenticatedLoginName();
        }
    }
}
