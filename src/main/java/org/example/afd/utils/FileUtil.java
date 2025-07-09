package org.example.afd.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件工具类
 */
@Component
public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    @Value("${file.upload.path:/uploads}")
    private String uploadPath;

    @Value("${file.access.url:http://localhost:8080}")
    private String accessUrl;

    /**
     * 上传文件
     *
     * @param file 上传的文件
     * @param type 文件类型（例如：avatar, product, license等）
     * @return 文件访问URL
     * @throws IOException IO异常
     */
    public String uploadFile(MultipartFile file, String type) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("文件为空");
        }

        // 检查文件类型
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        
        // 检查是否为允许的文件类型
        if (!isAllowedFileType(fileExtension)) {
            throw new IOException("不支持的文件类型: " + fileExtension);
        }

        // 生成保存路径
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String filePath = uploadPath + "/" + type + "/" + datePath;
        
        // 创建目录
        Path directory = Paths.get(filePath);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        // 生成文件名
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        Path targetPath = directory.resolve(uniqueFileName);
        
        // 保存文件
        try {
            Files.write(targetPath, file.getBytes());
            logger.info("文件上传成功: {}", targetPath);
        } catch (IOException e) {
            logger.error("文件上传失败", e);
            throw e;
        }

        // 返回访问URL
        return accessUrl + "/" + type + "/" + datePath + "/" + uniqueFileName;
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDotIndex = filename.lastIndexOf('.');
        return (lastDotIndex == -1) ? "" : filename.substring(lastDotIndex);
    }

    /**
     * 检查是否为允许的文件类型
     *
     * @param fileExtension 文件扩展名
     * @return 是否允许
     */
    private boolean isAllowedFileType(String fileExtension) {
        if (fileExtension == null || fileExtension.isEmpty()) return false;
        
        String lowerExtension = fileExtension.toLowerCase();
        return lowerExtension.equals(".jpg") || 
               lowerExtension.equals(".jpeg") || 
               lowerExtension.equals(".png") || 
               lowerExtension.equals(".gif") || 
               lowerExtension.equals(".pdf") || 
               lowerExtension.equals(".doc") || 
               lowerExtension.equals(".docx");
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     * @return 删除结果
     */
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) return false;
        
        // 从URL获取文件路径
        String filePath = fileUrl.replace(accessUrl, uploadPath);
        File file = new File(filePath);
        
        if (file.exists() && file.isFile()) {
            boolean result = file.delete();
            logger.info("删除文件 {}: {}", filePath, result ? "成功" : "失败");
            return result;
        } else {
            logger.info("文件不存在: {}", filePath);
            return false;
        }
    }
} 