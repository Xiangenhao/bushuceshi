package org.example.afd.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 管理员服务接口
 * @author AI Assistant
 * @date 2025-06-02
 */
public interface AdminService {
    
    // ==================== 管理员认证相关 ====================
    
    /**
     * 验证管理员权限
     * @param userId 用户ID
     * @return 是否为管理员
     */
    boolean isAdmin(Integer userId);
    
    /**
     * 获取管理员信息
     * @param adminId 管理员ID
     * @return 管理员信息
     */
    Map<String, Object> getAdminInfo(Integer adminId);
    
    /**
     * 更新管理员信息
     * @param adminId 管理员ID
     * @param username 用户名
     * @param email 邮箱
     * @param phoneNumber 手机号
     * @param avatar 头像
     * @return 是否成功
     */
    boolean updateAdminInfo(Integer adminId, String username, String email, String phoneNumber, String avatar);
    
    // ==================== Banner管理 ====================
    
    /**
     * 获取所有Banner列表
     * @param position 位置筛选（可选）
     * @return Banner列表
     */
    List<Map<String, Object>> getAllBanners(String position);
    
    /**
     * 根据ID获取Banner详情
     * @param bannerId Banner ID
     * @return Banner详情
     */
    Map<String, Object> getBannerById(Long bannerId);
    
    /**
     * 创建新Banner
     * @param title 标题
     * @param imageUrl 图片URL
     * @param linkType 链接类型
     * @param targetId 目标ID
     * @param linkUrl 链接URL
     * @param position 位置
     * @param sortOrder 排序
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param status 状态
     * @param remark 备注
     * @return 是否成功
     */
    boolean createBanner(String title, String imageUrl, Integer linkType, Long targetId, String linkUrl, 
                        String position, Integer sortOrder, Long startTime, Long endTime, Integer status, String remark);
    
    /**
     * 更新Banner
     * @param bannerId Banner ID
     * @param title 标题
     * @param imageUrl 图片URL
     * @param linkType 链接类型
     * @param targetId 目标ID
     * @param linkUrl 链接URL
     * @param position 位置
     * @param sortOrder 排序
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param status 状态
     * @param remark 备注
     * @return 是否成功
     */
    boolean updateBanner(Long bannerId, String title, String imageUrl, Integer linkType, Long targetId, String linkUrl, 
                        String position, Integer sortOrder, Long startTime, Long endTime, Integer status, String remark);
    
    /**
     * 删除Banner
     * @param bannerId Banner ID
     * @return 是否成功
     */
    boolean deleteBanner(Long bannerId);
    
    /**
     * 更新Banner状态
     * @param bannerId Banner ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateBannerStatus(Long bannerId, Integer status);
    
    // ==================== 分类管理 ====================
    
    /**
     * 获取所有分类列表
     * @param parentId 父分类ID（可选）
     * @return 分类列表
     */
    List<Map<String, Object>> getAllCategories(Long parentId);
    
    /**
     * 根据ID获取分类详情
     * @param categoryId 分类ID
     * @return 分类详情
     */
    Map<String, Object> getCategoryById(Long categoryId);
    
    /**
     * 创建新分类
     * @param parentId 父分类ID
     * @param categoryName 分类名称
     * @param icon 图标
     * @param sortOrder 排序
     * @param status 状态
     * @return 是否成功
     */
    boolean createCategory(Long parentId, String categoryName, String icon, Integer sortOrder, Integer status);
    
    /**
     * 更新分类
     * @param categoryId 分类ID
     * @param parentId 父分类ID
     * @param categoryName 分类名称
     * @param icon 图标
     * @param sortOrder 排序
     * @param status 状态
     * @return 是否成功
     */
    boolean updateCategory(Long categoryId, Long parentId, String categoryName, String icon, Integer sortOrder, Integer status);
    
    /**
     * 删除分类
     * @param categoryId 分类ID
     * @return 是否成功
     */
    boolean deleteCategory(Long categoryId);
    
    /**
     * 更新分类状态
     * @param categoryId 分类ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateCategoryStatus(Long categoryId, Integer status);
    
    // ==================== 用户管理 ====================
    
    /**
     * 获取用户列表（分页）
     * @param status 状态筛选（可选）
     * @param page 页码
     * @param size 每页数量
     * @return 用户列表和分页信息
     */
    Map<String, Object> getAllUsers(Integer status, int page, int size);
    
    /**
     * 根据用户名搜索用户（分页）
     * @param username 用户名
     * @param page 页码
     * @param size 每页数量
     * @return 用户列表和分页信息
     */
    Map<String, Object> searchUsersByUsername(String username, int page, int size);
    
    /**
     * 根据用户ID获取用户详细信息
     * @param userId 用户ID
     * @return 用户详细信息
     */
    Map<String, Object> getUserDetailById(Long userId);
    
    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateUserStatus(Long userId, Integer status);
    
