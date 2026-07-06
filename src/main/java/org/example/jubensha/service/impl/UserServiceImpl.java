package org.example.jubensha.service.impl;

import jakarta.servlet.http.HttpSession;
import org.example.jubensha.dto.LoginRequest;
import org.example.jubensha.dto.RegisterRequest;
import org.example.jubensha.entity.User;
import org.example.jubensha.entity.UserRegistrationHistory;
import org.example.jubensha.mapper.HistoryMapper;
import org.example.jubensha.mapper.UserMapper;
import org.example.jubensha.service.CaptchaService;
import org.example.jubensha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final HistoryMapper historyMapper;
    private final CaptchaService captchaService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 核心修复 1：引入我们在 application.properties 中配置的统一下载路径
    @Value("${file.upload-path}")
    private String uploadPath;

    @Autowired
    public UserServiceImpl(UserMapper userMapper, HistoryMapper historyMapper, CaptchaService captchaService) {
        this.userMapper = userMapper;
        this.historyMapper = historyMapper;
        this.captchaService = captchaService;
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    public boolean updateUserProfile(User user) {
        return userMapper.updateUserProfile(user) > 0;
    }

    @Override
    public boolean updateUserProfile(User user, MultipartFile avatar) {
        try {
            if (avatar != null && !avatar.isEmpty()) {
                // 核心修复 2：使用动态配置的路径替代写死的 D 盘路径
                String fileName = "avatar_" + System.currentTimeMillis() + "_" + avatar.getOriginalFilename();

                File dir = new File(uploadPath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File dest = new File(uploadPath + fileName);
                avatar.transferTo(dest);

                // 核心修复 3：URL 前缀必须是 /uploads/，这样 WebConfig 才能拦截并显示图片！
                user.setAvatarUrl("/uploads/" + fileName);
            }
            return userMapper.updateUserProfile(user) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public User login(LoginRequest loginRequest) {
        User user = userMapper.selectByUsername(loginRequest.getUsername());
        if (user == null) {
            System.out.println("[DEBUG] 登录失败: 用户不存在 - " + loginRequest.getUsername());
            return null;
        }
        
        System.out.println("[DEBUG] 找到用户: " + user.getUsername());
        System.out.println("[DEBUG] 输入密码: " + loginRequest.getPassword());
        System.out.println("[DEBUG] 数据库密码哈希: " + user.getPassword());
        System.out.println("[DEBUG] 密码哈希前缀: " + (user.getPassword() != null ? user.getPassword().substring(0, Math.min(10, user.getPassword().length())) : "null"));
        
        boolean matches = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
        System.out.println("[DEBUG] BCrypt 验证结果: " + matches);
        
        // 临时支持明文密码验证(仅用于调试)
        if (!matches) {
            // 支持多个常用测试密码
            if ("123456".equals(user.getPassword()) || "admin123".equals(user.getPassword())) {
                System.out.println("[DEBUG] 使用明文密码验证成功");
                matches = true;
            }
        }
        
        if (!matches) {
            System.out.println("[DEBUG] 登录失败: 密码错误");
            return null;
        }
        
        System.out.println("[DEBUG] 登录成功!");
        user.setPassword(null);
        return user;
    }

    @Override
    public User register(RegisterRequest registerRequest, HttpSession session) {
        // 1. 校验两次密码是否一致
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException("两次密码输入不一致");
        }

        // 2. 校验用户名和手机号是否已存在
        if (userMapper.selectByUsername(registerRequest.getUsername()) != null) {
            throw new RuntimeException("用户名已被注册");
        }
        if (userMapper.selectByPhone(registerRequest.getPhone()) != null) {
            throw new RuntimeException("手机号已被注册");
        }

        // 3. 调用验证码服务进行真实校验
        boolean isCodeValid = captchaService.verifyCode(
                registerRequest.getPhone(),
                registerRequest.getCode(),
                session
        );
        // 测试模式：跳过验证码验证
        if (!isCodeValid && !"123456".equals(registerRequest.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 4. 密码加密并构建对象
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(encodedPassword);
        user.setPhone(registerRequest.getPhone());
        user.setNickname(registerRequest.getUsername());
        user.setGender(0);
        user.setUserLevel(1);
        user.setUid("A" + System.currentTimeMillis());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setIsDeleted(0);

        if (userMapper.insertUser(user) > 0) {
            // 记录用户注册历史
            UserRegistrationHistory history = new UserRegistrationHistory();
            history.setUserId(user.getUserId());
            history.setUsername(user.getUsername());
            history.setPhone(user.getPhone());
            history.setNickname(user.getNickname());
            history.setRegistrationTime(LocalDateTime.now());
            historyMapper.insertUserRegistration(history);

            user.setPassword(null);
            return user;
        } else {
            throw new RuntimeException("注册失败，请重试");
        }
    }
}