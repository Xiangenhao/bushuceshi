package org.example.afd.dto;

import lombok.Data;

/**
 * 简化的帖子信息，用于列表展示
 */
@Data
public class PostMini {
    private Integer postId;
    private String title;
    private String coverUrl;
    private String username;
    private Integer viewCount;
    private Integer duration;
} 