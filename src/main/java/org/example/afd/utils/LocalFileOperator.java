package org.example.afd.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地文件存储操作工具类
 */
@Slf4j
@Component
public class LocalFileOperator {

    @Value("${file.upload.base-path:/upload/files}")
    private String basePath;

    @Value("${file.upload.base-url:http://192.168.50.239:8080/files}")
    private String baseUrl;

    /**
     * 初始化存储目录
     */
    @PostConstruct
    public void init() {
        try {
            File directory = new File(basePath);
            if (!directory.exists()) {
                if (directory.mkdirs()) {
                    log.info("本地文件存储目录创建成功: {}", basePath);
                } else {
                    log.error("无法创建本地文件存储目录: {}", basePath);
                }
            }
            validateConfiguration();
        } catch (Exception e) {
            log.error("初始化本地文件存储失败", e);
        }
    }

    /**
     * 上传文件到本地存储
     *
     * @param fileBytes   文件字节数组
     * @param relativePath 相对路径 (例如: avatar/123/file.jpg)
     * @param contentType 内容类型
     * @return 文件访问URL
     * @throws IOException 如果文件操作失败
     */
    public String upload(byte[] fileBytes, String relativePath, String contentType) throws IOException {
        validateConfiguration();

        // 创建完整的目录路径
        String fullPath = basePath + File.separator + relativePath;
        File file = new File(fullPath);
        
        // 确保目录存在
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("无法创建目录: " + parentDir.getAbsolutePath());
            }
        }

        // 写入文件
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(fileBytes);
        }

        log.info("文件已成功上传到本地存储: {}", fullPath);

        // 返回可访问的URL - 修复URL拼接
        String normalizedBaseUrl = baseUrl;
        if (normalizedBaseUrl.endsWith("/")) {
            normalizedBaseUrl = normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1);
        }
        String normalizedPath = relativePath.replace(File.separator, "/");
        if (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }
        
        String fileUrl = normalizedBaseUrl + "/" + normalizedPath;
        log.info("生成文件访问URL: {}", fileUrl);
        return fileUrl;
    }

    /**
     * 验证配置是否有效
     */
    private void validateConfiguration() {
        if (basePath == null || basePath.trim().isEmpty()) {
            throw new IllegalStateException("本地文件存储基础路径未配置");
        }
        
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            throw new IllegalStateException("本地文件访问基础URL未配置");
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param relativePath 相对路径
     * @return 文件是否存在
     */
    public boolean doesFileExist(String relativePath) {
        String fullPath = basePath + File.separator + relativePath;
        File file = new File(fullPath);
        return file.exists() && file.isFile();
    }

    /**
     * 删除文件
     *
     * @param relativePath 相对路径
     * @return 是否删除成功
     */
    public boolean delete(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return false;
        }

        try {
            String fullPath = basePath + File.separator + relativePath;
            Path path = Paths.get(fullPath);
            
            if (Files.exists(path) && Files.isRegularFile(path)) {
                Files.delete(path);
                log.info("文件已成功删除: {}", fullPath);
                return true;
            } else {
                log.warn("文件不存在，无法删除: {}", fullPath);
                return false;
            }
        } catch (Exception e) {
            log.error("删除文件失败: {}", relativePath, e);
            return false;
        }
    }
} 