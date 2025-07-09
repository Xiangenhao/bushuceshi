package org.example.afd.controller;

import org.example.afd.dto.AddressDTO;
import org.example.afd.model.Result;
import org.example.afd.service.AddressService;
import org.example.afd.utils.UserIdHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户地址相关接口的Controller
 * 
 * @author system
 * @date 2024-12-28
 */
@RestController
@RequestMapping("/api/v1")
public class AddressController {

    private static final Logger logger = LoggerFactory.getLogger(AddressController.class);

    @Autowired
    private AddressService addressService;

    /**
     * 获取当前用户的地址列表
     * @return 地址列表
     */
    @GetMapping("/addresses")
    public Result<List<AddressDTO>> getAddressList() {
        logger.info("开始获取用户地址列表");
        try {
            // 从UserIdHolder获取用户ID
            Integer userId = UserIdHolder.getUserId();
            logger.info("获取到用户ID: {}", userId);
            
            if (userId == null) {
                logger.warn("用户ID为空，用户未登录");
                return Result.error("用户未登录");
            }
            
            List<AddressDTO> addresses = addressService.getAddressList(userId.longValue());
            logger.info("成功获取到{}个地址", addresses != null ? addresses.size() : 0);
            return Result.success(addresses);
        } catch (Exception e) {
            logger.error("获取地址列表失败", e);
            return Result.error("获取地址列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取地址详情
     * @param addressId 地址ID
     * @param request HTTP请求对象，用于获取用户ID
     * @return 地址详情
     */
    @GetMapping("/addresses/{addressId}")
    public Result<AddressDTO> getAddress(@PathVariable Long addressId, HttpServletRequest request) {
        try {
            // 从请求头获取用户ID
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            AddressDTO address = addressService.getAddress(userId, addressId);
            if (address == null) {
                return Result.error("地址不存在");
            }
            return Result.success(address);
        } catch (Exception e) {
            return Result.error("获取地址详情失败");
        }
    }

    /**
     * 获取默认地址
     * @return 默认地址
     */
    @GetMapping("/addresses/default")
    public Result<AddressDTO> getDefaultAddress() {
        logger.info("开始获取用户默认地址");
        try {
            // 从UserIdHolder获取用户ID
            Integer userId = UserIdHolder.getUserId();
            logger.info("获取到用户ID: {}", userId);
            
            if (userId == null) {
                logger.warn("用户ID为空，用户未登录");
                return Result.error("用户未登录");
            }
            
            AddressDTO address = addressService.getDefaultAddress(userId.longValue());
            logger.info("成功获取默认地址: {}", address != null ? "存在默认地址" : "无默认地址");
            return Result.success(address);
        } catch (Exception e) {
            logger.error("获取默认地址失败", e);
            return Result.error("获取默认地址失败: " + e.getMessage());
        }
    }

    /**
     * 新增地址
     * @param address 地址信息
     * @return 操作结果
     */
    @PostMapping("/addresses")
    public Result<Long> addAddress(@RequestBody AddressDTO address) {
        logger.info("开始添加新地址");
        try {
            // 从UserIdHolder获取用户ID
            Integer userId = UserIdHolder.getUserId();
            logger.info("获取到用户ID: {}", userId);
            
            if (userId == null) {
                logger.warn("用户ID为空，用户未登录");
                return Result.error("用户未登录");
            }
            
            // 验证必要字段
            if (address.getReceiverName() == null || address.getReceiverName().trim().isEmpty()) {
                logger.warn("收货人姓名为空");
                return Result.error("收货人姓名不能为空");
            }
            if (address.getReceiverPhone() == null || address.getReceiverPhone().trim().isEmpty()) {
                logger.warn("收货人电话为空");
                return Result.error("收货人电话不能为空");
            }
            
            logger.info("地址信息验证通过，收货人: {}, 电话: {}", address.getReceiverName(), address.getReceiverPhone());
            
            // 自动生成地址行
            try {
                address.generateAddressLines();
                logger.info("已生成地址行");
            } catch (Exception ex) {
                logger.warn("生成地址行失败，继续处理: {}", ex.getMessage());
            }
            
            Long addressId = addressService.addAddress(userId.longValue(), address);
            logger.info("成功添加地址，地址ID: {}", addressId);
            return Result.success("添加地址成功", addressId);
        } catch (Exception e) {
            logger.error("添加地址失败", e);
            return Result.error("添加地址失败: " + e.getMessage());
        }
    }

    /**
     * 更新地址
     * @param addressId 地址ID
     * @param address 地址信息
     * @param request HTTP请求对象，用于获取用户ID
     * @return 操作结果
     */
    @PutMapping("/addresses/{addressId}")
    public Result<Boolean> updateAddress(@PathVariable Long addressId, @RequestBody AddressDTO address, HttpServletRequest request) {
        try {
            // 从请求头获取用户ID
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            // 验证必要字段
            if (address.getReceiverName() == null || address.getReceiverName().trim().isEmpty()) {
                return Result.error("收货人姓名不能为空");
            }
            if (address.getReceiverPhone() == null || address.getReceiverPhone().trim().isEmpty()) {
                return Result.error("收货人电话不能为空");
            }
            
            // 自动生成地址行
            address.generateAddressLines();
            
            boolean success = addressService.updateAddress(userId, addressId, address);
            if (success) {
                return Result.success("更新地址成功", true);
            } else {
                return Result.error("更新地址失败");
            }
        } catch (Exception e) {
            return Result.error("更新地址失败");
        }
    }

    /**
     * 删除地址
     * @param addressId 地址ID
     * @param request HTTP请求对象，用于获取用户ID
     * @return 操作结果
     */
    @DeleteMapping("/addresses/{addressId}")
    public Result<Boolean> deleteAddress(@PathVariable Long addressId, HttpServletRequest request) {
        try {
            // 从请求头获取用户ID
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            boolean success = addressService.deleteAddress(userId, addressId);
            if (success) {
                return Result.success("删除地址成功", true);
            } else {
                return Result.error("删除地址失败");
            }
        } catch (Exception e) {
            return Result.error("删除地址失败");
        }
    }

    /**
     * 设为默认地址
     * @param addressId 地址ID
     * @param request HTTP请求对象，用于获取用户ID
     * @return 操作结果
     */
    @PutMapping("/addresses/{addressId}/default")
    public Result<Boolean> setDefaultAddress(@PathVariable Long addressId, HttpServletRequest request) {
        try {
            // 从请求头获取用户ID
            Long userId = getUserIdFromRequest(request);
            if (userId == null) {
                return Result.error("用户未登录");
            }
            
            boolean success = addressService.setDefaultAddress(userId, addressId);
            if (success) {
                return Result.success("设置默认地址成功", true);
            } else {
                return Result.error("设置默认地址失败");
            }
        } catch (Exception e) {
            return Result.error("设置默认地址失败");
        }
    }

    /**
     * 从请求头中获取用户ID
     * @param request HTTP请求对象
     * @return 用户ID，如果获取失败返回null
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        try {
            String userIdStr = request.getHeader("userId");
            if (userIdStr != null && !userIdStr.isEmpty()) {
                return Long.parseLong(userIdStr);
            }
        } catch (NumberFormatException e) {
            // 用户ID格式错误
        }
        return null;
    }
} 