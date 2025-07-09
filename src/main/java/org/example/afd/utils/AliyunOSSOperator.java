package org.example.afd.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.OSSObjectSummary;
import com.aliyun.oss.model.ObjectListing;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 阿里云OSS操作类
 */
@Slf4j
@Component
@Data
public class AliyunOSSOperator {

    //方式一: 通过@Value注解一个属性一个属性的注入
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    @Value("${aliyun.oss.region}")
    private String region;
    
    // 生成文件URL的前缀
    private String urlPrefix;
    
    /**
     * 初始化完成后记录配置信息
     */
    public void init() {
        // 设置URL前缀
        urlPrefix = "https://" + bucketName + "." + endpoint + "/";
        
        // 不输出日志
    }

    /**
     * 上传文件到OSS
     * 
     * @param fileBytes 文件字节数组
     * @param objectKey OSS对象名称，例如：users/123/avatar.jpg
     * @param contentType 内容类型，例如：image/jpeg
     * @return 文件访问URL
     */
    public String upload(byte[] fileBytes, String objectKey, String contentType) {
        OSS ossClient = null;
        try {
            // 验证配置是否完整
            validateConfiguration();
            
            // 使用环境变量凭证提供者
            EnvironmentVariableCredentialsProvider credentialsProvider = 
                CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
            
            // 创建OSSClient实例
            ClientBuilderConfiguration clientConfig = new ClientBuilderConfiguration();
            clientConfig.setSignatureVersion(SignVersion.V4);
            
            ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientConfig)
                .region(region)
                .build();

            // 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fileBytes.length);
            if (contentType != null && !contentType.isEmpty()) {
                metadata.setContentType(contentType);
            }

            // 上传文件
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, objectKey, 
                    new ByteArrayInputStream(fileBytes), metadata);
            ossClient.putObject(putRequest);

            // 生成文件访问URL
            String fileUrl = urlPrefix + objectKey;
            
