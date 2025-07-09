package org.example.afd.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 用户地址数据访问接口
 */
@Mapper
public interface AddressMapper {
    
    /**
     * 查询用户地址列表（排除软删除的地址）
     * @param userId 用户ID
     * @return 地址列表
     */
    @Select("SELECT ua.*, uaj.postal_code, uaj.prefecture, uaj.municipality, uaj.town, " +
            "uaj.chome, uaj.banchi, uaj.building, uaj.room_number, uaj.address_line1, uaj.address_line2 " +
            "FROM user_address ua " +
            "LEFT JOIN user_address_japan uaj ON ua.address_id = uaj.address_id " +
            "WHERE ua.user_id = #{userId} AND ua.is_deleted = 0 " +
            "ORDER BY ua.is_default DESC, ua.create_time DESC")
    List<Map<String, Object>> selectAddressList(@Param("userId") Long userId);
    
    /**
     * 查询用户的默认地址（排除软删除的地址）
     * @param userId 用户ID
     * @return 默认地址
     */
    @Select("SELECT ua.*, uaj.postal_code, uaj.prefecture, uaj.municipality, uaj.town, " +
            "uaj.chome, uaj.banchi, uaj.building, uaj.room_number, uaj.address_line1, uaj.address_line2 " +
            "FROM user_address ua " +
            "LEFT JOIN user_address_japan uaj ON ua.address_id = uaj.address_id " +
            "WHERE ua.user_id = #{userId} AND ua.is_default = 1 AND ua.is_deleted = 0 LIMIT 1")
    Map<String, Object> selectDefaultAddress(@Param("userId") Long userId);
    
    /**
     * 根据ID查询地址（包含软删除的地址，用于订单详情显示）
     * @param addressId 地址ID
     * @param userId 用户ID
     * @return 地址信息
     */
    @Select("SELECT ua.*, uaj.postal_code, uaj.prefecture, uaj.municipality, uaj.town, " +
            "uaj.chome, uaj.banchi, uaj.building, uaj.room_number, uaj.address_line1, uaj.address_line2 " +
            "FROM user_address ua " +
            "LEFT JOIN user_address_japan uaj ON ua.address_id = uaj.address_id " +
            "WHERE ua.address_id = #{addressId} AND ua.user_id = #{userId}")
    Map<String, Object> selectAddressById(@Param("addressId") Long addressId, @Param("userId") Long userId);
    
    /**
     * 新增地址（使用统一地址系统）
     * 注意：此方法已废弃，请使用 UnifiedAddressMapper.insertAddress()
     * @param address 地址信息
     * @return 影响行数
     */
    @Deprecated
    default int insertAddress(Map<String, Object> address) {
        throw new UnsupportedOperationException("请使用 UnifiedAddressMapper.insertAddress() 方法");
    }
    
    /**
     * 更新地址（使用统一地址系统）
     * 注意：此方法已废弃，请使用 UnifiedAddressMapper.updateAddress()
     * @param address 地址信息
     * @return 影响行数
     */
    @Deprecated
    default int updateAddress(Map<String, Object> address) {
        throw new UnsupportedOperationException("请使用 UnifiedAddressMapper.updateAddress() 方法");
    }
    
    /**
     * 软删除地址
     * @param addressId 地址ID
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE user_address SET is_deleted = 1 WHERE address_id = #{addressId} AND user_id = #{userId}")
    int deleteAddress(@Param("addressId") Long addressId, @Param("userId") Long userId);
    
    /**
     * 将用户所有地址设为非默认（排除软删除的地址）
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE user_address SET is_default = 0 WHERE user_id = #{userId} AND is_deleted = 0")
    int resetDefaultAddress(@Param("userId") Long userId);
    
    /**
     * 设置地址为默认地址（仅对未删除的地址生效）
     * @param addressId 地址ID
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE user_address SET is_default = 1 WHERE address_id = #{addressId} AND user_id = #{userId} AND is_deleted = 0")
    int setDefaultAddress(@Param("addressId") Long addressId, @Param("userId") Long userId);
} 