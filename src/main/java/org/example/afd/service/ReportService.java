package org.example.afd.service;

import org.example.afd.entity.Report;
import org.example.afd.mapper.ReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 举报业务逻辑层
 */
@Service
public class ReportService {

    @Autowired
    private ReportMapper reportMapper;

    /**
     * 提交举报
     */
    @Transactional
    public boolean submitReport(Long reporterId, String reportType, Long targetContentId, Long targetUserId, String reason, String reportContent, String contentSnapshot) {
        // 检查是否已经举报过
        int existingCount = reportMapper.checkExistingReport(reporterId, reportType, targetContentId);
        if (existingCount > 0) {
            throw new RuntimeException("您已经举报过该内容，请勿重复举报");
        }

        // 创建举报记录
        Report report = new Report(reporterId, reportType, targetContentId, targetUserId, reason, reportContent, contentSnapshot);
        int result = reportMapper.insertReport(report);
        
        return result > 0;
    }

    /**
     * 查询用户的举报记录
     */
    public List<Report> getUserReports(Long reporterId) {
        return reportMapper.selectReportsByReporter(reporterId);
    }

    /**
     * 查询针对某个对象的举报记录
     */
    public List<Report> getTargetReports(String reportType, Long targetId) {
        return reportMapper.selectReportsByTarget(reportType, targetId);
    }

    /**
     * 分页查询所有举报记录（管理员用）
     */
    public List<Report> getAllReports(int page, int size) {
        int offset = (page - 1) * size;
        return reportMapper.selectAllReports(offset, size);
    }

    /**
     * 根据状态分页查询举报记录
     */
    public List<Report> getReportsByStatus(Integer status, int page, int size) {
        int offset = (page - 1) * size;
        return reportMapper.selectReportsByStatus(status, offset, size);
    }

    /**
     * 统计举报总数
     */
    public int getTotalReportsCount() {
        return reportMapper.countAllReports();
    }

    /**
     * 根据状态统计举报数
     */
    public int getReportsCountByStatus(Integer status) {
        return reportMapper.countReportsByStatus(status);
    }

    /**
     * 处理举报（管理员操作）
     */
    @Transactional
    public boolean handleReport(Long reportId, Integer status, Long adminId, String adminNote) {
        int result = reportMapper.updateReportStatus(reportId, status, adminId, adminNote);
        return result > 0;
    }

    /**
     * 根据ID查询举报详情
     */
    public Report getReportById(Long reportId) {
        return reportMapper.selectReportById(reportId);
    }

    /**
     * 获取举报列表（支持状态和类型过滤）
     */
    public List<Report> getReports(Integer status, String type) {
        return reportMapper.selectReportsWithFilter(status, type);
    }

    /**
     * 删除举报记录
     */
    @Transactional
    public boolean deleteReport(Long reportId) {
        int result = reportMapper.deleteReport(reportId);
        return result > 0;
    }
} 