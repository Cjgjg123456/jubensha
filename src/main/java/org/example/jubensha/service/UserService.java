package org.example.jubensha.service;

import jakarta.servlet.http.HttpSession;
import org.example.jubensha.dto.LoginRequest;
import org.example.jubensha.dto.RegisterRequest;
import org.example.jubensha.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    User getUserByUsername(String username);
    boolean updateUserProfile(User user);
    boolean updateUserProfile(User user, MultipartFile avatar);

    /**
     * 登录验证
     */
    User login(LoginRequest loginRequest);

    /**
     * 用户注册
     */
    User register(RegisterRequest registerRequest, HttpSession session);
}