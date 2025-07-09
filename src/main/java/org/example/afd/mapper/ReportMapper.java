package org.example.afd.mapper;

import org.apache.ibatis.annotations.*;
import org.example.afd.entity.Report;

import java.util.List;

/**
 * 举报数据访问层
 */
@Mapper
public interface ReportMapper {

    /**
     * 提交举报
     */
    @Insert("INSERT INTO afd.reports (reporter_id, report_type, target_content_id, target_user_id, reason, report_content, content_snapshot, status, create_time) " +
            "VALUES (#{reporterId}, #{reportType}, #{targetContentId}, #{targetUserId}, #{reason}, #{reportContent}, #{contentSnapshot}, #{status}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "reportId")
    int insertReport(Report report);

    /**
     * 根据举报ID查询举报
     */
    @Select("SELECT * FROM afd.reports WHERE report_id = #{reportId}")
    Report selectReportById(Long reportId);

    /**
     * 查询用户的举报记录
     */
    @Select("SELECT * FROM afd.reports WHERE reporter_id = #{reporterId} ORDER BY create_time DESC")
    List<Report> selectReportsByReporter(Long reporterId);

    /**
     * 查询针对某个对象的举报记录
     */
    @Select("SELECT * FROM afd.reports WHERE report_type = #{reportType} AND target_content_id = #{targetContentId} ORDER BY create_time DESC")
    List<Report> selectReportsByTarget(String reportType, Long targetContentId);

    /**
     * 查询所有举报记录（管理员用）
     */
    @Select("SELECT * FROM afd.reports ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<Report> selectAllReports(int offset, int limit);

    /**
     * 根据状态查询举报记录
     */
    @Select("SELECT * FROM afd.reports WHERE status = #{status} ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<Report> selectReportsByStatus(Integer status, int offset, int limit);

    /**
     * 统计举报总数
     */
    @Select("SELECT COUNT(*) FROM afd.reports")
    int countAllReports();

    /**
     * 根据状态统计举报数
     */
    @Select("SELECT COUNT(*) FROM afd.reports WHERE status = #{status}")
    int countReportsByStatus(Integer status);

    /**
     * 处理举报
     */
    @Update("UPDATE afd.reports SET status = #{status}, admin_id = #{adminId}, admin_note = #{adminNote}, handle_time = NOW() " +
            "WHERE report_id = #{reportId}")
    int updateReportStatus(Long reportId, Integer status, Long adminId, String adminNote);

    /**
     * 检查是否已经举报过
     */
    @Select("SELECT COUNT(*) FROM afd.reports WHERE reporter_id = #{reporterId} AND report_type = #{reportType} AND target_content_id = #{targetContentId}")
    int checkExistingReport(Long reporterId, String reportType, Long targetContentId);

    /**
     * 删除举报记录
     */
    @Delete("DELETE FROM afd.reports WHERE report_id = #{reportId}")
    int deleteReport(Long reportId);

    /**
     * 根据状态和类型过滤查询举报记录
     */
    @Select("<script>" +
            "SELECT * FROM afd.reports " +
            "WHERE 1=1 " +
            "<if test='status != null'> AND status = #{status} </if>" +
            "<if test='type != null and type != \"\"'> AND report_type = #{type} </if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<Report> selectReportsWithFilter(@Param("status") Integer status, @Param("type") String type);
} 