    /**
     * 重置用户密码
     * @param userId 用户ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetUserPassword(Long userId, String newPassword);
    
    // ==================== 系统统计 ====================
    
    /**
     * 获取系统统计数据
     * @return 统计数据
     */
    Map<String, Object> getSystemStatistics();
    
    // ==================== 支付渠道管理 ====================
    
    /**
     * 获取所有支付渠道列表
     * @return 支付渠道列表
     */
    List<Map<String, Object>> getAllPaymentChannels();
    
    /**
     * 根据ID获取支付渠道详情
     * @param channelId 支付渠道ID
     * @return 支付渠道详情
     */
    Map<String, Object> getPaymentChannelById(Integer channelId);
    
    /**
     * 创建新支付渠道
     * @param channelCode 渠道代码
     * @param channelName 渠道名称
     * @param iconUrl 图标URL
     * @param description 描述
     * @param isEnabled 是否启用
     * @param sortOrder 排序
     * @param config 配置
     * @return 是否成功
     */
    boolean createPaymentChannel(String channelCode, String channelName, String iconUrl, String description, 
                               Integer isEnabled, Integer sortOrder, String config);
    
    /**
     * 更新支付渠道
     * @param channelId 渠道ID
     * @param channelCode 渠道代码
     * @param channelName 渠道名称
     * @param iconUrl 图标URL
     * @param description 描述
     * @param isEnabled 是否启用
     * @param sortOrder 排序
     * @param config 配置
     * @return 是否成功
     */
    boolean updatePaymentChannel(Integer channelId, String channelCode, String channelName, String iconUrl, String description, 
                               Integer isEnabled, Integer sortOrder, String config);
    
    /**
     * 删除支付渠道
     * @param channelId 渠道ID
     * @return 是否成功
     */
    boolean deletePaymentChannel(Integer channelId);
    
    /**
     * 更新支付渠道状态
     * @param channelId 渠道ID
     * @param isEnabled 是否启用
     * @return 是否成功
     */
    boolean updatePaymentChannelStatus(Integer channelId, Integer isEnabled);
    
    // ==================== 轮播图管理 ====================
    
    /**
     * 获取轮播图列表
     * @param position 位置筛选（可选）
     * @param status 状态筛选（可选）
     * @return 轮播图列表
     */
    List<Map<String, Object>> getCarouselBanners(String position, Integer status);
    
    /**
     * 根据ID获取轮播图详情
     * @param bannerId 轮播图ID
     * @return 轮播图详情
     */
    Map<String, Object> getCarouselBannerById(Long bannerId);
    
    /**
     * 创建新轮播图
     * @param title 标题
     * @param imageUrl 图片URL
     * @param linkUrl 链接URL
     * @param position 位置
     * @param status 状态
     * @param sortOrder 排序
     * @param description 描述
     * @return 是否成功
     */
    boolean createCarouselBanner(String title, String imageUrl, String linkUrl, String position, 
                               Integer status, Integer sortOrder, String description);
    
    /**
     * 更新轮播图
     * @param bannerId 轮播图ID
     * @param title 标题
     * @param imageUrl 图片URL
     * @param linkUrl 链接URL
     * @param position 位置
     * @param status 状态
     * @param sortOrder 排序
     * @param description 描述
     * @return 是否成功
     */
    boolean updateCarouselBanner(Long bannerId, String title, String imageUrl, String linkUrl, String position, 
                               Integer status, Integer sortOrder, String description);
    
    /**
     * 删除轮播图
     * @param bannerId 轮播图ID
     * @return 是否成功
     */
    boolean deleteCarouselBanner(Long bannerId);
    
    // ==================== 登录记录查询 ====================
    
    /**
     * 获取登录记录列表（分页）
     * @param page 页码
     * @param size 每页数量
     * @param userId 用户ID筛选（可选）
     * @param startDate 开始时间筛选（可选）
     * @param endDate 结束时间筛选（可选）
     * @param loginType 登录类型筛选（可选）
     * @return 登录记录列表和分页信息
     */
    Map<String, Object> getLoginRecords(int page, int size, Long userId, Date startDate, Date endDate, Integer loginType);
    
    /**
     * 根据ID获取登录记录详情
     * @param recordId 记录ID
     * @return 登录记录详情
     */
    Map<String, Object> getLoginRecordById(Long recordId);
    
    /**
     * 获取登录统计信息
     * @param startDate 开始时间（可选）
     * @param endDate 结束时间（可选）
     * @param groupBy 分组方式（可选）
     * @return 统计信息
     */
    List<Map<String, Object>> getLoginStatistics(Date startDate, Date endDate, String groupBy);
    
    /**
     * 获取登录汇总统计
     * @param startDate 开始时间（可选）
     * @param endDate 结束时间（可选）
     * @return 汇总统计
     */
    Map<String, Object> getLoginSummary(Date startDate, Date endDate);
} 