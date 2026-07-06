package org.example.jubensha.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.jubensha.common.Result;
import org.example.jubensha.service.CaptchaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor
@CrossOrigin // 添加跨域支持
public class CaptchaController {

    private final CaptchaService captchaService;

    /**
     * 获取验证码接口
     */
    @PostMapping("/getCode")
    public Result<String> getCode(@RequestParam String phone, HttpSession session) {
        return captchaService.getCode(phone, session);
    }
}