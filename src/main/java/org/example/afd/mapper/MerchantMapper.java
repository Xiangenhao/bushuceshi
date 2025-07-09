package org.example.afd.mapper;

import org.apache.ibatis.annotations.*;
import org.example.afd.dto.MerchantDTO;
import org.example.afd.model.Merchant;

import java.util.List;
import java.util.Map;

/**
 * 商家数据访问接口
 */
@Mapper
public interface MerchantMapper {
    
    /**
     * 根据ID查询商家信息
     * @param merchantId 商家ID
     * @return 商家信息
     */
    @Select("SELECT * FROM shop_merchant WHERE merchant_id = #{merchantId}")
    MerchantDTO selectMerchantById(@Param("merchantId") Long merchantId);
    
    /**
     * 根据ID查询商家信息，包含关联用户信息
     * @param merchantId 商家ID
     * @return 商家信息（包含用户头像和昵称）
     */
    @Select("SELECT m.*, u.avatar as userAvatar, u.username as userNickname " +
            "FROM shop_merchant m " +
            "LEFT JOIN users u ON m.user_id = u.user_id " +
            "WHERE m.merchant_id = #{merchantId}")
    MerchantDTO selectMerchantWithUserInfoById(@Param("merchantId") Long merchantId);
    
    /**
     * 根据ID查询商家详细信息
     * @param merchantId 商家ID
     * @return 商家详细信息
     */
    @Select("SELECT m.*, " +
            "(SELECT COUNT(*) FROM shop_product WHERE merchant_id = #{merchantId} AND status = 1) AS product_count, " +
            "(SELECT COUNT(*) FROM orders WHERE related_id = #{merchantId} AND order_type = 1) AS order_count " +
            "FROM shop_merchant m WHERE m.merchant_id = #{merchantId}")
    MerchantDTO selectMerchantDetailById(@Param("merchantId") Long merchantId);
    
