package org.example.afd.mapper;

import org.apache.ibatis.annotations.*;
import org.example.afd.dto.UnifiedAddressDTO;

import java.util.List;
import java.util.Map;

/**
 * 统一地址数据访问映射器
 * 支持多国地址系统
 * 
 * @author system
 * @date 2025-01-03
 */
@Mapper
public interface UnifiedAddressMapper {

    /**
     * 查询用户的地址列表（支持按国家筛选）
     * 
     * @param userId 用户ID
     * @param countryCode 国家代码，null表示查询所有国家
     * @return 地址列表
     */
    @Select({
        "<script>",
        "SELECT ua.*, uaj.* FROM user_address ua ",
        "LEFT JOIN user_address_japan uaj ON ua.address_id = uaj.address_id ",
        "WHERE ua.user_id = #{userId} AND ua.is_deleted = 0 ",
        "<if test='countryCode != null'> AND ua.country_code = #{countryCode} </if>",
        "ORDER BY ua.is_default DESC, ua.last_used_time DESC, ua.create_time DESC",
        "</script>"
    })
    List<Map<String, Object>> selectAddressList(@Param("userId") Long userId, 
                                               @Param("countryCode") String countryCode);

    /**
     * 根据地址ID查询地址详情
     * 
     * @param addressId 地址ID
     * @param userId 用户ID
     * @return 地址详情
     */
    @Select({
        "SELECT ua.*, uaj.* FROM user_address ua ",
        "LEFT JOIN user_address_japan uaj ON ua.address_id = uaj.address_id ",
        "WHERE ua.address_id = #{addressId} AND ua.user_id = #{userId}"
    })
    Map<String, Object> selectAddressById(@Param("addressId") Long addressId, 
                                         @Param("userId") Long userId);

    /**
     * 查询用户的默认地址
     * 
     * @param userId 用户ID
     * @param countryCode 国家代码
     * @return 默认地址
     */
    @Select({
        "SELECT ua.*, uaj.* FROM user_address ua ",
        "LEFT JOIN user_address_japan uaj ON ua.address_id = uaj.address_id ",
        "WHERE ua.user_id = #{userId} AND ua.country_code = #{countryCode} ",
        "AND ua.is_default = 1 AND ua.is_deleted = 0 LIMIT 1"
    })
    Map<String, Object> selectDefaultAddress(@Param("userId") Long userId, 
                                           @Param("countryCode") String countryCode);

    /**
     * 插入地址主表记录
     * 
     * @param address 地址信息
     * @return 影响行数
     */
    @Insert({
        "INSERT INTO user_address (user_id, country_code, receiver_name, receiver_phone, ",
        "is_default, address_type, delivery_instructions, last_used_time) ",
        "VALUES (#{userId}, #{countryCode}, #{receiverName}, #{receiverPhone}, ",
        "#{isDefault}, #{addressType}, #{deliveryInstructions}, #{lastUsedTime})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "addressId")
    int insertAddress(UnifiedAddressDTO address);

    /**
     * 插入日本地址详情
     * 
     * @param address 地址信息
     * @return 影响行数
     */
    @Insert({
        "INSERT INTO user_address_japan (address_id, postal_code, prefecture, municipality, ",
        "town, chome, banchi, building, room_number, address_line1, address_line2) ",
        "VALUES (#{addressId}, #{postalCode}, #{prefecture}, #{municipality}, ",
        "#{town}, #{chome}, #{banchi}, #{building}, #{roomNumber}, #{addressLine1}, #{addressLine2})"
    })
    int insertJapanAddress(UnifiedAddressDTO address);

    /**
     * 更新地址主表信息
     * 
     * @param address 地址信息
     * @return 影响行数
     */
    @Update({
        "UPDATE user_address SET ",
        "receiver_name = #{receiverName}, receiver_phone = #{receiverPhone}, ",
        "is_default = #{isDefault}, address_type = #{addressType}, ",
        "delivery_instructions = #{deliveryInstructions}, ",
        "last_used_time = #{lastUsedTime} ",
        "WHERE address_id = #{addressId} AND user_id = #{userId}"
    })
    int updateAddress(UnifiedAddressDTO address);

    /**
     * 更新日本地址详情
     * 
     * @param address 地址信息
     * @return 影响行数
     */
    @Update({
        "UPDATE user_address_japan SET ",
        "postal_code = #{postalCode}, prefecture = #{prefecture}, ",
        "municipality = #{municipality}, town = #{town}, chome = #{chome}, ",
        "banchi = #{banchi}, building = #{building}, room_number = #{roomNumber}, ",
        "address_line1 = #{addressLine1}, address_line2 = #{addressLine2} ",
        "WHERE address_id = #{addressId}"
    })
    int updateJapanAddress(UnifiedAddressDTO address);

    /**
     * 软删除地址
     * 
     * @param addressId 地址ID
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE user_address SET is_deleted = 1 WHERE address_id = #{addressId} AND user_id = #{userId}")
    int deleteAddress(@Param("addressId") Long addressId, @Param("userId") Long userId);

    /**
     * 将用户的所有地址设为非默认
     * 
     * @param userId 用户ID
     * @param countryCode 国家代码
     * @return 影响行数
     */
    @Update({
        "UPDATE user_address SET is_default = 0 ",
        "WHERE user_id = #{userId} AND country_code = #{countryCode} AND is_default = 1 AND is_deleted = 0"
    })
    int resetDefaultAddress(@Param("userId") Long userId, @Param("countryCode") String countryCode);

    /**
     * 设置指定地址为默认地址
     * 
     * @param addressId 地址ID
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update({
        "UPDATE user_address SET is_default = 1, last_used_time = NOW() ",
        "WHERE address_id = #{addressId} AND user_id = #{userId} AND is_deleted = 0"
    })
    int setDefaultAddress(@Param("addressId") Long addressId, @Param("userId") Long userId);

    /**
     * 更新地址最后使用时间
     * 
     * @param addressId 地址ID
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update({
        "UPDATE user_address SET last_used_time = NOW() ",
        "WHERE address_id = #{addressId} AND user_id = #{userId}"
    })
    int updateLastUsedTime(@Param("addressId") Long addressId, @Param("userId") Long userId);

    /**
     * 统计用户的地址数量
     * 
     * @param userId 用户ID
     * @param countryCode 国家代码
     * @return 地址数量
     */
    @Select({
        "SELECT COUNT(*) FROM user_address ",
        "WHERE user_id = #{userId} AND country_code = #{countryCode} AND is_deleted = 0"
    })
    int countUserAddresses(@Param("userId") Long userId, @Param("countryCode") String countryCode);

    /**
     * 查询支持的国家列表
     * 
     * @return 国家配置列表
     */
    @Select({
        "SELECT country_code, country_name, country_name_en, address_format ",
        "FROM address_country_config WHERE is_active = 1 ORDER BY sort_order"
    })
    List<Map<String, Object>> selectActiveCountries();
} 