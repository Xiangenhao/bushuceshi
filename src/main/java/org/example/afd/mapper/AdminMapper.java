package org.example.afd.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

/**
 * 管理员相关数据访问层
 * @author AI Assistant
 * @date 2025-06-02
 */
@Mapper
public interface AdminMapper {
    
    // ==================== 管理员认证相关 ====================
    
    /**
     * 根据用户ID检查是否为管理员
     * @param userId 用户ID
     * @return 用户角色
     */
    @Select("SELECT role FROM users WHERE user_id = #{userId} AND deleted = 0")
    String getUserRole(Integer userId);
    
    /**
     * 获取管理员信息
     * @param adminId 管理员ID
     * @return 管理员信息
     */
    @Select("SELECT user_id, username, email, phone_number, avatar, role, registration_time, " +
            "last_login_time, last_login_ip, status FROM users WHERE user_id = #{adminId} AND role = 'ADMIN' AND deleted = 0")
    Map<String, Object> getAdminInfo(Integer adminId);
    
    /**
     * 更新管理员信息
     * @param adminId 管理员ID
     * @param username 用户名
     * @param email 邮箱
     * @param phoneNumber 手机号
     * @param avatar 头像
     * @return 更新条数
     */
    @Update("UPDATE users SET username = #{username}, email = #{email}, phone_number = #{phoneNumber}, " +
            "avatar = #{avatar}, update_time = NOW() WHERE user_id = #{adminId} AND role = 'ADMIN'")
    int updateAdminInfo(@Param("adminId") Integer adminId, @Param("username") String username, 
                       @Param("email") String email, @Param("phoneNumber") String phoneNumber, 
                       @Param("avatar") String avatar);
    
    /**
     * 根据账号获取管理员信息（支持用户名、邮箱、手机号登录）
     * @param account 账号（用户名、邮箱或手机号）
     * @return 管理员信息
     */
    @Select("SELECT user_id, username, password, salt, email, phone_number, avatar, role, " +
            "registration_time, last_login_time, last_login_ip, status " +
            "FROM users WHERE (username = #{account} OR email = #{account} OR phone_number = #{account}) " +
            "AND role = 'ADMIN' AND deleted = 0")
    Map<String, Object> getAdminByAccount(@Param("account") String account);
    
    // ==================== Banner管理 ====================
    
    /**
     * 获取所有Banner列表
     * @return Banner列表
     */
    @Select("SELECT banner_id, title, image_url, link_type, target_id, link_url, position, " +
            "sort_order, start_time, end_time, status, remark, create_time, update_time " +
            "FROM t_banner ORDER BY sort_order ASC, create_time DESC")
    List<Map<String, Object>> getAllBanners();
    
    /**
     * 根据位置获取Banner列表
     * @param position 位置
     * @return Banner列表
     */
    @Select("SELECT banner_id, title, image_url, link_type, target_id, link_url, position, " +
            "sort_order, start_time, end_time, status, remark, create_time, update_time " +
            "FROM t_banner WHERE position = #{position} ORDER BY sort_order ASC, create_time DESC")
    List<Map<String, Object>> getBannersByPosition(@Param("position") String position);
    
    /**
     * 根据ID获取Banner详情
     * @param bannerId Banner ID
     * @return Banner详情
     */
    @Select("SELECT banner_id, title, image_url, link_type, target_id, link_url, position, " +
            "sort_order, start_time, end_time, status, remark, create_time, update_time " +
            "FROM t_banner WHERE banner_id = #{bannerId}")
    Map<String, Object> getBannerById(Long bannerId);
    
