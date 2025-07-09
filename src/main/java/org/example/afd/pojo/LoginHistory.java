package org.example.afd.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginHistory {
    private Integer id;
    private Integer userId;
    private LocalDateTime loginTime;
    private String loginIp;
    private String loginDevice;
    private Integer loginStatus;    // 0-失败，1-成功
    private String loginMessage;
} 