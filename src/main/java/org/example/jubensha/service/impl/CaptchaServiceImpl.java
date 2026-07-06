package org.example.jubensha.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.jubensha.common.CaptchaUtil;
import org.example.jubensha.common.Result;
import org.example.jubensha.service.CaptchaService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

    // Session中存储验证码的Key前缀
    private static final String SESSION_CODE_PREFIX = "captcha_code_";

    @Override
    public Result<String> getCode(String phone, jakarta.servlet.http.HttpSession session) {
        // 1. 简单校验手机号格式
        if (!StringUtils.hasText(phone) || !phone.matches("^1[3-9]\\d{9}$")) {
            return Result.fail("请输入正确的11位手机号");
        }

        // 2. 生成6位验证码
        String code = CaptchaUtil.generateCode();

        // 3. 将验证码存入Session，5分钟后过期（依赖Session的默认过期时间或手动设置）
        session.setAttribute(SESSION_CODE_PREFIX + phone, code);
        // 可选：手动设置Session中验证码的过期时间（需额外逻辑，这里简化依赖Session全局配置）

        // 4. 直接将验证码返回！这是核心改动
        return Result.success(code);
    }

    @Override
    public boolean verifyCode(String phone, String code, jakarta.servlet.http.HttpSession session) {
        if (!StringUtils.hasText(phone) || !StringUtils.hasText(code)) {
            return false;
        }

        String sessionKey = SESSION_CODE_PREFIX + phone;
        String storedCode = (String) session.getAttribute(sessionKey);

        // 校验匹配
        if (code.equals(storedCode)) {
            // 校验通过，立即移除，防止重复使用
            session.removeAttribute(sessionKey);
            return true;
        }
        return false;
    }
}