            log.info("文件上传成功：objectKey={}, size={}, url={}", objectKey, fileBytes.length, fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("文件上传失败：objectKey={}", objectKey, e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        } finally {
            // 关闭OSSClient
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 验证OSS配置是否完整
     */
    private void validateConfiguration() {
        StringBuilder errors = new StringBuilder();
        
        if (endpoint == null || endpoint.trim().isEmpty() || endpoint.contains("${")) {
            errors.append("OSS endpoint未配置; ");
        }
        
        if (bucketName == null || bucketName.trim().isEmpty() || bucketName.contains("${")) {
            errors.append("OSS bucketName未配置; ");
        }
        
        if (region == null || region.trim().isEmpty() || region.contains("${")) {
            errors.append("OSS region未配置; ");
        }
        
        // 检查环境变量
        String accessKeyId = System.getenv("OSS_ACCESS_KEY_ID");
        String accessKeySecret = System.getenv("OSS_ACCESS_KEY_SECRET");
        
        if (accessKeyId == null || accessKeyId.trim().isEmpty()) {
            errors.append("环境变量OSS_ACCESS_KEY_ID未设置; ");
        }
        
        if (accessKeySecret == null || accessKeySecret.trim().isEmpty()) {
            errors.append("环境变量OSS_ACCESS_KEY_SECRET未设置; ");
        }
        
        if (errors.length() > 0) {
            throw new IllegalStateException("阿里云OSS配置不完整: " + errors.toString());
        }
    }

    /**
     * 上传文件到OSS（支持指定业务类型和业务ID的版本）
     * 
     * @param fileBytes 文件字节数组
     * @param originalFilename 原始文件名
     * @param businessType 业务类型，例如：avatar, post, product等
     * @param businessId 业务ID，例如：用户ID
     * @return 文件访问URL
     */
    public String upload(byte[] fileBytes, String originalFilename, String businessType, String businessId) {
        // 从原始文件名中提取扩展名
        String extension = "";
        int dotPos = originalFilename.lastIndexOf(".");
        if (dotPos > 0) {
            extension = originalFilename.substring(dotPos);
        }

        // 构建OSS对象名称，格式：businessType/businessId/fileName
        String objectKey = businessType.toLowerCase() + "/" + businessId + "/" 
                + System.currentTimeMillis() + extension;

        // 从文件名推断内容类型
        String contentType = getContentTypeByFilename(originalFilename);

        // 上传文件
        return upload(fileBytes, objectKey, contentType);
    }

    /**
     * 从OSS删除文件
     * 
     * @param objectKey OSS对象名称
     */
    public void delete(String objectKey) {
        OSS ossClient = null;
        try {
            // 验证配置是否完整
            validateConfiguration();
            
            // 使用环境变量凭证提供者
            EnvironmentVariableCredentialsProvider credentialsProvider = 
                CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
            
            // 创建OSSClient实例
            ClientBuilderConfiguration clientConfig = new ClientBuilderConfiguration();
            clientConfig.setSignatureVersion(SignVersion.V4);
            
            ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientConfig)
                .region(region)
                .build();

            // 删除文件
            ossClient.deleteObject(bucketName, objectKey);
            
            log.info("文件删除成功：objectKey={}", objectKey);
        } catch (Exception e) {
            log.error("文件删除失败：objectKey={}", objectKey, e);
            throw new RuntimeException("文件删除失败：" + e.getMessage());
        } finally {
            // 关闭OSSClient
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 判断文件是否存在
     * 
     * @param objectKey OSS对象名称
     * @return 是否存在
     */
    public boolean doesObjectExist(String objectKey) {
        OSS ossClient = null;
        try {
            // 验证配置是否完整
            validateConfiguration();
            
            // 使用环境变量凭证提供者
            EnvironmentVariableCredentialsProvider credentialsProvider = 
                CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
            
            // 创建OSSClient实例
            ClientBuilderConfiguration clientConfig = new ClientBuilderConfiguration();
            clientConfig.setSignatureVersion(SignVersion.V4);
            
            ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientConfig)
                .region(region)
                .build();

            // 判断文件是否存在
            return ossClient.doesObjectExist(bucketName, objectKey);
        } catch (Exception e) {
            log.error("检查文件是否存在失败：objectKey={}", objectKey, e);
            return false;
        } finally {
            // 关闭OSSClient
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    /**
     * 根据文件名判断内容类型
     * 
     * @param filename 文件名
     * @return 内容类型
     */
    private String getContentTypeByFilename(String filename) {
        filename = filename.toLowerCase();
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.endsWith(".png")) {
            return "image/png";
        } else if (filename.endsWith(".gif")) {
            return "image/gif";
        } else if (filename.endsWith(".bmp")) {
            return "image/bmp";
        } else if (filename.endsWith(".webp")) {
            return "image/webp";
        } else if (filename.endsWith(".mp4")) {
            return "video/mp4";
        } else if (filename.endsWith(".avi")) {
            return "video/x-msvideo";
        } else if (filename.endsWith(".wmv")) {
            return "video/x-ms-wmv";
        } else if (filename.endsWith(".mov")) {
            return "video/quicktime";
        } else if (filename.endsWith(".flv")) {
            return "video/x-flv";
        } else if (filename.endsWith(".mp3")) {
            return "audio/mpeg";
        } else if (filename.endsWith(".wav")) {
            return "audio/wav";
        } else if (filename.endsWith(".pdf")) {
            return "application/pdf";
        } else if (filename.endsWith(".doc") || filename.endsWith(".docx")) {
            return "application/msword";
        } else if (filename.endsWith(".xls") || filename.endsWith(".xlsx")) {
            return "application/vnd.ms-excel";
        } else if (filename.endsWith(".ppt") || filename.endsWith(".pptx")) {
            return "application/vnd.ms-powerpoint";
        } else if (filename.endsWith(".zip")) {
            return "application/zip";
        } else if (filename.endsWith(".rar")) {
            return "application/x-rar-compressed";
        } else if (filename.endsWith(".7z")) {
            return "application/x-7z-compressed";
        } else {
            return "application/octet-stream";
        }
    }
}
