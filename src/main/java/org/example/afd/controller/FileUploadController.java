package org.example.afd.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.model.Result;
import org.example.afd.service.UserService;
import org.example.afd.utils.AliyunOSSOperator;
import org.example.afd.utils.LocalFileOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

@Slf4j
@RestController
public class FileUploadController {
    
    @Autowired
    private AliyunOSSOperator aliyunOSSOperator;
    
    @Autowired
    private LocalFileOperator localFileOperator;
    
    @Autowired
    private UserService userService;

    /**
     * 通用文件上传接口
     * @param file 文件
     * @param businessType 业务类型 (AVATAR/POST/PRODUCT/VIDEO)
     * @param request HTTP请求对象
     * @return 文件URL
     */
    @PostMapping("/api/v1/upload")
    public Result uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("businessType") String businessType,
            @RequestParam(value = "userId", required = false) String userIdParam,
            HttpServletRequest request) {
        
        log.info("=== 文件上传请求开始 ===");
        log.info("请求URL: {}", request.getRequestURL());
        log.info("请求方法: {}", request.getMethod());
        log.info("Content-Type: {}", request.getContentType());
        log.info("文件名: {}", file != null ? file.getOriginalFilename() : "null");
        log.info("文件大小: {}", file != null ? file.getSize() : 0);
        log.info("业务类型: {}", businessType);
        log.info("请求参数userId: {}", userIdParam);
        
        // 打印所有请求头
        log.info("=== 请求头信息 ===");
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            log.info("Header: {} = {}", headerName, request.getHeader(headerName));
        });
        
        String userId = userIdParam;
        
        try {
            // 优先从请求头获取用户ID（因为该接口被排除在JWT认证拦截器外）
            String userIdFromHeader = request.getHeader("X-User-ID");
            if (userIdFromHeader != null && !userIdFromHeader.isEmpty()) {
                userId = userIdFromHeader;
                log.info("从X-User-ID请求头获取用户ID: {}", userId);
            }
            
            // 如果请求头没有，再尝试从请求属性中获取（由JwtAuthInterceptor设置）
            if (userId == null || userId.isEmpty()) {
                Integer authUserId = (Integer) request.getAttribute("userId");
                if (authUserId != null) {
                    userId = authUserId.toString();
                    log.info("从请求属性中获取用户ID: {}", userId);
                }
            }
            
            // 如果还没有，使用请求参数中的userId
            if (userId == null || userId.isEmpty()) {
                userId = userIdParam;
                log.info("使用请求参数中的用户ID: {}", userId);
            }
            
            // 确保有userId
            if (userId == null || userId.isEmpty()) {
                log.warn("未提供用户ID，无法继续上传");
                return Result.error("未提供用户ID");
            }

            log.info("文件上传请求: type={}, userId={}, fileName={}, size={}", 
                    businessType, userId, file.getOriginalFilename(), file.getSize());
            
            // 1. 验证用户是否存在
            if (!validateUser(userId)) {
                log.warn("用户不存在或无权限: userId={}", userId);
                return Result.error("用户不存在或无权限");
            }
            
            // 2. 验证文件类型
            if (!validateFileType(file, businessType)) {
                log.warn("不支持的文件类型: type={}, contentType={}", 
                        businessType, file.getContentType());
                return Result.error("上传失败：文件类型不支持");
            }
            
            // 3. 验证文件大小
            long maxSize = getMaxFileSize(businessType);
            if (file.getSize() > maxSize) {
                log.warn("文件大小超过限制: size={}, maxSize={}", 
                        file.getSize(), maxSize);
                return Result.error("文件大小超过限制");
            }
            
            // 4. 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            
            // 5. 构建本地存储路径（与OSS路径结构保持一致）
            String filePath = businessType.toLowerCase() + "/" + userId + "/" + filename;
            
            // 6. 上传到本地存储
            String fileUrl = localFileOperator.upload(
                    file.getBytes(), 
                    filePath,
                    getContentType(file));
            
            log.info("文件上传成功，URL: {}", fileUrl);
            
            // 7. 处理特定业务逻辑
            handleBusinessLogic(businessType, userId, fileUrl);
            
            log.info("=== 文件上传请求成功结束 ===");
            log.info("返回结果: code=200, message=媒体文件上传成功, data={}", fileUrl);
            
            return Result.success("媒体文件上传成功", fileUrl);
        } catch (Exception e) {
            log.error("=== 文件上传请求失败 ===");
            log.error("媒体文件上传失败: type={}, userId={}", businessType, userId, e);
            log.error("返回结果: code=500, message=上传失败：{}", e.getMessage());
            return Result.error("上传失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取内容类型
     */
    private String getContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            // 根据文件扩展名判断内容类型
            String filename = file.getOriginalFilename();
            if (filename != null) {
                if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                    return "image/jpeg";
                } else if (filename.endsWith(".png")) {
                    return "image/png";
                } else if (filename.endsWith(".gif")) {
                    return "image/gif";
                } else if (filename.endsWith(".mp4")) {
                    return "video/mp4";
                }
            }
            return "application/octet-stream";
        }
        return contentType;
    }
    
    /**
     * 获取文件大小限制
     */
    private long getMaxFileSize(String businessType) {
        switch (businessType.toUpperCase()) {
            case "VIDEO":
                return 100 * 1024 * 1024; // 视频最大100MB
            case "AVATAR":
                return 5 * 1024 * 1024; // 头像最大5MB
            default:
                return 10 * 1024 * 1024; // 默认最大10MB
        }
    }
    
    /**
     * 验证用户是否存在
     */
    private boolean validateUser(String userId) {
        try {
            int id = Integer.parseInt(userId);
            return userService.getUserById(id) != null;
        } catch (Exception e) {
            log.warn("用户验证失败: userId={}, error={}", userId, e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证文件类型
     */
    private boolean validateFileType(MultipartFile file, String businessType) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        switch (businessType.toUpperCase()) {
            case "AVATAR":
            case "POST":
            case "PRODUCT":
                return contentType.startsWith("image/");
            case "VIDEO":
                return contentType.startsWith("video/");
            default:
                return contentType.startsWith("image/") || contentType.startsWith("video/");
        }
    }
    
    /**
     * 处理特定业务逻辑
     */
    private void handleBusinessLogic(String businessType, String userId, String fileUrl) {
        try {
            int id = Integer.parseInt(userId);
            
            switch (businessType.toUpperCase()) {
                case "AVATAR":
                    // 更新用户头像
                    userService.updateAvatar(id, fileUrl);
                    log.info("用户头像已更新, userId={}, url={}", userId, fileUrl);
                    break;
                case "POST":
                    // 动态图片不需要立即关联，创建动态时再关联
                    log.info("动态图片已上传, userId={}, url={}", userId, fileUrl);
                    break;
                case "PRODUCT":
                    // 商品图片不需要立即关联，创建商品时再关联
                    log.info("商品图片已上传, userId={}, url={}", userId, fileUrl);
                    break;
                case "VIDEO":
                    // 视频不需要立即关联，创建动态时再关联
                    log.info("视频已上传, userId={}, url={}", userId, fileUrl);
                    break;
                default:
                    log.info("文件已上传，类型={}, userId={}, url={}", 
                            businessType, userId, fileUrl);
                    break;
            }
        } catch (Exception e) {
            log.error("处理业务逻辑失败: type={}, userId={}", businessType, userId, e);
            throw new RuntimeException("处理业务逻辑失败: " + e.getMessage());
        }
    }
} 