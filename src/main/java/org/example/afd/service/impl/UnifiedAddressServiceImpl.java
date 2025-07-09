package org.example.afd.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.afd.dto.UnifiedAddressDTO;
import org.example.afd.mapper.UnifiedAddressMapper;
import org.example.afd.service.UnifiedAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一地址业务服务实现类
 * 支持多国地址系统
 */
@Slf4j
@Service
public class UnifiedAddressServiceImpl implements UnifiedAddressService {

    @Autowired
    private UnifiedAddressMapper addressMapper;

    @Override
    public List<UnifiedAddressDTO> getAddressList(Long userId, String countryCode) {
        try {
            log.info("获取用户地址列表: userId={}, countryCode={}", userId, countryCode);
            
            if (userId == null) {
                log.warn("用户ID为空");
                return new ArrayList<>();
            }
            
            List<Map<String, Object>> addressMaps = addressMapper.selectAddressList(userId, countryCode);
            List<UnifiedAddressDTO> addresses = convertMapListToDTO(addressMaps);
            log.info("获取到{}条地址记录", addresses != null ? addresses.size() : 0);
            
            return addresses != null ? addresses : new ArrayList<>();
        } catch (Exception e) {
            log.error("获取用户地址列表失败: userId={}, countryCode={}", userId, countryCode, e);
            return new ArrayList<>();
        }
    }

    @Override
    public UnifiedAddressDTO getAddressById(Long addressId, Long userId) {
        try {
            log.info("获取地址详情: addressId={}, userId={}", addressId, userId);
            
            if (addressId == null || userId == null) {
                log.warn("地址ID或用户ID为空");
                return null;
            }
            
            Map<String, Object> addressMap = addressMapper.selectAddressById(addressId, userId);
            UnifiedAddressDTO address = convertMapToDTO(addressMap);
            log.info("获取地址详情结果: {}", address != null ? "成功" : "未找到");
            
            return address;
        } catch (Exception e) {
            log.error("获取地址详情失败: addressId={}, userId={}", addressId, userId, e);
            return null;
        }
    }

    @Override
    public UnifiedAddressDTO getDefaultAddress(Long userId, String countryCode) {
        try {
            log.info("获取默认地址: userId={}, countryCode={}", userId, countryCode);
            
            if (userId == null) {
                log.warn("用户ID为空");
                return null;
            }
            
            Map<String, Object> addressMap = addressMapper.selectDefaultAddress(userId, countryCode);
            UnifiedAddressDTO defaultAddress = convertMapToDTO(addressMap);
            log.info("获取默认地址结果: {}", defaultAddress != null ? "成功" : "未找到");
            
            return defaultAddress;
        } catch (Exception e) {
            log.error("获取默认地址失败: userId={}, countryCode={}", userId, countryCode, e);
            return null;
        }
    }

