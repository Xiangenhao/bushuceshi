package org.example.afd.service.impl;

import org.example.afd.dto.AddressDTO;
import org.example.afd.dto.UnifiedAddressDTO;
import org.example.afd.mapper.AddressMapper;
import org.example.afd.mapper.UnifiedAddressMapper;
import org.example.afd.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户地址服务实现类
 */
@Service
public class AddressServiceImpl implements AddressService {

    private static final Logger logger = LoggerFactory.getLogger(AddressServiceImpl.class);

    @Autowired
    private AddressMapper addressMapper;
    
    @Autowired
    private UnifiedAddressMapper unifiedAddressMapper;

    @Override
    public List<AddressDTO> getAddressList(Long userId) {
        logger.info("开始查询用户地址列表，用户ID: {}", userId);
        try {
            List<Map<String, Object>> addresses = addressMapper.selectAddressList(userId);
            logger.info("数据库查询完成，查询到{}条地址记录", addresses != null ? addresses.size() : 0);
            
            List<AddressDTO> addressDTOs = new ArrayList<>();
            
            if (addresses != null && !addresses.isEmpty()) {
                for (Map<String, Object> address : addresses) {
                    AddressDTO dto = convertToAddressDTO(address);
                    addressDTOs.add(dto);
                    logger.debug("转换地址DTO: 地址ID={}, 收货人={}", dto.getAddressId(), dto.getReceiverName());
                }
            }
            
            logger.info("地址列表查询完成，返回{}条记录", addressDTOs.size());
            return addressDTOs;
        } catch (Exception e) {
            logger.error("查询用户地址列表失败，用户ID: {}", userId, e);
            throw e;
        }
    }

    @Override
    public AddressDTO getAddress(Long userId, Long addressId) {
        Map<String, Object> address = addressMapper.selectAddressById(addressId, userId);
        if (address == null) {
            return null;
        }
        return convertToAddressDTO(address);
    }

    @Override
    public AddressDTO getDefaultAddress(Long userId) {
        Map<String, Object> address = addressMapper.selectDefaultAddress(userId);
        if (address == null) {
            return null;
        }
        return convertToAddressDTO(address);
    }

    @Override
    @Transactional
    public Long addAddress(Long userId, AddressDTO address) {
        if (address == null) {
            throw new RuntimeException("地址信息不完整");
        }
        
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("userId", userId);
        addressMap.put("receiverName", address.getReceiverName());
        addressMap.put("receiverPhone", address.getReceiverPhone());
        addressMap.put("postalCode", address.getPostalCode());
        addressMap.put("prefecture", address.getPrefecture());
        addressMap.put("city", address.getCity());
        addressMap.put("town", address.getTown());
        addressMap.put("chome", address.getChome());
        addressMap.put("banchi", address.getBanchi());
        addressMap.put("building", address.getBuilding());
        addressMap.put("roomNumber", address.getRoomNumber());
        addressMap.put("addressLine1", address.getAddressLine1());
        addressMap.put("addressLine2", address.getAddressLine2());
        addressMap.put("isDefault", address.getIsDefault());
        addressMap.put("addressType", address.getAddressType());
        addressMap.put("deliveryInstructions", address.getDeliveryInstructions());
        
        // 如果是默认地址，先将其他地址设为非默认
        if (address.getIsDefault() != null && address.getIsDefault()) {
            addressMapper.resetDefaultAddress(userId);
        }
        
        addressMapper.insertAddress(addressMap);
        
        // 处理自增ID的类型转换
        Object addressIdObj = addressMap.get("addressId");
        if (addressIdObj instanceof Number) {
            return ((Number) addressIdObj).longValue();
        }
        return null;
    }

