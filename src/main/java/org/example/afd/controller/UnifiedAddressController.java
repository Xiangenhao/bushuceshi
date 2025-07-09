package org.example.afd.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.dto.UnifiedAddressDTO;
import org.example.afd.model.Result;
import org.example.afd.service.UnifiedAddressService;
import org.example.afd.utils.UserIdHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 统一地址管理控制器
 * 支持多国地址格式，目前主要支持日本地址
 */
@RestController
@RequestMapping("/api/v1/address")
@Slf4j
public class UnifiedAddressController {

    @Autowired
    private UnifiedAddressService addressService;

    /**
     * 获取用户地址列表
     */
    @GetMapping
    public ResponseEntity<Result<List<UnifiedAddressDTO>>> getAddressList(
            @RequestParam(defaultValue = "JP") String countryCode) {
        try {
            Integer userIdInt = UserIdHolder.getUserId();
            Long userId = userIdInt != null ? userIdInt.longValue() : null;
            log.info("获取用户地址列表: userId={}, countryCode={}", userId, countryCode);
            
            List<UnifiedAddressDTO> addresses = addressService.getAddressList(userId, countryCode);
            log.info("获取地址列表成功，共{}条", addresses.size());
            
            return ResponseEntity.ok(Result.success("获取地址列表成功", addresses));
        } catch (Exception e) {
            log.error("获取地址列表失败", e);
            return ResponseEntity.ok(Result.error("获取地址列表失败: " + e.getMessage()));
        }
    }

    /**
     * 根据ID获取地址详情
     */
    @GetMapping("/{addressId}")
    public ResponseEntity<Result<UnifiedAddressDTO>> getAddressById(@PathVariable Long addressId) {
        try {
            Integer userIdInt = UserIdHolder.getUserId();
            Long userId = userIdInt != null ? userIdInt.longValue() : null;
            log.info("获取地址详情: userId={}, addressId={}", userId, addressId);
            
            UnifiedAddressDTO address = addressService.getAddressById(addressId, userId);
            if (address == null) {
                return ResponseEntity.ok(Result.error("地址不存在或无权限访问"));
            }
            
            log.info("获取地址详情成功: {}", address);
            return ResponseEntity.ok(Result.success("获取地址详情成功", address));
        } catch (Exception e) {
            log.error("获取地址详情失败", e);
            return ResponseEntity.ok(Result.error("获取地址详情失败: " + e.getMessage()));
        }
    }

    /**
     * 创建新地址
     */
    @PostMapping
    public ResponseEntity<Result<UnifiedAddressDTO>> createAddress(@RequestBody UnifiedAddressDTO addressDTO) {
        try {
            Integer userIdInt = UserIdHolder.getUserId();
            Long userId = userIdInt != null ? userIdInt.longValue() : null;
            log.info("创建新地址: userId={}, address={}", userId, addressDTO);
            
            // 设置用户ID
            addressDTO.setUserId(userId);
            
            // 验证地址数据
            if (!addressService.validateAddress(addressDTO)) {
                return ResponseEntity.ok(Result.error("地址信息不完整，请检查必填字段"));
            }
            
            Long addressId = addressService.addAddress(addressDTO);
            if (addressId == null) {
                return ResponseEntity.ok(Result.error("创建地址失败"));
            }
            
            // 获取创建的地址详情
            UnifiedAddressDTO createdAddress = addressService.getAddressById(addressId, userId);
            log.info("创建地址成功: {}", createdAddress);
            
            return ResponseEntity.ok(Result.success("创建地址成功", createdAddress));
        } catch (Exception e) {
            log.error("创建地址失败", e);
            return ResponseEntity.ok(Result.error("创建地址失败: " + e.getMessage()));
        }
    }

