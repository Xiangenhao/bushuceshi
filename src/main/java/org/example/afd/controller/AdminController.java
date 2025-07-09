package org.example.afd.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.afd.dto.BannerDTO;
import org.example.afd.dto.CategoryDTO;
import org.example.afd.model.*;
import org.example.afd.service.AdminService;
import org.example.afd.service.impl.AdminServiceImpl;
import org.example.afd.utils.UserIdHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 管理员控制器
 * 提供后台管理功能，包括管理员认证、Banner管理、分类管理、用户管理等
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    
    @Autowired
    private AdminService adminService;
    
    @Autowired
    private AdminServiceImpl adminServiceImpl;
    
    // ==================== 管理员认证相关接口 ====================
    
    /**
     * 管理员注册
     */
    @PostMapping("/register")
    public ResponseEntity<Result<Object>> adminRegister(@RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            log.info("管理员注册请求: {}", request);
            // 设置客户端信息
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            // 调用管理员服务注册
            Map<String, Object> result = adminServiceImpl.adminRegister(request);
            
            if ((Boolean) result.get("success")) {
                return ResponseEntity.ok(Result.success("管理员注册成功"));
            } else {
                return ResponseEntity.ok(Result.error((String) result.get("message")));
            }
        } catch (Exception e) {
            log.error("管理员注册失败", e);
            return ResponseEntity.ok(Result.error("注册失败: " + e.getMessage()));
        }
    }
    
    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public ResponseEntity<Result<AuthResponse>> adminLogin(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            log.info("管理员登录请求: {}", request);
            // 设置客户端信息
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            // 调用管理员服务登录
            AuthResponse authResponse = adminServiceImpl.adminLogin(request);
            return ResponseEntity.ok(Result.success("管理员登录成功", authResponse));
        } catch (Exception e) {
            log.error("管理员登录失败", e);
            return ResponseEntity.ok(Result.error("登录失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取管理员信息
     */
    @GetMapping("/info")
    public ResponseEntity<Result<Map<String, Object>>> getAdminInfo() {
        try {
            Integer adminId = UserIdHolder.getUserId();
            if (adminId == null) {
                return ResponseEntity.ok(Result.failure(ResultCode.UNAUTHORIZED, "用户未登录"));
            }
            
            if (!adminServiceImpl.checkAdminPermission(adminId)) {
                return ResponseEntity.ok(Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限"));
            }
            
            Map<String, Object> adminInfo = adminService.getAdminInfo(adminId);
            if (adminInfo == null) {
                return ResponseEntity.ok(Result.failure(ResultCode.NOT_FOUND, "管理员信息不存在"));
            }
            
            return ResponseEntity.ok(Result.success(adminInfo));
        } catch (Exception e) {
            log.error("获取管理员信息失败", e);
            return ResponseEntity.ok(Result.failure(ResultCode.SYSTEM_ERROR, "获取管理员信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新管理员信息
     */
    @PutMapping("/info")
    public ResponseEntity<Result<Boolean>> updateAdminInfo(@RequestBody Map<String, Object> adminInfo) {
        try {
            Integer adminId = UserIdHolder.getUserId();
            if (adminId == null) {
                return ResponseEntity.ok(Result.failure(ResultCode.UNAUTHORIZED, "用户未登录"));
            }
            
            if (!adminServiceImpl.checkAdminPermission(adminId)) {
                return ResponseEntity.ok(Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限"));
            }
            
            String username = (String) adminInfo.get("username");
            String email = (String) adminInfo.get("email");
            String phoneNumber = (String) adminInfo.get("phoneNumber");
            String avatar = (String) adminInfo.get("avatar");
            
            boolean updated = adminService.updateAdminInfo(adminId, username, email, phoneNumber, avatar);
            if (updated) {
                return ResponseEntity.ok(Result.success(true));
            } else {
                return ResponseEntity.ok(Result.failure(ResultCode.SYSTEM_ERROR, "更新管理员信息失败"));
            }
        } catch (Exception e) {
            log.error("更新管理员信息失败", e);
            return ResponseEntity.ok(Result.failure(ResultCode.SYSTEM_ERROR, "更新管理员信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 验证管理员权限
     * 检查当前用户是否具有管理员权限
     */
    private boolean checkAdminPermission() {
        try {
            Integer userId = UserIdHolder.getUserId();
            if (userId == null) {
                log.warn("管理员权限验证失败：用户未登录");
                return false;
            }
            
            // 临时设置用户ID 7为管理员用于测试
            if (userId == 7) {
                return true;
            }
            
            return adminServiceImpl.checkAdminPermission(userId);
        } catch (Exception e) {
            log.error("管理员权限验证异常", e);
            return false;
        }
    }
    
    // ==================== Banner管理接口 ====================
    
    /**
     * 获取所有Banner列表
     * @param position 位置筛选，可选参数
     * @return Banner列表
     */
    @GetMapping("/banners")
    public Result<List<Map<String, Object>>> getAllBanners(
            @RequestParam(required = false) String position) {
        log.info("=== 管理员获取Banner列表 ===");
        log.info("请求参数: position={}", position);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            List<Map<String, Object>> banners = adminService.getAllBanners(position);
            log.info("获取Banner列表成功: position={}, count={}", position, banners.size());
            return Result.success(banners);
        } catch (Exception e) {
            log.error("获取Banner列表失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取Banner列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取Banner详情
     * @param bannerId Banner ID
     * @return Banner详情
     */
    @GetMapping("/banners/{bannerId}")
    public Result<Map<String, Object>> getBannerById(@PathVariable Long bannerId) {
        log.info("=== 管理员获取Banner详情 ===");
        log.info("请求参数: bannerId={}", bannerId);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            Map<String, Object> banner = adminService.getBannerById(bannerId);
            if (banner == null) {
                log.warn("Banner不存在: bannerId={}", bannerId);
                return Result.failure(ResultCode.NOT_FOUND, "Banner不存在");
            }
            
            log.info("获取Banner详情成功: bannerId={}", bannerId);
            return Result.success(banner);
        } catch (Exception e) {
            log.error("获取Banner详情失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取Banner详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建新Banner
     * @param banner Banner信息
     * @return 创建结果
     */
    @PostMapping("/banners")
    public Result<Boolean> createBanner(@RequestBody BannerDTO banner) {
        log.info("=== 管理员创建Banner ===");
        log.info("请求参数: {}", banner);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            boolean created = adminService.createBanner(
                banner.getTitle(),
                banner.getImageUrl(),
                banner.getLinkType(),
                banner.getTargetId(),
                banner.getLinkUrl(),
                banner.getPosition(),
                banner.getSortOrder(),
                banner.getStartTime(),
                banner.getEndTime(),
                banner.getStatus(),
                banner.getRemark()
            );
            
            if (created) {
                log.info("创建Banner成功");
                return Result.success(true);
            } else {
                log.warn("创建Banner失败");
                return Result.failure(ResultCode.SYSTEM_ERROR, "创建Banner失败");
            }
        } catch (Exception e) {
            log.error("创建Banner失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "创建Banner失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新Banner
     * @param bannerId Banner ID
     * @param banner Banner信息
     * @return 更新结果
     */
    @PutMapping("/banners/{bannerId}")
    public Result<Boolean> updateBanner(@PathVariable Long bannerId, @RequestBody BannerDTO banner) {
        log.info("=== 管理员更新Banner ===");
        log.info("请求参数: bannerId={}, banner={}", bannerId, banner);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            boolean updated = adminService.updateBanner(
                bannerId,
                banner.getTitle(),
                banner.getImageUrl(),
                banner.getLinkType(),
                banner.getTargetId(),
                banner.getLinkUrl(),
                banner.getPosition(),
                banner.getSortOrder(),
                banner.getStartTime(),
                banner.getEndTime(),
                banner.getStatus(),
                banner.getRemark()
            );
            
            if (updated) {
                log.info("更新Banner成功: bannerId={}", bannerId);
                return Result.success(true);
            } else {
                log.warn("更新Banner失败: bannerId={}", bannerId);
                return Result.failure(ResultCode.SYSTEM_ERROR, "更新Banner失败");
            }
        } catch (Exception e) {
            log.error("更新Banner失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "更新Banner失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除Banner
     * @param bannerId Banner ID
     * @return 删除结果
     */
    @DeleteMapping("/banners/{bannerId}")
    public Result<Boolean> deleteBanner(@PathVariable Long bannerId) {
        log.info("=== 管理员删除Banner ===");
        log.info("请求参数: bannerId={}", bannerId);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            boolean deleted = adminService.deleteBanner(bannerId);
            if (deleted) {
                log.info("删除Banner成功: bannerId={}", bannerId);
                return Result.success(true);
            } else {
                log.warn("删除Banner失败: bannerId={}", bannerId);
                return Result.failure(ResultCode.SYSTEM_ERROR, "删除Banner失败");
            }
        } catch (Exception e) {
            log.error("删除Banner失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "删除Banner失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量删除Banner
     * @param bannerIds Banner ID列表
     * @return 删除结果
     */
    @DeleteMapping("/banners")
    public Result<Boolean> deleteBanners(@RequestBody List<Long> bannerIds) {
        log.info("=== 管理员批量删除Banner ===");
        log.info("请求参数: bannerIds={}", bannerIds);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            if (bannerIds == null || bannerIds.isEmpty()) {
                return Result.failure(ResultCode.PARAM_ERROR, "Banner ID列表不能为空");
            }
            
            int deletedCount = 0;
            for (Long bannerId : bannerIds) {
                try {
                    if (adminService.deleteBanner(bannerId)) {
                        deletedCount++;
                    }
                } catch (Exception e) {
                    log.warn("删除Banner失败: bannerId={}", bannerId, e);
                }
            }
            
            log.info("批量删除Banner完成: 总数={}, 成功={}", bannerIds.size(), deletedCount);
            if (deletedCount > 0) {
                return Result.success(true);
            } else {
                return Result.failure(ResultCode.SYSTEM_ERROR, "删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除Banner失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "批量删除Banner失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新Banner状态
     * @param bannerId Banner ID
     * @param statusMap 状态信息
     * @return 更新结果
     */
    @PutMapping("/banners/{bannerId}/status")
    public Result<Boolean> updateBannerStatus(@PathVariable Long bannerId, @RequestBody Map<String, Object> statusMap) {
        log.info("=== 管理员更新Banner状态 ===");
        log.info("请求参数: bannerId={}, statusMap={}", bannerId, statusMap);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            Integer status = (Integer) statusMap.get("status");
            if (status == null) {
                return Result.failure(ResultCode.PARAM_ERROR, "状态参数不能为空");
            }
            
            boolean updated = adminService.updateBannerStatus(bannerId, status);
            if (updated) {
                log.info("更新Banner状态成功: bannerId={}, status={}", bannerId, status);
                return Result.success(true);
            } else {
                log.warn("更新Banner状态失败: bannerId={}, status={}", bannerId, status);
                return Result.failure(ResultCode.NOT_FOUND, "Banner不存在或更新失败");
            }
        } catch (Exception e) {
            log.error("更新Banner状态失败: bannerId=" + bannerId, e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "更新Banner状态失败: " + e.getMessage());
        }
    }
    
    // ==================== 分类管理接口 ====================
    
    /**
     * 获取所有分类列表
     * @param parentId 父分类ID，可选参数
     * @return 分类列表
     */
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> getAllCategories(
            @RequestParam(required = false) Long parentId) {
        log.info("=== 管理员获取分类列表 ===");
        log.info("请求参数: parentId={}", parentId);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            List<Map<String, Object>> categories = adminService.getAllCategories(parentId);
            log.info("获取分类列表成功: parentId={}, count={}", parentId, categories.size());
            return Result.success(categories);
        } catch (Exception e) {
            log.error("获取分类列表失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取分类列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取分类详情
     * @param categoryId 分类ID
     * @return 分类详情
     */
    @GetMapping("/categories/{categoryId}")
    public Result<Map<String, Object>> getCategoryById(@PathVariable Long categoryId) {
        log.info("=== 管理员获取分类详情 ===");
        log.info("请求参数: categoryId={}", categoryId);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            Map<String, Object> category = adminService.getCategoryById(categoryId);
            if (category == null) {
                log.warn("分类不存在: categoryId={}", categoryId);
                return Result.failure(ResultCode.NOT_FOUND, "分类不存在");
            }
            
            log.info("获取分类详情成功: categoryId={}", categoryId);
            return Result.success(category);
        } catch (Exception e) {
            log.error("获取分类详情失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取分类详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建新分类
     * @param category 分类信息
     * @return 创建结果
     */
    @PostMapping("/categories")
    public Result<Boolean> createCategory(@RequestBody CategoryDTO category) {
        log.info("=== 管理员创建分类 ===");
        log.info("请求参数: {}", category);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            boolean created = adminService.createCategory(
                category.getParentId(),
                category.getCategoryName(),
                category.getIcon(),
                category.getSortOrder(),
                category.getStatus()
            );
            
            if (created) {
                log.info("创建分类成功");
                return Result.success(true);
            } else {
                log.warn("创建分类失败");
                return Result.failure(ResultCode.SYSTEM_ERROR, "创建分类失败");
            }
        } catch (Exception e) {
            log.error("创建分类失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "创建分类失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新分类
     * @param categoryId 分类ID
     * @param category 分类信息
     * @return 更新结果
     */
    @PutMapping("/categories/{categoryId}")
    public Result<Boolean> updateCategory(@PathVariable Long categoryId, @RequestBody CategoryDTO category) {
        log.info("=== 管理员更新分类 ===");
        log.info("请求参数: categoryId={}, category={}", categoryId, category);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            boolean updated = adminService.updateCategory(
                categoryId,
                category.getParentId(),
                category.getCategoryName(),
                category.getIcon(),
                category.getSortOrder(),
                category.getStatus()
            );
            
            if (updated) {
                log.info("更新分类成功: categoryId={}", categoryId);
                return Result.success(true);
            } else {
                log.warn("更新分类失败: categoryId={}", categoryId);
                return Result.failure(ResultCode.SYSTEM_ERROR, "更新分类失败");
            }
        } catch (Exception e) {
            log.error("更新分类失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "更新分类失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除分类
     * @param categoryId 分类ID
     * @return 删除结果
     */
    @DeleteMapping("/categories/{categoryId}")
    public Result<Boolean> deleteCategory(@PathVariable Long categoryId) {
        log.info("=== 管理员删除分类 ===");
        log.info("请求参数: categoryId={}", categoryId);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            boolean deleted = adminService.deleteCategory(categoryId);
            if (deleted) {
                log.info("删除分类成功: categoryId={}", categoryId);
                return Result.success(true);
            } else {
                log.warn("删除分类失败: categoryId={}", categoryId);
                return Result.failure(ResultCode.NOT_FOUND, "分类不存在或删除失败");
            }
        } catch (Exception e) {
            log.error("删除分类失败: categoryId=" + categoryId, e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "删除分类失败: " + e.getMessage());
        }
    }
    
    // ==================== 用户管理接口 ====================
    
    /**
     * 根据用户名搜索用户
     * @param username 用户名（支持模糊搜索）
     * @param page 页码，默认1
     * @param size 每页数量，默认10
     * @return 用户列表
     */
    @GetMapping("/users/search")
    public Result<Map<String, Object>> searchUsersByUsername(
            @RequestParam String username,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("=== 管理员搜索用户 ===");
        log.info("请求参数: username={}, page={}, size={}", username, page, size);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            if (username == null || username.trim().isEmpty()) {
                return Result.failure(ResultCode.PARAM_ERROR, "用户名不能为空");
            }
            
            Map<String, Object> result = adminService.searchUsersByUsername(username, page, size);
            log.info("搜索用户成功: username={}, 找到用户数={}", username, result.get("total"));
            return Result.success(result);
        } catch (Exception e) {
            log.error("搜索用户失败: username=" + username, e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "搜索用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有用户列表
     * @param page 页码，默认1
     * @param size 每页数量，默认10
     * @param status 用户状态筛选，可选
     * @return 用户列表
     */
    @GetMapping("/users")
    public Result<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        log.info("=== 管理员获取用户列表 ===");
        log.info("请求参数: page={}, size={}, status={}", page, size, status);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            Map<String, Object> result = adminService.getAllUsers(status, page, size);
            log.info("获取用户列表成功: 共{}个用户", result.get("total"));
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取用户列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据用户ID获取用户详细信息
     * @param userId 用户ID
     * @return 用户详细信息
     */
    @GetMapping("/users/{userId}")
    public Result<Map<String, Object>> getUserById(@PathVariable Long userId) {
        log.info("=== 管理员获取用户详情 ===");
        log.info("请求参数: userId={}", userId);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            Map<String, Object> userInfo = adminService.getUserDetailById(userId);
            if (userInfo == null) {
                log.warn("用户不存在: userId={}", userId);
                return Result.failure(ResultCode.NOT_FOUND, "用户不存在");
            }
            
            log.info("获取用户详情成功: userId={}", userId);
            return Result.success(userInfo);
        } catch (Exception e) {
            log.error("获取用户详情失败: userId=" + userId, e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取用户详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户状态（启用/禁用）
     * @param userId 用户ID
     * @param statusMap 状态信息
     * @return 更新结果
     */
    @PutMapping("/users/{userId}/status")
    public Result<Boolean> updateUserStatus(
            @PathVariable Long userId, 
            @RequestBody Map<String, Object> statusMap) {
        log.info("=== 管理员更新用户状态 ===");
        log.info("请求参数: userId={}, statusMap={}", userId, statusMap);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            Integer status = (Integer) statusMap.get("status");
            if (status == null) {
                return Result.failure(ResultCode.PARAM_ERROR, "状态参数不能为空");
            }
            
            boolean updated = adminService.updateUserStatus(userId, status);
            if (updated) {
                log.info("更新用户状态成功: userId={}, status={}", userId, status);
                return Result.success(true);
            } else {
                log.warn("更新用户状态失败: userId={}, status={}", userId, status);
                return Result.failure(ResultCode.NOT_FOUND, "用户不存在或更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户状态失败: userId=" + userId, e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "更新用户状态失败: " + e.getMessage());
        }
    }
    
    // ==================== 系统统计接口 ====================
    
    /**
     * 获取系统统计数据（仪表板用）
     * @return 系统统计数据
     */
    @GetMapping("/statistics/system")
    public Result<Map<String, Object>> getSystemStatisticsForDashboard() {
        log.info("=== 获取系统统计数据（仪表板） ===");
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            Map<String, Object> systemStats = adminService.getSystemStatistics();
            
            // 重新构造仪表盘需要的数据格式
            Map<String, Object> dashboardStats = new HashMap<>();
            
            // 从各个统计模块提取数据
            Map<String, Object> userStats = (Map<String, Object>) systemStats.get("userStatistics");
            Map<String, Object> productStats = (Map<String, Object>) systemStats.get("productStatistics");
            Map<String, Object> orderStats = (Map<String, Object>) systemStats.get("orderStatistics");
            
            // 设置仪表盘数据
            dashboardStats.put("totalUsers", userStats != null ? userStats.get("totalUsers") : 0);
            dashboardStats.put("totalProducts", productStats != null ? productStats.get("totalProducts") : 0);
            dashboardStats.put("totalOrders", orderStats != null ? orderStats.get("totalOrders") : 0);
            dashboardStats.put("totalSales", orderStats != null ? orderStats.get("totalSales") : 0);
            
            log.info("获取仪表盘统计数据成功: {}", dashboardStats);
            return Result.success(dashboardStats);
        } catch (Exception e) {
            log.error("获取系统统计数据失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取系统统计数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取系统统计数据
     * @return 系统统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getSystemStatistics() {
        log.info("=== 获取系统统计数据 ===");
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            Map<String, Object> systemStats = adminService.getSystemStatistics();
            Map<String, Object> statistics = (Map<String, Object>) systemStats.get("userStatistics");
            if (statistics == null) {
                statistics = new java.util.HashMap<>();
            }
            
            log.info("获取系统统计数据成功");
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取系统统计数据失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取系统统计数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户统计信息
     * @return 用户统计数据
     */
    @GetMapping("/statistics/users")
    public Result<Map<String, Object>> getUserStatistics() {
        log.info("=== 管理员获取用户统计信息 ===");
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            Map<String, Object> systemStats = adminService.getSystemStatistics();
            Map<String, Object> statistics = (Map<String, Object>) systemStats.get("userStatistics");
            if (statistics == null) {
                statistics = new java.util.HashMap<>();
            }
            
            log.info("获取用户统计信息成功");
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取用户统计信息失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取用户统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取商品统计信息
     * @return 商品统计数据
     */
    @GetMapping("/statistics/products")
    public Result<Map<String, Object>> getProductStatistics() {
        log.info("=== 管理员获取商品统计信息 ===");
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            Map<String, Object> systemStats = adminService.getSystemStatistics();
            Map<String, Object> statistics = (Map<String, Object>) systemStats.get("productStatistics");
            if (statistics == null) {
                statistics = new HashMap<>();
            }
            
            log.info("获取商品统计信息成功");
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取商品统计信息失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取商品统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取订单统计信息
     * @return 订单统计数据
     */
    @GetMapping("/statistics/orders")
    public Result<Map<String, Object>> getOrderStatistics() {
        log.info("=== 管理员获取订单统计信息 ===");
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            Map<String, Object> systemStats = adminService.getSystemStatistics();
            Map<String, Object> statistics = (Map<String, Object>) systemStats.get("orderStatistics");
            if (statistics == null) {
                statistics = new HashMap<>();
            }
            
            log.info("获取订单统计信息成功");
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取订单统计信息失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取订单统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取销售统计信息
     * @return 销售统计数据
     */
    @GetMapping("/statistics/sales")
    public Result<Map<String, Object>> getSalesStatistics() {
        log.info("=== 管理员获取销售统计信息 ===");
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            Map<String, Object> systemStats = adminService.getSystemStatistics();
            Map<String, Object> orderStats = (Map<String, Object>) systemStats.get("orderStatistics");
            
            // 构造销售统计数据
            Map<String, Object> salesStats = new HashMap<>();
            if (orderStats != null) {
                salesStats.put("totalSales", orderStats.get("totalSales"));
                salesStats.put("totalOrders", orderStats.get("totalOrders"));
                salesStats.put("completedOrders", orderStats.get("completed_orders"));
            }
            
            log.info("获取销售统计信息成功");
            return Result.success(salesStats);
        } catch (Exception e) {
            log.error("获取销售统计信息失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取销售统计信息失败: " + e.getMessage());
        }
    }

    // ==================== 支付渠道管理接口 ====================
    
    /**
     * 获取所有支付渠道列表
     * @return 支付渠道列表
     */
    @GetMapping("/payment-channels")
    public Result<List<Map<String, Object>>> getAllPaymentChannels() {
        log.info("=== 管理员获取支付渠道列表 ===");
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            List<Map<String, Object>> channels = adminService.getAllPaymentChannels();
            log.info("获取支付渠道列表成功: count={}", channels.size());
            return Result.success(channels);
        } catch (Exception e) {
            log.error("获取支付渠道列表失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取支付渠道列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取支付渠道详情
     * @param channelId 渠道ID
     * @return 支付渠道详情
     */
    @GetMapping("/payment-channels/{channelId}")
    public Result<Map<String, Object>> getPaymentChannelById(@PathVariable Integer channelId) {
        log.info("=== 管理员获取支付渠道详情 ===");
        log.info("请求参数: channelId={}", channelId);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            Map<String, Object> channel = adminService.getPaymentChannelById(channelId);
            if (channel == null) {
                return Result.failure(ResultCode.NOT_FOUND, "支付渠道不存在");
            }
            
            log.info("获取支付渠道详情成功");
            return Result.success(channel);
        } catch (Exception e) {
            log.error("获取支付渠道详情失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取支付渠道详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建支付渠道
     * @param channelData 支付渠道数据
     * @return 创建结果
     */
    @PostMapping("/payment-channels")
    public Result<Boolean> createPaymentChannel(@RequestBody Map<String, Object> channelData) {
        log.info("=== 管理员创建支付渠道 ===");
        log.info("请求参数: {}", channelData);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            // 验证必需参数
            if (!channelData.containsKey("channelCode") || !channelData.containsKey("channelName")) {
                return Result.failure(ResultCode.PARAM_ERROR, "渠道代码和渠道名称不能为空");
            }
            
            String channelCode = (String) channelData.get("channelCode");
            String channelName = (String) channelData.get("channelName");
            String iconUrl = (String) channelData.get("iconUrl");
            String description = (String) channelData.get("description");
            Integer isEnabled = channelData.containsKey("isEnabled") ? (Integer) channelData.get("isEnabled") : 1;
            Integer sortOrder = channelData.containsKey("sortOrder") ? (Integer) channelData.get("sortOrder") : 0;
            String config = channelData.containsKey("config") ? (String) channelData.get("config") : "{}";
            
            boolean created = adminService.createPaymentChannel(channelCode, channelName, iconUrl, description, 
                                                              isEnabled, sortOrder, config);
            if (created) {
                log.info("创建支付渠道成功");
                return Result.success(true);
            } else {
                return Result.failure(ResultCode.SYSTEM_ERROR, "创建支付渠道失败");
            }
        } catch (Exception e) {
            log.error("创建支付渠道失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "创建支付渠道失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新支付渠道
     * @param channelId 渠道ID
     * @param channelData 支付渠道数据
     * @return 更新结果
     */
    @PutMapping("/payment-channels/{channelId}")
    public Result<Boolean> updatePaymentChannel(@PathVariable Integer channelId, @RequestBody Map<String, Object> channelData) {
        log.info("=== 管理员更新支付渠道 ===");
        log.info("请求参数: channelId={}, data={}", channelId, channelData);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            // 验证必需参数
            if (!channelData.containsKey("channelCode") || !channelData.containsKey("channelName")) {
                return Result.failure(ResultCode.PARAM_ERROR, "渠道代码和渠道名称不能为空");
            }
            
            String channelCode = (String) channelData.get("channelCode");
            String channelName = (String) channelData.get("channelName");
            String iconUrl = (String) channelData.get("iconUrl");
            String description = (String) channelData.get("description");
            Integer isEnabled = channelData.containsKey("isEnabled") ? (Integer) channelData.get("isEnabled") : 1;
            Integer sortOrder = channelData.containsKey("sortOrder") ? (Integer) channelData.get("sortOrder") : 0;
            String config = channelData.containsKey("config") ? (String) channelData.get("config") : "{}";
            
            boolean updated = adminService.updatePaymentChannel(channelId, channelCode, channelName, iconUrl, description, 
                                                              isEnabled, sortOrder, config);
            if (updated) {
                log.info("更新支付渠道成功");
                return Result.success(true);
            } else {
                return Result.failure(ResultCode.SYSTEM_ERROR, "更新支付渠道失败");
            }
        } catch (Exception e) {
            log.error("更新支付渠道失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "更新支付渠道失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除支付渠道
     * @param channelId 渠道ID
     * @return 删除结果
     */
    @DeleteMapping("/payment-channels/{channelId}")
    public Result<Boolean> deletePaymentChannel(@PathVariable Integer channelId) {
        log.info("=== 管理员删除支付渠道 ===");
        log.info("请求参数: channelId={}", channelId);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            boolean deleted = adminService.deletePaymentChannel(channelId);
            if (deleted) {
                log.info("删除支付渠道成功");
                return Result.success(true);
            } else {
                return Result.failure(ResultCode.SYSTEM_ERROR, "删除支付渠道失败");
            }
        } catch (Exception e) {
            log.error("删除支付渠道失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "删除支付渠道失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新支付渠道状态
     * @param channelId 渠道ID
     * @param statusData 状态数据
     * @return 更新结果
     */
    @PutMapping("/payment-channels/{channelId}/status")
    public Result<Boolean> updatePaymentChannelStatus(@PathVariable Integer channelId, @RequestBody Map<String, Object> statusData) {
        log.info("=== 管理员更新支付渠道状态 ===");
        log.info("请求参数: channelId={}, status={}", channelId, statusData);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }
            
            if (!statusData.containsKey("isEnabled")) {
                return Result.failure(ResultCode.PARAM_ERROR, "状态参数不能为空");
            }
            
            Integer isEnabled = (Integer) statusData.get("isEnabled");
            boolean updated = adminService.updatePaymentChannelStatus(channelId, isEnabled);
            if (updated) {
                log.info("更新支付渠道状态成功");
                return Result.success(true);
            } else {
                return Result.failure(ResultCode.SYSTEM_ERROR, "更新支付渠道状态失败");
            }
        } catch (Exception e) {
            log.error("更新支付渠道状态失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "更新支付渠道状态失败: " + e.getMessage());
        }
    }

    // ==================== 举报管理接口 ====================
    
    @Autowired
    private org.example.afd.service.ReportService reportService;

    /**
     * 获取举报列表（管理员）
     */
    @GetMapping("/reports")
    public Result<List<org.example.afd.entity.Report>> getReports(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String type) {
        log.info("=== 管理员获取举报列表 ===");
        log.info("请求参数: status={}, type={}", status, type);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }

            List<org.example.afd.entity.Report> reports = reportService.getReports(status, type);
            log.info("获取举报列表成功: count={}", reports.size());
            return Result.success(reports);

        } catch (Exception e) {
            log.error("获取举报列表失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取举报列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取举报详情（管理员）
     */
    @GetMapping("/reports/{reportId}")
    public Result<org.example.afd.entity.Report> getReportDetail(@PathVariable Long reportId) {
        log.info("=== 管理员获取举报详情 ===");
        log.info("请求参数: reportId={}", reportId);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }

            org.example.afd.entity.Report report = reportService.getReportById(reportId);
            if (report == null) {
                return Result.failure(ResultCode.NOT_FOUND, "举报不存在");
            }

            log.info("获取举报详情成功");
            return Result.success(report);

        } catch (Exception e) {
            log.error("获取举报详情失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "获取举报详情失败: " + e.getMessage());
        }
    }

    /**
     * 处理举报（管理员）
     */
    @PostMapping("/reports/{reportId}/handle")
    public Result<Boolean> handleReport(
            @PathVariable Long reportId,
            @RequestBody Map<String, Object> handleData) {
        log.info("=== 管理员处理举报 ===");
        log.info("请求参数: reportId={}, data={}", reportId, handleData);
        
        try {
            if (!checkAdminPermission()) {
                return Result.failure(ResultCode.UNAUTHORIZED, "权限不足，需要管理员权限");
            }

            // 获取管理员ID
            Integer adminId = UserIdHolder.getUserId();
            if (adminId == null) {
                return Result.failure(ResultCode.UNAUTHORIZED, "管理员未登录");
            }

            // 获取处理数据
            Integer status = (Integer) handleData.get("status");
            String adminNote = (String) handleData.get("adminNote");

            // 验证参数
            if (status == null || (status != 1 && status != 2)) {
                return Result.failure(ResultCode.PARAM_ERROR, "无效的处理状态");
            }

            if (adminNote == null || adminNote.trim().isEmpty()) {
                return Result.failure(ResultCode.PARAM_ERROR, "处理备注不能为空");
            }

            // 处理举报
            boolean success = reportService.handleReport(reportId, status, adminId.longValue(), adminNote);
            
            if (success) {
                log.info("举报处理成功");
                return Result.success(true);
            } else {
                return Result.failure(ResultCode.SYSTEM_ERROR, "举报处理失败");
            }

        } catch (RuntimeException e) {
            log.error("举报处理失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, e.getMessage());
        } catch (Exception e) {
            log.error("举报处理失败", e);
            return Result.failure(ResultCode.SYSTEM_ERROR, "举报处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