    /**
     * 查询推荐商家列表
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 推荐商家列表
     */
    @Select("SELECT * FROM shop_merchant WHERE status = 1 ORDER BY commission_rate DESC LIMIT #{offset}, #{limit}")
    List<MerchantDTO> selectRecommendedMerchants(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 查询附近商家列表
     * 使用MySQL的地理空间函数计算距离，需要shop_merchant表中有经纬度字段
     * @param latitude 纬度
     * @param longitude 经度
     * @param distance 距离（单位：公里）
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 附近商家列表
     */
    @Select("SELECT *, " +
            "ST_Distance_Sphere(point(longitude, latitude), point(#{longitude}, #{latitude})) / 1000 AS distance " +
            "FROM shop_merchant " +
            "WHERE status = 1 " +
            "HAVING distance <= #{distance} " +
            "ORDER BY distance " +
            "LIMIT #{offset}, #{limit}")
    List<MerchantDTO> selectNearbyMerchants(@Param("latitude") double latitude, 
                                          @Param("longitude") double longitude,
                                          @Param("distance") double distance,
                                          @Param("offset") int offset, 
                                          @Param("limit") int limit);
    
    /**
     * 搜索商家
     * @param keyword 关键词
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 商家列表
     */
    @Select("SELECT * FROM shop_merchant " +
            "WHERE status = 1 AND (merchant_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR description LIKE CONCAT('%', #{keyword}, '%')) " +
            "LIMIT #{offset}, #{limit}")
    List<MerchantDTO> searchMerchants(@Param("keyword") String keyword, 
                                    @Param("offset") int offset, 
                                    @Param("limit") int limit);
    
    /**
     * 更新商家信息
     * @param merchantDTO 商家信息
     * @return 影响行数
     */
    @Update("UPDATE shop_merchant SET " +
            "merchant_name = #{merchantName}, " +
            "description = #{description}, " +
            "contact_name = #{contactName}, " +
            "contact_phone = #{contactPhone}, " +
            "contact_email = #{contactEmail}, " +
            "update_time = NOW() " +
            "WHERE merchant_id = #{merchantId}")
    int updateMerchant(MerchantDTO merchantDTO);
    
    /**
     * 更新商家Logo
     * @param merchantId 商家ID
     * @param logoUrl Logo URL
     * @return 影响行数
     */
    @Update("UPDATE shop_merchant SET logo = #{logoUrl}, update_time = NOW() WHERE merchant_id = #{merchantId}")
    int updateMerchantLogo(@Param("merchantId") Long merchantId, @Param("logoUrl") String logoUrl);
    
    /**
     * 更新商家营业执照
     * @param merchantId 商家ID
     * @param licenseUrl 营业执照URL
     * @return 影响行数
     */
    @Update("UPDATE shop_merchant SET business_license = #{licenseUrl}, update_time = NOW() WHERE merchant_id = #{merchantId}")
    int updateMerchantLicense(@Param("merchantId") Long merchantId, @Param("licenseUrl") String licenseUrl);
    
    /**
     * 更新商家支付信息
     * @param merchantId 商家ID
     * @param bankAccount 银行账号
     * @param bankName 银行名称
     * @param settlementCycle 结算周期
     * @return 影响行数
     */
    @Update("UPDATE shop_merchant SET " +
            "bank_account = #{bankAccount}, " +
            "bank_name = #{bankName}, " +
            "settlement_cycle = #{settlementCycle}, " +
            "update_time = NOW() " +
            "WHERE merchant_id = #{merchantId}")
    int updateMerchantPaymentInfo(@Param("merchantId") Long merchantId, 
                               @Param("bankAccount") String bankAccount,
                               @Param("bankName") String bankName,
                               @Param("settlementCycle") Integer settlementCycle);
    
    /**
     * 查询用户关注的商家列表
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 商家列表
     */
    @Select("SELECT m.* FROM shop_merchant m " +
            "JOIN user_relation r ON m.merchant_id = r.target_id " +
            "WHERE r.user_id = #{userId} AND r.relation_type = 1 AND r.status = 1 " +
            "ORDER BY r.create_time DESC " +
            "LIMIT #{offset}, #{limit}")
    List<MerchantDTO> selectUserFollowedMerchants(@Param("userId") Long userId, 
                                              @Param("offset") int offset, 
                                              @Param("limit") int limit);

    /**
     * 通过主键查询
     *
     * @param merchantId 商家ID
     * @return 商家信息
     */
    @Select("SELECT * FROM shop_merchant WHERE merchant_id = #{merchantId}")
    Merchant selectByPrimaryKey(Long merchantId);
    
    /**
     * 根据用户ID查询商家信息
     *
     * @param userId 用户ID
     * @return 商家信息
     */
    @Select("SELECT * FROM shop_merchant WHERE user_id = #{userId}")
    Merchant selectByUserId(Long userId);
    
    /**
     * 插入商家信息
     *
     * @param merchant 商家信息
     * @return 影响行数
     */
    @Insert("INSERT INTO shop_merchant (user_id, merchant_name, description, logo, business_license, " +
            "contact_name, contact_phone, contact_email, status, create_time, update_time) " +
            "VALUES (#{userId}, #{merchantName}, #{description}, #{logo}, #{businessLicense}, " +
            "#{contactName}, #{contactPhone}, #{contactEmail}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "merchantId")
    int insert(Merchant merchant);
    
    /**
     * 更新商家信息
     *
     * @param merchant 商家信息
     * @return 影响行数
     */
    int update(Merchant merchant);
    
    /**
     * 查询推荐商家列表
     *
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 商家列表
     */
    List<Merchant> selectRecommended(@Param("limit") int limit, @Param("offset") int offset);
    
    /**
     * 查询附近商家列表
     *
     * @param latitude 纬度
     * @param longitude 经度
     * @param distance 距离（单位：公里）
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 商家列表
     */
    List<Merchant> selectNearby(@Param("latitude") double latitude, 
                              @Param("longitude") double longitude, 
                              @Param("distance") double distance, 
                              @Param("limit") int limit, 
                              @Param("offset") int offset);
    
    /**
     * 搜索商家
     *
     * @param keyword 关键词
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 商家列表
     */
    List<Merchant> search(@Param("keyword") String keyword, 
                        @Param("limit") int limit, 
                        @Param("offset") int offset);
    
    /**
     * 获取用户关注的商家列表
     *
     * @param userId 用户ID
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 商家列表
     */
    List<Merchant> selectFollowedByUser(@Param("userId") Long userId, 
                                     @Param("limit") int limit, 
                                     @Param("offset") int offset);
} 