    /**
     * 创建新Banner
     */
    @Insert("INSERT INTO t_banner (title, image_url, link_type, target_id, link_url, position, " +
            "sort_order, start_time, end_time, status, remark, create_time, update_time) " +
            "VALUES (#{title}, #{imageUrl}, #{linkType}, #{targetId}, #{linkUrl}, #{position}, " +
            "#{sortOrder}, #{startTime}, #{endTime}, #{status}, #{remark}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "bannerId")
    int createBanner(Map<String, Object> banner);
    
    /**
     * 更新Banner
     */
    @Update("UPDATE t_banner SET title = #{title}, image_url = #{imageUrl}, " +
            "link_type = #{linkType}, target_id = #{targetId}, link_url = #{linkUrl}, " +
            "position = #{position}, sort_order = #{sortOrder}, start_time = #{startTime}, " +
            "end_time = #{endTime}, status = #{status}, remark = #{remark}, update_time = NOW() " +
            "WHERE banner_id = #{bannerId}")
    int updateBanner(Map<String, Object> banner);
    
    /**
     * 删除Banner
     * @param bannerId Banner ID
     * @return 删除条数
     */
    @Delete("DELETE FROM t_banner WHERE banner_id = #{bannerId}")
    int deleteBanner(Long bannerId);
    
    /**
     * 更新Banner状态
     * @param bannerId Banner ID
     * @param status 状态
     * @return 更新条数
     */
    @Update("UPDATE t_banner SET status = #{status}, update_time = NOW() WHERE banner_id = #{bannerId}")
    int updateBannerStatus(@Param("bannerId") Long bannerId, @Param("status") Integer status);
    
    // ==================== 分类管理 ====================
    
    /**
     * 获取所有分类列表
     * @return 分类列表
     */
    @Select("SELECT category_id, parent_id, category_name, icon, sort_order, status, create_time, update_time " +
            "FROM shop_category ORDER BY sort_order ASC, create_time DESC")
    List<Map<String, Object>> getAllCategories();
    
    /**
     * 根据父分类ID获取子分类
     * @param parentId 父分类ID
     * @return 分类列表
     */
    @Select("SELECT category_id, parent_id, category_name, icon, sort_order, status, create_time, update_time " +
            "FROM shop_category WHERE parent_id = #{parentId} ORDER BY sort_order ASC, create_time DESC")
    List<Map<String, Object>> getCategoriesByParentId(@Param("parentId") Long parentId);
    
    /**
     * 根据ID获取分类详情
     * @param categoryId 分类ID
     * @return 分类详情
     */
    @Select("SELECT category_id, parent_id, category_name, icon, sort_order, status, create_time, update_time " +
            "FROM shop_category WHERE category_id = #{categoryId}")
    Map<String, Object> getCategoryById(Long categoryId);
    
    /**
     * 创建新分类
     */
    @Insert("INSERT INTO shop_category (parent_id, category_name, icon, sort_order, status, create_time, update_time) " +
            "VALUES (#{parentId}, #{categoryName}, #{icon}, #{sortOrder}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "categoryId")
    int insertCategory(@Param("parentId") Long parentId, @Param("categoryName") String categoryName, 
                      @Param("icon") String icon, @Param("sortOrder") Integer sortOrder, 
                      @Param("status") Integer status, @Param("categoryId") Long categoryId);
    
    /**
     * 更新分类
     */
    @Update("UPDATE shop_category SET parent_id = #{parentId}, category_name = #{categoryName}, " +
            "icon = #{icon}, sort_order = #{sortOrder}, status = #{status}, update_time = NOW() " +
            "WHERE category_id = #{categoryId}")
    int updateCategory(@Param("categoryId") Long categoryId, @Param("parentId") Long parentId, 
                      @Param("categoryName") String categoryName, @Param("icon") String icon, 
                      @Param("sortOrder") Integer sortOrder, @Param("status") Integer status);
    
    /**
     * 删除分类
     * @param categoryId 分类ID
     * @return 删除条数
     */
    @Delete("DELETE FROM shop_category WHERE category_id = #{categoryId}")
    int deleteCategory(Long categoryId);
    
    /**
     * 更新分类状态
     * @param categoryId 分类ID
     * @param status 状态
     * @return 更新条数
     */
    @Update("UPDATE shop_category SET status = #{status}, update_time = NOW() WHERE category_id = #{categoryId}")
    int updateCategoryStatus(@Param("categoryId") Long categoryId, @Param("status") Integer status);
    
    // ==================== 用户管理 ====================
    
    /**
     * 获取所有用户列表（分页）
     * @param offset 偏移量
     * @param size 每页数量
     * @return 用户列表
     */
    @Select("SELECT user_id, username, email, phone_number, avatar, gender, role, registration_time, " +
            "last_login_time, last_login_ip, status, member_type, member_expiry_date " +
            "FROM users WHERE deleted = 0 ORDER BY registration_time DESC LIMIT #{offset}, #{size}")
    List<Map<String, Object>> getAllUsers(@Param("offset") int offset, @Param("size") int size);
    
    /**
     * 获取用户总数
     * @return 用户总数
     */
    @Select("SELECT COUNT(*) FROM users WHERE deleted = 0")
    int countAllUsers();
    
    /**
     * 根据状态获取用户列表
     * @param status 用户状态
     * @param offset 偏移量
     * @param size 每页数量
     * @return 用户列表
     */
    @Select("SELECT user_id, username, email, phone_number, avatar, gender, role, registration_time, " +
            "last_login_time, last_login_ip, status, member_type, member_expiry_date " +
            "FROM users WHERE deleted = 0 AND status = #{status} ORDER BY registration_time DESC LIMIT #{offset}, #{size}")
    List<Map<String, Object>> getUsersByStatus(@Param("status") Integer status, @Param("offset") int offset, @Param("size") int size);
    
    /**
     * 根据状态统计用户数量
     * @param status 用户状态
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM users WHERE deleted = 0 AND status = #{status}")
    int countUsersByStatus(@Param("status") Integer status);
    
    /**
     * 根据用户名搜索用户（分页）
     * @param username 用户名
     * @param offset 偏移量
     * @param size 每页数量
     * @return 用户列表
     */
    @Select("SELECT user_id, username, email, phone_number, avatar, gender, role, registration_time, " +
            "last_login_time, last_login_ip, status, member_type, member_expiry_date " +
            "FROM users WHERE username LIKE CONCAT('%', #{username}, '%') AND deleted = 0 " +
            "ORDER BY registration_time DESC LIMIT #{offset}, #{size}")
    List<Map<String, Object>> searchUsersByUsername(@Param("username") String username, 
                                                   @Param("offset") int offset, @Param("size") int size);
    
    /**
     * 搜索用户总数
     * @param username 用户名
     * @return 用户总数
     */
    @Select("SELECT COUNT(*) FROM users WHERE username LIKE CONCAT('%', #{username}, '%') AND deleted = 0")
    int countUsersByUsername(@Param("username") String username);
    
    /**
     * 根据用户ID获取用户详细信息
     * @param userId 用户ID
     * @return 用户详细信息
     */
    @Select("SELECT user_id, username, password, salt, phone_number, email, avatar, gender, birthday, " +
            "signature, region, role, registration_time, last_login_time, last_login_ip, status, " +
            "member_type, member_expiry_date, member_benefits, background_image, introduction, " +
            "follow_count, fans_count, subscription_count " +
            "FROM users WHERE user_id = #{userId} AND deleted = 0")
    Map<String, Object> getUserDetailById(Long userId);
    
    /**
     * 更新用户状态
     * @param userId 用户ID
     * @param status 状态
     * @return 更新条数
     */
    @Update("UPDATE users SET status = #{status}, update_time = NOW() WHERE user_id = #{userId}")
    int updateUserStatus(@Param("userId") Long userId, @Param("status") Integer status);
    
    /**
     * 重置用户密码
     * @param userId 用户ID
     * @param password 新密码
     * @param salt 盐值
     * @return 更新条数
     */
    @Update("UPDATE users SET password = #{password}, salt = #{salt}, update_time = NOW() WHERE user_id = #{userId}")
    int resetUserPassword(@Param("userId") Long userId, @Param("password") String password, @Param("salt") String salt);
    
    // ==================== 系统统计 ====================
    
    /**
     * 获取用户总数统计
     * @return 用户统计数据
     */
    @Select("SELECT " +
            "COUNT(*) as totalUsers, " +
            "COUNT(CASE WHEN status = 0 THEN 1 END) as activeUsers, " +
            "COUNT(CASE WHEN status = 1 THEN 1 END) as inactiveUsers, " +
            "COUNT(CASE WHEN DATE(registration_time) = CURDATE() THEN 1 END) as todayNewUsers " +
            "FROM users WHERE deleted = 0")
    Map<String, Object> getUserStatistics();
    
    /**
     * 获取商品总数统计
     * @return 商品统计数据
     */
    @Select("SELECT " +
            "COUNT(*) as totalProducts, " +
            "COUNT(CASE WHEN status = 1 THEN 1 END) as activeProducts, " +
            "COUNT(CASE WHEN status = 0 THEN 1 END) as inactiveProducts, " +
            "COUNT(CASE WHEN DATE(create_time) = CURDATE() THEN 1 END) as todayNewProducts " +
            "FROM shop_product WHERE is_deleted = 0")
    Map<String, Object> getProductStatistics();
    
    /**
     * 获取订单总数统计
     * @return 订单统计数据
     */
    @Select("SELECT " +
            "COUNT(*) as totalOrders, " +
            "COUNT(CASE WHEN order_status = 1 THEN 1 END) as pending_orders, " +
            "COUNT(CASE WHEN order_status = 2 THEN 1 END) as paid_orders, " +
            "COUNT(CASE WHEN order_status = 3 THEN 1 END) as shipped_orders, " +
            "COUNT(CASE WHEN order_status = 4 THEN 1 END) as completed_orders, " +
            "COUNT(CASE WHEN DATE(create_time) = CURDATE() THEN 1 END) as today_new_orders, " +
            "COALESCE(SUM(total_amount), 0) as totalSales " +
            "FROM orders")
    Map<String, Object> getOrderStatistics();
    
    /**
     * 获取Banner总数统计
     * @return Banner统计数据
     */
    @Select("SELECT " +
            "COUNT(*) as total_banners, " +
            "COUNT(CASE WHEN status = 1 THEN 1 END) as active_banners, " +
            "COUNT(CASE WHEN status = 0 THEN 1 END) as inactive_banners " +
            "FROM t_banner")
    Map<String, Object> getBannerStatistics();
    
    /**
     * 获取分类总数统计
     * @return 分类统计数据
     */
    @Select("SELECT " +
            "COUNT(*) as total_categories, " +
            "COUNT(CASE WHEN status = 1 THEN 1 END) as active_categories, " +
            "COUNT(CASE WHEN status = 0 THEN 1 END) as inactive_categories, " +
            "COUNT(CASE WHEN parent_id = 0 THEN 1 END) as top_level_categories " +
            "FROM shop_category")
    Map<String, Object> getCategoryStatistics();
    
    // ==================== 支付渠道管理 ====================
    
    /**
     * 获取所有支付渠道列表
     * @return 支付渠道列表
     */
    @Select("SELECT channel_id, channel_code, channel_name, icon_url, description, is_enabled, " +
            "sort_order, config, create_time, update_time " +
            "FROM pay_channels ORDER BY sort_order ASC, create_time DESC")
    List<Map<String, Object>> getAllPaymentChannels();
    
    /**
     * 根据ID获取支付渠道详情
     * @param channelId 支付渠道ID
     * @return 支付渠道详情
     */
    @Select("SELECT channel_id, channel_code, channel_name, icon_url, description, is_enabled, " +
            "sort_order, config, create_time, update_time " +
            "FROM pay_channels WHERE channel_id = #{channelId}")
    Map<String, Object> getPaymentChannelById(@Param("channelId") Integer channelId);
    
    /**
     * 创建新支付渠道
     */
    @Insert("INSERT INTO pay_channels (channel_code, channel_name, icon_url, description, " +
            "is_enabled, sort_order, config, create_time, update_time) " +
            "VALUES (#{channelCode}, #{channelName}, #{iconUrl}, #{description}, " +
            "#{isEnabled}, #{sortOrder}, #{config}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "channelId")
    int createPaymentChannel(@Param("channelCode") String channelCode, 
                            @Param("channelName") String channelName,
                            @Param("iconUrl") String iconUrl, 
                            @Param("description") String description,
                            @Param("isEnabled") Integer isEnabled, 
                            @Param("sortOrder") Integer sortOrder,
                            @Param("config") String config,
                            @Param("channelId") Integer channelId);
    
    /**
     * 更新支付渠道
     */
    @Update("UPDATE pay_channels SET channel_code = #{channelCode}, channel_name = #{channelName}, " +
            "icon_url = #{iconUrl}, description = #{description}, is_enabled = #{isEnabled}, " +
            "sort_order = #{sortOrder}, config = #{config}, update_time = NOW() " +
            "WHERE channel_id = #{channelId}")
    int updatePaymentChannel(@Param("channelId") Integer channelId,
                            @Param("channelCode") String channelCode, 
                            @Param("channelName") String channelName,
                            @Param("iconUrl") String iconUrl, 
                            @Param("description") String description,
                            @Param("isEnabled") Integer isEnabled, 
                            @Param("sortOrder") Integer sortOrder,
                            @Param("config") String config);
    
    /**
     * 删除支付渠道
     * @param channelId 渠道ID
     * @return 删除条数
     */
    @Delete("DELETE FROM pay_channels WHERE channel_id = #{channelId}")
    int deletePaymentChannel(@Param("channelId") Integer channelId);
    
    /**
     * 更新支付渠道状态
     * @param channelId 渠道ID
     * @param isEnabled 是否启用
     * @return 更新条数
     */
    @Update("UPDATE pay_channels SET is_enabled = #{isEnabled}, update_time = NOW() " +
            "WHERE channel_id = #{channelId}")
    int updatePaymentChannelStatus(@Param("channelId") Integer channelId, @Param("isEnabled") Integer isEnabled);
    
    // ==================== 轮播图管理 ====================
    
    /**
     * 获取轮播图列表
     * @return 轮播图列表
     */
    @Select("SELECT banner_id, title, image_url, link_url, position, status, sort_order, " +
            "remark as description, create_time, update_time FROM t_banner ORDER BY sort_order ASC, create_time DESC")
    List<Map<String, Object>> getCarouselBanners();
    
    /**
     * 根据位置获取轮播图
     * @param position 位置
     * @return 轮播图列表
     */
    @Select("SELECT banner_id, title, image_url, link_url, position, status, sort_order, " +
            "remark as description, create_time, update_time FROM t_banner WHERE position = #{position} ORDER BY sort_order ASC")
    List<Map<String, Object>> getCarouselBannersByPosition(@Param("position") String position);
    
    /**
     * 根据状态获取轮播图
     * @param status 状态
     * @return 轮播图列表
     */
    @Select("SELECT banner_id, title, image_url, link_url, position, status, sort_order, " +
            "remark as description, create_time, update_time FROM t_banner WHERE status = #{status} ORDER BY sort_order ASC")
    List<Map<String, Object>> getCarouselBannersByStatus(@Param("status") Integer status);
    
    /**
     * 根据ID获取轮播图详情
     * @param bannerId 轮播图ID
     * @return 轮播图详情
     */
    @Select("SELECT banner_id, title, image_url, link_url, position, status, sort_order, " +
            "remark as description, create_time, update_time FROM t_banner WHERE banner_id = #{bannerId}")
    Map<String, Object> getCarouselBannerById(Long bannerId);
    
    /**
     * 创建新轮播图
     */
    @Insert("INSERT INTO t_banner (title, image_url, link_url, position, status, sort_order, " +
            "remark, create_time, update_time) VALUES (#{title}, #{imageUrl}, #{linkUrl}, " +
            "#{position}, #{status}, #{sortOrder}, #{description}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "bannerId")
    Long createCarouselBanner(@Param("title") String title, @Param("imageUrl") String imageUrl,
                             @Param("linkUrl") String linkUrl, @Param("position") String position,
                             @Param("status") Integer status, @Param("sortOrder") Integer sortOrder,
                             @Param("description") String description);
    
    /**
     * 更新轮播图
     */
    @Update("UPDATE t_banner SET title = #{title}, image_url = #{imageUrl}, link_url = #{linkUrl}, " +
            "position = #{position}, status = #{status}, sort_order = #{sortOrder}, " +
            "remark = #{description}, update_time = NOW() WHERE banner_id = #{bannerId}")
    int updateCarouselBanner(@Param("bannerId") Long bannerId, @Param("title") String title,
                            @Param("imageUrl") String imageUrl, @Param("linkUrl") String linkUrl,
                            @Param("position") String position, @Param("status") Integer status,
                            @Param("sortOrder") Integer sortOrder, @Param("description") String description);
    
    /**
     * 删除轮播图
     * @param bannerId 轮播图ID
     * @return 删除条数
     */
    @Delete("DELETE FROM t_banner WHERE banner_id = #{bannerId}")
    int deleteCarouselBanner(Long bannerId);
    
    // ==================== 登录记录查询 ====================
    
    /**
     * 获取登录记录列表（基础版本）
     * @param offset 偏移量
     * @param size 每页数量
     * @return 登录记录列表
     */
    @Select("SELECT lr.id as record_id, lr.user_id, u.username, u.role, lr.login_ip, " +
            "lr.login_device, lr.login_time, lr.login_status, lr.login_message " +
            "FROM user_login_history lr LEFT JOIN users u ON lr.user_id = u.user_id " +
            "ORDER BY lr.login_time DESC LIMIT #{offset}, #{size}")
    List<Map<String, Object>> getLoginRecords(@Param("offset") int offset, @Param("size") int size);
    
    /**
     * 获取登录记录总数
     * @return 记录总数
     */
    @Select("SELECT COUNT(*) FROM user_login_history")
    int getLoginRecordsCount();
    
    /**
     * 根据用户ID获取登录记录
     * @param userId 用户ID
     * @param offset 偏移量
     * @param size 每页数量
     * @return 登录记录列表
     */
    @Select("SELECT lr.id as record_id, lr.user_id, u.username, u.role, lr.login_ip, " +
            "lr.login_device, lr.login_time, lr.login_status, lr.login_message " +
            "FROM user_login_history lr LEFT JOIN users u ON lr.user_id = u.user_id " +
            "WHERE lr.user_id = #{userId} ORDER BY lr.login_time DESC LIMIT #{offset}, #{size}")
    List<Map<String, Object>> getLoginRecordsByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("size") int size);
    
    /**
     * 根据用户ID统计登录记录数
     * @param userId 用户ID
     * @return 记录数
     */
    @Select("SELECT COUNT(*) FROM user_login_history WHERE user_id = #{userId}")
    int getLoginRecordsCountByUserId(@Param("userId") Long userId);
    
    /**
     * 根据ID获取登录记录详情
     * @param recordId 记录ID
     * @return 登录记录详情
     */
    @Select("SELECT lr.id as record_id, lr.user_id, u.username, u.role, lr.login_ip, " +
            "lr.login_device, lr.login_time, lr.login_status, lr.login_message " +
            "FROM user_login_history lr LEFT JOIN users u ON lr.user_id = u.user_id WHERE lr.id = #{recordId}")
    Map<String, Object> getLoginRecordById(Long recordId);
    
    /**
     * 获取登录统计信息 - 按天分组
     * @return 统计信息
     */
    @Select("SELECT DATE(login_time) as date_group, COUNT(*) as login_count, " +
            "COUNT(DISTINCT user_id) as unique_users FROM user_login_history " +
            "GROUP BY DATE(login_time) ORDER BY date_group DESC LIMIT 30")
    List<Map<String, Object>> getLoginStatisticsByDay();
    
    /**
     * 获取登录总体统计
     * @return 总体统计
     */
    @Select("SELECT COUNT(*) as total_logins, COUNT(DISTINCT user_id) as unique_users, " +
            "COUNT(CASE WHEN login_status = 1 THEN 1 END) as success_logins, " +
            "COUNT(CASE WHEN login_status = 0 THEN 1 END) as failed_logins " +
            "FROM user_login_history")
    Map<String, Object> getLoginSummary();
} 