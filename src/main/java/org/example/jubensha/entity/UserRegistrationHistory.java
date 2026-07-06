package org.example.jubensha.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserRegistrationHistory {
    private Integer id;
    private Long userId;
    private String username;
    private String phone;
    private String nickname;
    private LocalDateTime registrationTime;
    private String ipAddress;
    private String deviceInfo;
    private String status;
}
