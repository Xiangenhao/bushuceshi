package org.example.afd.service;

import org.example.afd.dto.AddressDTO;
import java.util.List;
import java.util.Map;

/**
 * 用户地址服务接口
 */
public interface AddressService {

    /**
     * 获取用户的地址列表
     * @param userId 用户ID
     * @return 地址列表
     */
    List<AddressDTO> getAddressList(Long userId);
    
    /**
     * 获取地址详情
     * @param userId 用户ID
     * @param addressId 地址ID
     * @return 地址详情
     */
    AddressDTO getAddress(Long userId, Long addressId);
    
    /**
     * 获取用户的默认地址
     * @param userId 用户ID
     * @return 默认地址
     */
    AddressDTO getDefaultAddress(Long userId);
    
    /**
     * 新增地址
     * @param userId 用户ID
     * @param address 地址信息
     * @return 新增地址的ID
     */
    Long addAddress(Long userId, AddressDTO address);
    
    /**
     * 更新地址
     * @param userId 用户ID
     * @param addressId 地址ID
     * @param address 地址信息
     * @return 操作是否成功
     */
    boolean updateAddress(Long userId, Long addressId, AddressDTO address);
    
    /**
     * 删除地址
     * @param userId 用户ID
     * @param addressId 地址ID
     * @return 操作是否成功
     */
    boolean deleteAddress(Long userId, Long addressId);
    
    /**
     * 设为默认地址
     * @param userId 用户ID
     * @param addressId 地址ID
     * @return 操作是否成功
     */
    boolean setDefaultAddress(Long userId, Long addressId);
} 