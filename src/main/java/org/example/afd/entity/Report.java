package org.example.afd.entity;

import java.time.LocalDateTime;

/**
 * 举报实体类
 */
public class Report {
    private Long reportId;
    private Long reporterId;
    private String reportType; // post, comment, reply, user
    private Long targetContentId; // 被举报内容ID（动态ID、评论ID、回复ID等）
    private Long targetUserId; // 被举报用户ID
    private String reason; // 举报原因（从预设选项中选择）
    private String reportContent; // 用户填写的详细举报内容
    private String contentSnapshot; // 被举报内容快照
    private Integer status; // 0-待处理，1-已处理，2-已驳回
    private Long adminId;
    private String adminNote;
    private LocalDateTime createTime;
    private LocalDateTime handleTime;

    // 构造函数
    public Report() {}

    public Report(Long reporterId, String reportType, Long targetContentId, Long targetUserId, String reason, String reportContent, String contentSnapshot) {
        this.reporterId = reporterId;
        this.reportType = reportType;
        this.targetContentId = targetContentId;
        this.targetUserId = targetUserId;
        this.reason = reason;
        this.reportContent = reportContent;
        this.contentSnapshot = contentSnapshot;
        this.status = 0; // 默认待处理
        this.createTime = LocalDateTime.now();
    }

    // Getter和Setter方法
    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public void setReporterId(Long reporterId) {
        this.reporterId = reporterId;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public Long getTargetContentId() {
        return targetContentId;
    }

    public void setTargetContentId(Long targetContentId) {
        this.targetContentId = targetContentId;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReportContent() {
        return reportContent;
    }

    public void setReportContent(String reportContent) {
        this.reportContent = reportContent;
    }

    public String getContentSnapshot() {
        return contentSnapshot;
    }

    public void setContentSnapshot(String contentSnapshot) {
        this.contentSnapshot = contentSnapshot;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(LocalDateTime handleTime) {
        this.handleTime = handleTime;
    }

    @Override
    public String toString() {
        return "Report{" +
                "reportId=" + reportId +
                ", reporterId=" + reporterId +
                ", reportType='" + reportType + '\'' +
                ", targetContentId=" + targetContentId +
                ", targetUserId=" + targetUserId +
                ", reason='" + reason + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }
} 