package org.example.jubensha.config;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 字符编码过滤器
 * 确保所有请求和响应都使用 UTF-8 编码
 */
@Component("customEncodingFilter")
@WebFilter("/*")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CharacterEncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        // 设置请求编码
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
        // 设置响应编码
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
        // 强制设置 Content-Type 包含 UTF-8
        if (response instanceof jakarta.servlet.http.HttpServletResponse) {
            jakarta.servlet.http.HttpServletResponse httpResponse = 
                (jakarta.servlet.http.HttpServletResponse) response;
            httpResponse.setContentType("application/json;charset=UTF-8");
        }
        
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化
    }

    @Override
    public void destroy() {
        // 销毁
    }
}
