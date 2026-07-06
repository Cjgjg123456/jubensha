package org.example.jubensha.service;

import org.example.jubensha.common.Result;

public interface CaptchaService {
    // 获取验证码（直接返回验证码内容）
    Result<String> getCode(String phone, jakarta.servlet.http.HttpSession session);
    // 校验验证码
    boolean verifyCode(String phone, String code, jakarta.servlet.http.HttpSession session);
}