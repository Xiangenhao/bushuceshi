package org.example.afd.service;

import org.example.afd.dto.UnifiedAddressDTO;

import java.util.List;
import java.util.Map;

/**
 * 统一地址业务服务接口
 * 支持多国地址系统
 * 
 * @author system
 * @date 2025-01-03
 */
public interface UnifiedAddressService {

    /**
     * 获取用户的地址列表
     * 
     * @param userId 用户ID
     * @param countryCode 国家代码，null表示查询所有国家
     * @return 地址列表
     */
    List<UnifiedAddressDTO> getAddressList(Long userId, String countryCode);

    /**
     * 根据地址ID获取地址详情
     * 
     * @param addressId 地址ID
     * @param userId 用户ID
     * @return 地址详情
     */
    UnifiedAddressDTO getAddressById(Long addressId, Long userId);

    /**
     * 获取用户的默认地址
     * 
     * @param userId 用户ID
     * @param countryCode 国家代码
     * @return 默认地址
     */
    UnifiedAddressDTO getDefaultAddress(Long userId, String countryCode);

    /**
     * 添加新地址
     * 
     * @param address 地址信息
     * @return 新地址ID
     */
    Long addAddress(UnifiedAddressDTO address);

    /**
     * 更新地址信息
     * 
     * @param address 地址信息
     * @return 是否成功
     */
    boolean updateAddress(UnifiedAddressDTO address);

    /**
     * 删除地址
     * 
     * @param addressId 地址ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteAddress(Long addressId, Long userId);

    /**
     * 设置默认地址
     * 
     * @param addressId 地址ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean setDefaultAddress(Long addressId, Long userId);

    /**
     * 更新地址最后使用时间
     * 
     * @param addressId 地址ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean updateLastUsedTime(Long addressId, Long userId);

    /**
     * 获取支持的国家列表
     * 
     * @return 国家配置列表
     */
    List<Map<String, Object>> getSupportedCountries();

    /**
     * 验证地址信息是否完整
     * 
     * @param address 地址信息
     * @return 验证结果
     */
    boolean validateAddress(UnifiedAddressDTO address);
} 