package org.example.afd.utils;

import org.springframework.stereotype.Component;
import java.util.Base64;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 密码工具类
 */
@Component
public class PasswordUtils {

    /**
     * 生成随机盐值
     */
    public String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 使用SHA-256加盐加密密码
     *
     * @param password 原始密码
     * @param salt     盐值
     * @return 加密后的密码
     */
    public String encryptPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] bytes = md.digest(password.getBytes());
            
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("加密密码时出错", e);
        }
    }

    /**
     * 验证密码
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的密码
     * @param salt            盐值
     * @return 密码是否匹配
     */
    public boolean matches(String rawPassword, String encodedPassword, String salt) {
        String encryptedPassword = encryptPassword(rawPassword, salt);
        return encryptedPassword.equals(encodedPassword);
    }
} 