    @Override
    @Transactional
    public boolean updateAddress(Long userId, Long addressId, AddressDTO address) {
        if (addressId == null || address == null) {
            throw new RuntimeException("地址信息不完整");
        }
        
        // 检查地址是否存在
        Map<String, Object> existingAddress = addressMapper.selectAddressById(addressId, userId);
        if (existingAddress == null) {
            throw new RuntimeException("地址不存在");
        }
        
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("addressId", addressId);
        addressMap.put("userId", userId);
        addressMap.put("receiverName", address.getReceiverName());
        addressMap.put("receiverPhone", address.getReceiverPhone());
        addressMap.put("postalCode", address.getPostalCode());
        addressMap.put("prefecture", address.getPrefecture());
        addressMap.put("city", address.getCity());
        addressMap.put("town", address.getTown());
        addressMap.put("chome", address.getChome());
        addressMap.put("banchi", address.getBanchi());
        addressMap.put("building", address.getBuilding());
        addressMap.put("roomNumber", address.getRoomNumber());
        addressMap.put("addressLine1", address.getAddressLine1());
        addressMap.put("addressLine2", address.getAddressLine2());
        addressMap.put("isDefault", address.getIsDefault());
        addressMap.put("addressType", address.getAddressType());
        addressMap.put("deliveryInstructions", address.getDeliveryInstructions());
        
        // 如果是默认地址，先将其他地址设为非默认
        if (address.getIsDefault() != null && address.getIsDefault()) {
            addressMapper.resetDefaultAddress(userId);
        }
        
        int rows = addressMapper.updateAddress(addressMap);
        return rows > 0;
    }

    @Override
    @Transactional
    public boolean deleteAddress(Long userId, Long addressId) {
        if (addressId == null) {
            throw new RuntimeException("地址ID不能为空");
        }
        
        // 检查地址是否存在
        Map<String, Object> existingAddress = addressMapper.selectAddressById(addressId, userId);
        if (existingAddress == null) {
            throw new RuntimeException("地址不存在");
        }
        
        int rows = addressMapper.deleteAddress(addressId, userId);
        return rows > 0;
    }

    @Override
    @Transactional
    public boolean setDefaultAddress(Long userId, Long addressId) {
        if (addressId == null) {
            throw new RuntimeException("地址ID不能为空");
        }
        
        // 检查地址是否存在
        Map<String, Object> existingAddress = addressMapper.selectAddressById(addressId, userId);
        if (existingAddress == null) {
            throw new RuntimeException("地址不存在");
        }
        
        // 将其他地址设为非默认
        addressMapper.resetDefaultAddress(userId);
        
        // 设置当前地址为默认
        int rows = addressMapper.setDefaultAddress(addressId, userId);
        return rows > 0;
    }
    
    /**
     * 将数据库查询结果转换为地址DTO
     * @param address 数据库查询结果
     * @return 地址DTO
     */
    private AddressDTO convertToAddressDTO(Map<String, Object> address) {
        AddressDTO addressDTO = new AddressDTO();
        
        // 处理主表字段（user_address）
        Object addressIdObj = address.get("address_id");
        if (addressIdObj instanceof Number) {
            addressDTO.setAddressId(((Number) addressIdObj).longValue());
        }
        
        Object userIdObj = address.get("user_id");
        if (userIdObj instanceof Number) {
            addressDTO.setUserId(((Number) userIdObj).longValue());
        }
        
        addressDTO.setReceiverName((String) address.get("receiver_name"));
        addressDTO.setReceiverPhone((String) address.get("receiver_phone"));
        
        // 处理日本地址详情字段（user_address_japan）
        addressDTO.setPostalCode((String) address.get("postal_code"));
        addressDTO.setPrefecture((String) address.get("prefecture"));
        // 注意：新系统中是 municipality，不是 city
        String city = (String) address.get("municipality");
        if (city == null) {
            city = (String) address.get("city"); // 兼容旧字段名
        }
        addressDTO.setCity(city);
        
        addressDTO.setTown((String) address.get("town"));
        addressDTO.setChome((String) address.get("chome"));
        addressDTO.setBanchi((String) address.get("banchi"));
        addressDTO.setBuilding((String) address.get("building"));
        addressDTO.setRoomNumber((String) address.get("room_number"));
        addressDTO.setAddressLine1((String) address.get("address_line1"));
        addressDTO.setAddressLine2((String) address.get("address_line2"));
        
        // 处理is_default字段的类型转换（数据库返回Integer，需要转换为Boolean）
        Object isDefaultObj = address.get("is_default");
        if (isDefaultObj instanceof Integer) {
            addressDTO.setIsDefault(((Integer) isDefaultObj) == 1);
        } else if (isDefaultObj instanceof Boolean) {
            addressDTO.setIsDefault((Boolean) isDefaultObj);
        } else {
            addressDTO.setIsDefault(false);
        }
        
        Object addressTypeObj = address.get("address_type");
        if (addressTypeObj instanceof Number) {
            addressDTO.setAddressType(((Number) addressTypeObj).intValue());
        }
        
        addressDTO.setDeliveryInstructions((String) address.get("delivery_instructions"));
        
        return addressDTO;
    }
} 