    @Override
    @Transactional
    public Long addAddress(UnifiedAddressDTO address) {
        try {
            log.info("添加新地址: {}", address);
            
            if (address == null || address.getUserId() == null) {
                log.warn("地址信息或用户ID为空");
                return null;
            }
            
            // 验证地址信息
            if (!validateAddress(address)) {
                log.warn("地址信息验证失败");
                return null;
            }
            
            // 设置创建时间
            address.setCreateTime(LocalDateTime.now());
            address.setUpdateTime(LocalDateTime.now());
            address.setLastUsedTime(LocalDateTime.now());
            
            // 如果是第一个地址，自动设为默认
            List<UnifiedAddressDTO> existingAddresses = getAddressList(address.getUserId(), address.getCountryCode());
            if (existingAddresses.isEmpty()) {
                address.setIsDefault(true);
            }
            
            // 如果设置为默认地址，需要先取消其他默认地址
            if (Boolean.TRUE.equals(address.getIsDefault())) {
                addressMapper.resetDefaultAddress(address.getUserId(), address.getCountryCode());
            }
            
            // 插入地址主表记录
            int result = addressMapper.insertAddress(address);
            if (result > 0 && address.getAddressId() != null) {
                // 如果是日本地址，插入日本地址详情
                if ("JP".equals(address.getCountryCode())) {
                    addressMapper.insertJapanAddress(address);
                }
                
                log.info("添加地址成功: addressId={}", address.getAddressId());
                return address.getAddressId();
            } else {
                log.warn("添加地址失败");
                return null;
            }
        } catch (Exception e) {
            log.error("添加地址失败", e);
            throw new RuntimeException("添加地址失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean updateAddress(UnifiedAddressDTO address) {
        try {
            log.info("更新地址: {}", address);
            
            if (address == null || address.getAddressId() == null || address.getUserId() == null) {
                log.warn("地址信息、地址ID或用户ID为空");
                return false;
            }
            
            // 验证地址信息
            if (!validateAddress(address)) {
                log.warn("地址信息验证失败");
                return false;
            }
            
            // 检查地址是否存在且属于当前用户
            UnifiedAddressDTO existingAddress = getAddressById(address.getAddressId(), address.getUserId());
            if (existingAddress == null) {
                log.warn("地址不存在或无权限修改");
                return false;
            }
            
            // 设置更新时间
            address.setUpdateTime(LocalDateTime.now());
            address.setLastUsedTime(LocalDateTime.now());
            
            // 如果设置为默认地址，需要先取消其他默认地址
            if (Boolean.TRUE.equals(address.getIsDefault())) {
                addressMapper.resetDefaultAddress(address.getUserId(), address.getCountryCode());
            }
            
            // 更新地址主表记录
            int result = addressMapper.updateAddress(address);
            if (result > 0) {
                // 如果是日本地址，更新日本地址详情
                if ("JP".equals(address.getCountryCode())) {
                    addressMapper.updateJapanAddress(address);
                }
                
                log.info("更新地址成功: addressId={}", address.getAddressId());
                return true;
            } else {
                log.warn("更新地址失败");
                return false;
            }
        } catch (Exception e) {
            log.error("更新地址失败", e);
            throw new RuntimeException("更新地址失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean deleteAddress(Long addressId, Long userId) {
        try {
            log.info("删除地址: addressId={}, userId={}", addressId, userId);
            
            if (addressId == null || userId == null) {
                log.warn("地址ID或用户ID为空");
                return false;
            }
            
            // 检查地址是否存在且属于当前用户
            UnifiedAddressDTO existingAddress = getAddressById(addressId, userId);
            if (existingAddress == null) {
                log.warn("地址不存在或无权限删除");
                return false;
            }
            
            // 删除地址记录（级联删除详情表）
            int result = addressMapper.deleteAddress(addressId, userId);
            if (result > 0) {
                log.info("删除地址成功: addressId={}", addressId);
                
                // 如果删除的是默认地址，需要设置新的默认地址
                if (Boolean.TRUE.equals(existingAddress.getIsDefault())) {
                    List<UnifiedAddressDTO> remainingAddresses = getAddressList(userId, existingAddress.getCountryCode());
                    if (!remainingAddresses.isEmpty()) {
                        setDefaultAddress(remainingAddresses.get(0).getAddressId(), userId);
                    }
                }
                
                return true;
            } else {
                log.warn("删除地址失败");
                return false;
            }
        } catch (Exception e) {
            log.error("删除地址失败", e);
            throw new RuntimeException("删除地址失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean setDefaultAddress(Long addressId, Long userId) {
        try {
            log.info("设置默认地址: addressId={}, userId={}", addressId, userId);
            
            if (addressId == null || userId == null) {
                log.warn("地址ID或用户ID为空");
                return false;
            }
            
            // 检查地址是否存在且属于当前用户
            UnifiedAddressDTO existingAddress = getAddressById(addressId, userId);
            if (existingAddress == null) {
                log.warn("地址不存在或无权限设置");
                return false;
            }
            
            // 先取消该用户在该国家的所有默认地址
            addressMapper.resetDefaultAddress(userId, existingAddress.getCountryCode());
            
            // 设置新的默认地址
            int result = addressMapper.setDefaultAddress(addressId, userId);
            if (result > 0) {
                log.info("设置默认地址成功: addressId={}", addressId);
                return true;
            } else {
                log.warn("设置默认地址失败");
                return false;
            }
        } catch (Exception e) {
            log.error("设置默认地址失败", e);
            throw new RuntimeException("设置默认地址失败: " + e.getMessage());
        }
    }

    @Override
    public boolean updateLastUsedTime(Long addressId, Long userId) {
        try {
            log.info("更新地址最后使用时间: addressId={}, userId={}", addressId, userId);
            
            if (addressId == null || userId == null) {
                log.warn("地址ID或用户ID为空");
                return false;
            }
            
            int result = addressMapper.updateLastUsedTime(addressId, userId);
            if (result > 0) {
                log.info("更新地址最后使用时间成功: addressId={}", addressId);
                return true;
            } else {
                log.warn("更新地址最后使用时间失败");
                return false;
            }
        } catch (Exception e) {
            log.error("更新地址最后使用时间失败", e);
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getSupportedCountries() {
        try {
            log.info("获取支持的国家列表");
            
            List<Map<String, Object>> countries = new ArrayList<>();
            
            // 日本
            Map<String, Object> japan = new HashMap<>();
            japan.put("code", "JP");
            japan.put("name", "日本");
            japan.put("nameEn", "Japan");
            japan.put("enabled", true);
            countries.add(japan);
            
            // 中国（预留）
            Map<String, Object> china = new HashMap<>();
            china.put("code", "CN");
            china.put("name", "中国");
            china.put("nameEn", "China");
            china.put("enabled", false);
            countries.add(china);
            
            log.info("获取支持的国家列表成功，共{}个国家", countries.size());
            return countries;
        } catch (Exception e) {
            log.error("获取支持的国家列表失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean validateAddress(UnifiedAddressDTO address) {
        try {
            if (address == null) {
                log.warn("地址对象为空");
                return false;
            }
            
            // 检查必填字段
            if (!StringUtils.hasText(address.getCountryCode())) {
                log.warn("国家代码为空");
                return false;
            }
            
            if (!StringUtils.hasText(address.getReceiverName())) {
                log.warn("收件人姓名为空");
                return false;
            }
            
            if (!StringUtils.hasText(address.getReceiverPhone())) {
                log.warn("收件人电话为空");
                return false;
            }
            
            // 根据国家代码验证特定字段
            if ("JP".equals(address.getCountryCode())) {
                return validateJapanAddress(address);
            } else if ("CN".equals(address.getCountryCode())) {
                return validateChinaAddress(address);
            }
            
            log.warn("不支持的国家代码: {}", address.getCountryCode());
            return false;
        } catch (Exception e) {
            log.error("验证地址失败", e);
            return false;
        }
    }
    
    /**
     * 验证日本地址
     */
    private boolean validateJapanAddress(UnifiedAddressDTO address) {
        // 检查日本地址必填字段
        if (!StringUtils.hasText(address.getPrefecture())) {
            log.warn("都道府县为空");
            return false;
        }
        
        if (!StringUtils.hasText(address.getMunicipality())) {
            log.warn("市区町村为空");
            return false;
        }
        
        if (!StringUtils.hasText(address.getPostalCode())) {
            log.warn("邮政编码为空");
            return false;
        }
        
        // 验证日本邮政编码格式 (XXX-XXXX)
        if (!address.getPostalCode().matches("\\d{3}-\\d{4}")) {
            log.warn("日本邮政编码格式不正确: {}", address.getPostalCode());
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证中国地址
     */
    private boolean validateChinaAddress(UnifiedAddressDTO address) {
        // 检查中国地址必填字段
        if (!StringUtils.hasText(address.getProvince())) {
            log.warn("省份为空");
            return false;
        }
        
        if (!StringUtils.hasText(address.getCity())) {
            log.warn("城市为空");
            return false;
        }
        
        if (!StringUtils.hasText(address.getDistrict())) {
            log.warn("区县为空");
            return false;
        }
        
        if (!StringUtils.hasText(address.getDetailAddress())) {
            log.warn("详细地址为空");
            return false;
        }
        
        return true;
    }
    
    /**
     * 将Map列表转换为DTO列表
     */
    private List<UnifiedAddressDTO> convertMapListToDTO(List<Map<String, Object>> mapList) {
        if (mapList == null || mapList.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<UnifiedAddressDTO> dtoList = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            UnifiedAddressDTO dto = convertMapToDTO(map);
            if (dto != null) {
                dtoList.add(dto);
            }
        }
        
        return dtoList;
    }
    
    /**
     * 将Map转换为DTO
     */
    private UnifiedAddressDTO convertMapToDTO(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        
        UnifiedAddressDTO dto = new UnifiedAddressDTO();
        
        // 主表字段
        dto.setAddressId(getLongValue(map, "address_id"));
        dto.setUserId(getLongValue(map, "user_id"));
        dto.setCountryCode(getStringValue(map, "country_code"));
        dto.setReceiverName(getStringValue(map, "receiver_name"));
        dto.setReceiverPhone(getStringValue(map, "receiver_phone"));
        dto.setIsDefault(getBooleanValue(map, "is_default"));
        dto.setAddressType(getIntegerValue(map, "address_type"));
        dto.setDeliveryInstructions(getStringValue(map, "delivery_instructions"));
        
        // 日本地址字段
        if ("JP".equals(dto.getCountryCode())) {
            dto.setPostalCode(getStringValue(map, "postal_code"));
            dto.setPrefecture(getStringValue(map, "prefecture"));
            dto.setMunicipality(getStringValue(map, "municipality"));
            dto.setTown(getStringValue(map, "town"));
            dto.setChome(getStringValue(map, "chome"));
            dto.setBanchi(getStringValue(map, "banchi"));
            dto.setBuilding(getStringValue(map, "building"));
            dto.setRoomNumber(getStringValue(map, "room_number"));
            dto.setAddressLine1(getStringValue(map, "address_line1"));
            dto.setAddressLine2(getStringValue(map, "address_line2"));
        }
        
        return dto;
    }
    
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
    
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Boolean getBooleanValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value) == 1;
        }
        try {
            return "1".equals(value.toString()) || "true".equalsIgnoreCase(value.toString());
        } catch (Exception e) {
            return false;
        }
    }
} 