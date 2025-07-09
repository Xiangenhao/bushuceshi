package org.example.afd.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户在线状态实体类
 * 对应数据库表：user_online_status
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_online_status")
public class UserOnlineStatus {

    /**
     * 用户ID，主键
     */
    @Id
    @Column(name = "user_id")
    private Integer userId;

    /**
     * 在线状态，0-离线，1-在线，2-隐身，3-忙碌
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 最后活跃时间
     */
    @Column(name = "last_active_time", nullable = false)
    private LocalDateTime lastActiveTime;

    /**
     * 设备信息
     */
    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    /**
     * 客户端类型，1-Android，2-iOS，3-Web，4-PC
     */
    @Column(name = "client_type")
    private Integer clientType;

    /**
     * 会话Token（用于推送等）
     */
    @Column(name = "session_token", length = 500)
    private String sessionToken;

    /**
     * IP地址
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * 位置信息
     */
    @Column(name = "location", length = 100)
    private String location;

    /**
     * 更新时间
     */
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
        if (this.lastActiveTime == null) {
            this.lastActiveTime = LocalDateTime.now();
        }
    }

    /**
     * 检查用户是否在线
     * 在线标准：状态为在线且最后活跃时间在5分钟内
     */
    public boolean isOnline() {
        if (status == null || status == 0) {
            return false;
        }
        
        if (lastActiveTime == null) {
            return false;
        }
        
        // 5分钟内有活跃视为在线
        return lastActiveTime.isAfter(LocalDateTime.now().minusMinutes(5));
    }

    /**
     * 设置为在线状态
     */
    public void setOnline() {
        this.status = 1;
        this.lastActiveTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 设置为离线状态
     */
    public void setOffline() {
        this.status = 0;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 更新活跃时间
     */
    public void updateActiveTime() {
        this.lastActiveTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 获取状态描述
     */
    public String getStatusDescription() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "离线";
            case 1:
                return "在线";
            case 2:
                return "隐身";
            case 3:
                return "忙碌";
            default:
                return "未知";
        }
    }

    /**
     * 获取客户端类型描述
     */
    public String getClientTypeDescription() {
        if (clientType == null) {
            return "未知";
        }
        switch (clientType) {
            case 1:
                return "Android";
            case 2:
                return "iOS";
            case 3:
                return "Web";
            case 4:
                return "PC";
            default:
                return "未知";
        }
    }
} 