    /**
     * 更新地址
     */
    @PutMapping("/{addressId}")
    public ResponseEntity<Result<UnifiedAddressDTO>> updateAddress(
            @PathVariable Long addressId, 
            @RequestBody UnifiedAddressDTO addressDTO) {
        try {
            Integer userIdInt = UserIdHolder.getUserId();
            Long userId = userIdInt != null ? userIdInt.longValue() : null;
            log.info("更新地址: userId={}, addressId={}, address={}", userId, addressId, addressDTO);
            
            // 设置地址ID和用户ID
            addressDTO.setAddressId(addressId);
            addressDTO.setUserId(userId);
            
            // 验证地址数据
            if (!addressService.validateAddress(addressDTO)) {
                return ResponseEntity.ok(Result.error("地址信息不完整，请检查必填字段"));
            }
            
            boolean success = addressService.updateAddress(addressDTO);
            if (!success) {
                return ResponseEntity.ok(Result.error("地址不存在或无权限修改"));
            }
            
            // 获取更新后的地址详情
            UnifiedAddressDTO updatedAddress = addressService.getAddressById(addressId, userId);
            log.info("更新地址成功: {}", updatedAddress);
            return ResponseEntity.ok(Result.success("更新地址成功", updatedAddress));
        } catch (Exception e) {
            log.error("更新地址失败", e);
            return ResponseEntity.ok(Result.error("更新地址失败: " + e.getMessage()));
        }
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Result<Object>> deleteAddress(@PathVariable Long addressId) {
        try {
            Integer userIdInt = UserIdHolder.getUserId();
            Long userId = userIdInt != null ? userIdInt.longValue() : null;
            log.info("删除地址: userId={}, addressId={}", userId, addressId);
            
            boolean deleted = addressService.deleteAddress(addressId, userId);
            if (!deleted) {
                return ResponseEntity.ok(Result.error("地址不存在或无权限删除"));
            }
            
            log.info("删除地址成功: addressId={}", addressId);
            return ResponseEntity.ok(Result.success("删除地址成功"));
        } catch (Exception e) {
            log.error("删除地址失败", e);
            return ResponseEntity.ok(Result.error("删除地址失败: " + e.getMessage()));
        }
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/{addressId}/default")
    public ResponseEntity<Result<Object>> setDefaultAddress(@PathVariable Long addressId) {
        try {
            Integer userIdInt = UserIdHolder.getUserId();
            Long userId = userIdInt != null ? userIdInt.longValue() : null;
            log.info("设置默认地址: userId={}, addressId={}", userId, addressId);
            
            boolean success = addressService.setDefaultAddress(addressId, userId);
            if (!success) {
                return ResponseEntity.ok(Result.error("地址不存在或无权限设置"));
            }
            
            log.info("设置默认地址成功: addressId={}", addressId);
            return ResponseEntity.ok(Result.success("设置默认地址成功"));
        } catch (Exception e) {
            log.error("设置默认地址失败", e);
            return ResponseEntity.ok(Result.error("设置默认地址失败: " + e.getMessage()));
        }
    }

    /**
     * 获取默认地址
     */
    @GetMapping("/default")
    public ResponseEntity<Result<UnifiedAddressDTO>> getDefaultAddress(
            @RequestParam(defaultValue = "JP") String countryCode) {
        try {
            Integer userIdInt = UserIdHolder.getUserId();
            Long userId = userIdInt != null ? userIdInt.longValue() : null;
            log.info("获取默认地址: userId={}, countryCode={}", userId, countryCode);
            
            UnifiedAddressDTO defaultAddress = addressService.getDefaultAddress(userId, countryCode);
            if (defaultAddress == null) {
                return ResponseEntity.ok(Result.error("未设置默认地址"));
            }
            
            log.info("获取默认地址成功: {}", defaultAddress);
            return ResponseEntity.ok(Result.success("获取默认地址成功", defaultAddress));
        } catch (Exception e) {
            log.error("获取默认地址失败", e);
            return ResponseEntity.ok(Result.error("获取默认地址失败: " + e.getMessage()));
        }
    }

    /**
     * 验证地址
     */
    @PostMapping("/validate")
    public ResponseEntity<Result<Object>> validateAddress(@RequestBody UnifiedAddressDTO addressDTO) {
        try {
            log.info("验证地址: {}", addressDTO);
            
            if (!addressService.validateAddress(addressDTO)) {
                return ResponseEntity.ok(Result.error("地址信息不完整，请检查必填字段"));
            }
            
            log.info("地址验证通过");
            return ResponseEntity.ok(Result.success("地址验证通过"));
        } catch (Exception e) {
            log.error("地址验证失败", e);
            return ResponseEntity.ok(Result.error("地址验证失败: " + e.getMessage()));
        }
    }

    /**
     * 测试API - 不依赖数据库
     */
    @GetMapping("/test")
    public ResponseEntity<Result<String>> test() {
        try {
            Integer userIdInt = UserIdHolder.getUserId();
            Long userId = userIdInt != null ? userIdInt.longValue() : null;
            log.info("测试API调用，用户ID: {}", userId);
            
            String message = String.format("地址服务正常工作，当前用户ID: %s", userId);
            return ResponseEntity.ok(Result.success("地址服务正常工作", message));
        } catch (Exception e) {
            log.error("测试API失败", e);
            return ResponseEntity.ok(Result.error("测试API失败: " + e.getMessage()));
        }
    }

    /**
     * 获取支持的国家列表
     */
    @GetMapping("/countries")
    public ResponseEntity<Result<List<Map<String, Object>>>> getSupportedCountries() {
        try {
            log.info("获取支持的国家列表");
            
            List<Map<String, Object>> countries = addressService.getSupportedCountries();
            log.info("获取支持的国家列表成功，共{}个国家", countries.size());
            
            return ResponseEntity.ok(Result.success("获取支持的国家列表成功", countries));
        } catch (Exception e) {
            log.error("获取支持的国家列表失败", e);
            return ResponseEntity.ok(Result.error("获取支持的国家列表失败: " + e.getMessage()));
        }
    }
} 