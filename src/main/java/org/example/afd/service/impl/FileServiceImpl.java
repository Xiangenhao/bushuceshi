package org.example.afd.service.impl;

import org.example.afd.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
 * 文件服务实现类
 * 注意: 推荐使用FileUploadController和LocalFileOperator进行文件上传操作
 */
@Service
public class FileServiceImpl implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    
    @Value("${file.upload.base-path}")
    private String uploadPath;
    
    @Value("${file.upload.base-url}")
    private String accessPath;

    @Override
    public String uploadFile(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            logger.error("上传文件为空");
            throw new RuntimeException("上传文件为空");
        }
        
        try {
            // 获取文件名和后缀
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            
            // 生成新的文件名
            String newFilename = UUID.randomUUID().toString().replace("-", "") + suffix;
            
            // 按日期和目录构建存储路径
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String relativePath = directory + "/" + datePath + "/";
            String absolutePath = uploadPath + "/" + relativePath;
            
            // 创建目录
            File dir = new File(absolutePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            // 保存文件
            Path filePath = Paths.get(absolutePath, newFilename);
            Files.copy(file.getInputStream(), filePath);
            
            // 返回访问URL
            return accessPath + "/" + relativePath + newFilename;
            
        } catch (IOException e) {
            logger.error("上传文件失败: {}", e.getMessage());
            throw new RuntimeException("上传文件失败", e);
        }
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }
        
        try {
            // 从URL中提取文件路径
            String relativePath = fileUrl.replace(accessPath, "");
            String absolutePath = uploadPath + relativePath;
            
            // 删除文件
            File file = new File(absolutePath);
            if (file.exists()) {
                return file.delete();
            }
            return true;
        } catch (Exception e) {
            logger.error("删除文件失败: {}", e.getMessage());
            return false;
        }
    }
} 