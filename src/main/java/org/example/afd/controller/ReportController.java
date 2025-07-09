package org.example.afd.controller;

import org.example.afd.entity.Report;
import org.example.afd.model.Result;
import org.example.afd.service.ReportService;
import org.example.afd.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 举报控制器
 */
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 提交举报
     */
    @PostMapping
    public Result<Boolean> submitReport(@RequestBody Map<String, Object> reportData, HttpServletRequest request) {
        try {
            // 从请求头获取用户ID
            Integer userIdInt = jwtUtil.getUserIdFromRequest(request);
            if (userIdInt == null) {
                return Result.error("用户未登录");
            }
            Long reporterId = userIdInt.longValue();

            // 获取举报数据
            String reportType = (String) reportData.get("reportType");
            Long targetContentId = Long.valueOf(reportData.get("targetContentId").toString());
            Long targetUserId = Long.valueOf(reportData.get("targetUserId").toString());
            String reason = (String) reportData.get("reason");
            String reportContent = (String) reportData.get("reportContent");
            String contentSnapshot = (String) reportData.get("contentSnapshot");

            // 验证必填字段
            if (reportType == null || targetContentId == null || targetUserId == null || reason == null || reportContent == null) {
                return Result.error("举报信息不完整");
            }

            if (reportContent.trim().length() < 10) {
                return Result.error("举报内容至少需要10个字符");
            }

            // 验证举报类型
            if (!isValidReportType(reportType)) {
                return Result.error("无效的举报类型");
            }

            // 提交举报
            boolean success = reportService.submitReport(reporterId, reportType, targetContentId, targetUserId, reason, reportContent, contentSnapshot);
            
            if (success) {
                return Result.success("举报提交成功，我们会尽快处理", true);
            } else {
                return Result.error("举报提交失败");
            }

        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("举报提交失败：" + e.getMessage());
        }
    }

    /**
     * 验证举报类型是否有效
     */
    private boolean isValidReportType(String reportType) {
        return "post".equals(reportType) || 
               "comment".equals(reportType) || 
               "reply".equals(reportType) || 
               "user".equals(reportType) ||
               "subscription_plan".equals(reportType);
    }

    // ==================== 管理员接口 ====================

    /**
     * 获取举报列表（管理员）
     */
    @GetMapping("/admin")
    public Result<List<Report>> getReports(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String type,
            HttpServletRequest request) {
        try {
            // 验证管理员权限
            if (!isAdmin(request)) {
                return Result.error("权限不足");
            }

            List<Report> reports = reportService.getReports(status, type);
            return Result.success(reports);

        } catch (Exception e) {
            return Result.error("获取举报列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取举报详情（管理员）
     */
    @GetMapping("/admin/{reportId}")
    public Result<Report> getReportDetail(@PathVariable Long reportId, HttpServletRequest request) {
        try {
            // 验证管理员权限
            if (!isAdmin(request)) {
                return Result.error("权限不足");
            }

            Report report = reportService.getReportById(reportId);
            if (report == null) {
                return Result.error("举报不存在");
            }

            return Result.success(report);

        } catch (Exception e) {
            return Result.error("获取举报详情失败：" + e.getMessage());
        }
    }

    /**
     * 处理举报（管理员）
     */
    @PostMapping("/admin/{reportId}/handle")
    public Result<Boolean> handleReport(
            @PathVariable Long reportId,
            @RequestBody Map<String, Object> handleData,
            HttpServletRequest request) {
        try {
            // 验证管理员权限
            if (!isAdmin(request)) {
                return Result.error("权限不足");
            }

            // 获取管理员ID
            Integer adminIdInt = jwtUtil.getUserIdFromRequest(request);
            if (adminIdInt == null) {
                return Result.error("管理员未登录");
            }
            Long adminId = adminIdInt.longValue();

            // 获取处理数据
            Integer status = (Integer) handleData.get("status");
            String adminNote = (String) handleData.get("adminNote");

            // 验证参数
            if (status == null || (status != 1 && status != 2)) {
                return Result.error("无效的处理状态");
            }

            if (adminNote == null || adminNote.trim().isEmpty()) {
                return Result.error("处理备注不能为空");
            }

            // 处理举报
            boolean success = reportService.handleReport(reportId, status, adminId, adminNote);
            
            if (success) {
                return Result.success("举报处理成功", true);
            } else {
                return Result.error("举报处理失败");
            }

        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("举报处理失败：" + e.getMessage());
        }
    }

    /**
     * 验证是否为管理员
     */
    private boolean isAdmin(HttpServletRequest request) {
        try {
            Integer userId = jwtUtil.getUserIdFromRequest(request);
            if (userId == null) {
                return false;
            }
            
            // 临时设置用户ID 7为管理员用于测试
            if (userId == 7) {
                return true;
            }
            
            // 这里可以添加更复杂的管理员验证逻辑
            // 目前简单判断用户ID为1的为管理员
            return userId == 1;
            
        } catch (Exception e) {
            return false;
        }
    }
}