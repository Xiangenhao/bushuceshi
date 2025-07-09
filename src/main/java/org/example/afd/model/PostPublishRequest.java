package org.example.afd.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PostPublishRequest {

    private String title;
    private String description;
    private String videoUrl;
    private String coverUrl;
    private Boolean isFree;
    private BigDecimal price;
    private String zoneName;
    private List<String> tagNames;
    
    // 视频元数据信息
    private Integer videoDuration;    // 视频时长(秒)
    private String videoResolution;   // 视频分辨率
    private Integer videoBitrate;     // 视频比特率(kbps)
    private Long videoFileSize;       // 视频文件大小(byte)
}
