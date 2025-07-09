package org.example.afd.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Integer userId;
    private String username;
    private String password;
    private String salt;
    private String email;
    private String phoneNumber;
    private String avatar;
    private String backgroundImage;
    private Integer gender;
    private Date birthday;
    private String signature;
    private String region;
    private String role;
    private LocalDateTime registrationTime;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private Integer status;
    private String memberType;
    private LocalDateTime memberExpiryDate;
    private String memberBenefits;
    private LocalDateTime updateTime;
    private Integer deleted;
    private String introduction;
    
    // 前端展示需要的字段
    private Integer followCount;    // 关注数
    private Integer fansCount;      // 粉丝数
    private Integer planCount;      // 订阅数
    private Boolean isFollowed;     // 是否被当前用户关注
    
    private transient String token;
    private transient String refreshToken;
}