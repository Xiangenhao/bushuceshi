package org.example.afd.dto;

/**
 * 用户注册请求DTO
 */
public class UserRegisterRequest {
    
    private String registerType; // phone 或 email
    private String account; // 手机号或邮箱
    private String captcha; // 图像验证码
    private String captchaId; // 验证码ID
    private String verificationCode; // 短信/邮件验证码
    private String password; // 密码
    private String username; // 用户名
    
    // Getters and Setters
    public String getRegisterType() {
        return registerType;
    }
    
    public void setRegisterType(String registerType) {
        this.registerType = registerType;
    }
    
    public String getAccount() {
        return account;
    }
    
    public void setAccount(String account) {
        this.account = account;
    }
    
    public String getCaptcha() {
        return captcha;
    }
    
    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }
    
    public String getCaptchaId() {
        return captchaId;
    }
    
    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }
    
    public String getVerificationCode() {
        return verificationCode;
    }
    
    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
} 