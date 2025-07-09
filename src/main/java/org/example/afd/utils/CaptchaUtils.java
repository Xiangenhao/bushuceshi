package org.example.afd.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Random;
import javax.imageio.ImageIO;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 图像验证码工具类
 * 用于生成和验证图像验证码
 */
@Slf4j
@Component
public class CaptchaUtils {
    
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    
    private final Random random = new Random();
    
    // 存储验证码的Map，实际项目中应该使用Redis等缓存
    private final Map<String, String> captchaStore = new HashMap<>();
    
    /**
     * 生成验证码图片
     * @return 包含验证码ID和Base64图片的Map
     */
    public Map<String, String> generateCaptcha() {
        try {
            // 生成验证码字符串
            String code = generateRandomCode();
            
            // 创建图片
            BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            
            // 设置抗锯齿
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 设置背景色
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, WIDTH, HEIGHT);
            
            // 绘制干扰线
            drawInterferenceLines(graphics);
            
            // 绘制验证码字符
            drawCode(graphics, code);
            
            // 绘制干扰点
            drawInterferencePoints(graphics);
            
            graphics.dispose();
            
            // 转换为Base64
            String base64Image = imageToBase64(image);
            
            // 生成验证码ID
            String captchaId = generateCaptchaId();
            
            // 存储验证码（5分钟过期）
            captchaStore.put(captchaId, code.toUpperCase());
            
            // 设置过期时间（简单实现，实际应该用Redis TTL）
            scheduleExpiry(captchaId);
            
            Map<String, String> result = new HashMap<>();
            result.put("captchaId", captchaId);
            result.put("imageBase64", base64Image);
            
            log.info("生成验证码成功: captchaId={}, code={}", captchaId, code);
            return result;
            
        } catch (Exception e) {
            log.error("生成验证码失败", e);
            throw new RuntimeException("验证码生成失败");
        }
    }
    
    /**
     * 验证验证码
     * @param captchaId 验证码ID
     * @param inputCode 用户输入的验证码
     * @return 是否验证成功
     */
    public boolean verifyCaptcha(String captchaId, String inputCode) {
        try {
            if (captchaId == null || inputCode == null) {
                return false;
            }
            
            String storedCode = captchaStore.get(captchaId);
            if (storedCode == null) {
                log.warn("验证码已过期或不存在: captchaId={}", captchaId);
                return false;
            }
            
            // 验证后删除验证码（一次性使用）
            captchaStore.remove(captchaId);
            
            boolean isValid = storedCode.equalsIgnoreCase(inputCode.trim());
            log.info("验证码校验: captchaId={}, inputCode={}, valid={}", captchaId, inputCode, isValid);
            
            return isValid;
            
        } catch (Exception e) {
            log.error("验证码校验失败: captchaId=" + captchaId, e);
            return false;
        }
    }
    
    /**
     * 生成随机验证码字符串
     */
    private String generateRandomCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }
    
    /**
     * 绘制验证码字符
     */
    private void drawCode(Graphics2D graphics, String code) {
        Font[] fonts = {
            new Font("Arial", Font.BOLD, 25),
            new Font("Times New Roman", Font.BOLD, 24),
            new Font("Courier New", Font.BOLD, 26)
        };
        
        Color[] colors = {
            new Color(50, 50, 50),
            new Color(70, 70, 70),
            new Color(90, 90, 90),
            new Color(110, 110, 110)
        };
        
        int x = 15;
        for (int i = 0; i < code.length(); i++) {
            // 随机字体和颜色
            graphics.setFont(fonts[random.nextInt(fonts.length)]);
            graphics.setColor(colors[random.nextInt(colors.length)]);
            
            // 随机旋转角度
            double angle = (random.nextDouble() - 0.5) * 0.5;
            graphics.rotate(angle);
            
            // 绘制字符
            int y = 25 + random.nextInt(5);
            graphics.drawString(String.valueOf(code.charAt(i)), x, y);
            
            // 恢复旋转
            graphics.rotate(-angle);
            
            x += 25;
        }
    }
    
    /**
     * 绘制干扰线
     */
    private void drawInterferenceLines(Graphics2D graphics) {
        graphics.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < 3; i++) {
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            
            graphics.setColor(new Color(180 + random.nextInt(50), 180 + random.nextInt(50), 180 + random.nextInt(50)));
            graphics.drawLine(x1, y1, x2, y2);
        }
    }
    
    /**
     * 绘制干扰点
     */
    private void drawInterferencePoints(Graphics2D graphics) {
        for (int i = 0; i < 30; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            graphics.setColor(new Color(200 + random.nextInt(50), 200 + random.nextInt(50), 200 + random.nextInt(50)));
            graphics.fillOval(x, y, 2, 2);
        }
    }
    
    /**
     * 将图片转换为Base64
     */
    private String imageToBase64(BufferedImage image) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
    }
    
    /**
     * 生成验证码ID
     */
    private String generateCaptchaId() {
        return "captcha_" + System.currentTimeMillis() + "_" + random.nextInt(10000);
    }
    
    /**
     * 设置验证码过期时间（简单实现）
     */
    private void scheduleExpiry(String captchaId) {
        // 5分钟后删除验证码
        new Thread(() -> {
            try {
                Thread.sleep(5 * 60 * 1000); // 5分钟
                captchaStore.remove(captchaId);
                log.debug("验证码已过期: captchaId={}", captchaId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
} 