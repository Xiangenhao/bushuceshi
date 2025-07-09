package org.example.afd.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 促销活动数据访问接口
 */
@Mapper
public interface PromotionMapper {
    
    /**
     * 查询折扣活动列表
     * @param offset 偏移量
     * @param size 查询数量
     * @return 折扣活动列表
     */
    @Select("SELECT * FROM shop_discount WHERE status = 1 " +
            "AND start_time <= NOW() AND end_time >= NOW() " +
            "ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<Map<String, Object>> selectDiscounts(@Param("offset") int offset, @Param("size") int size);
    
    /**
     * 统计有效折扣活动数量
     * @return 活动数量
     */
    @Select("SELECT COUNT(*) FROM shop_discount WHERE status = 1 " +
            "AND start_time <= NOW() AND end_time >= NOW()")
    int countDiscounts();
    
    /**
     * 根据ID查询折扣活动
     * @param discountId 折扣ID
     * @return 折扣活动信息
     */
    @Select("SELECT * FROM shop_discount WHERE discount_id = #{discountId}")
    Map<String, Object> selectDiscountById(@Param("discountId") Long discountId);
    
    /**
     * 查询满减活动列表
     * @param offset 偏移量
     * @param size 查询数量
     * @return 满减活动列表
     */
    @Select("SELECT * FROM shop_full_reduction WHERE status = 1 " +
            "AND start_time <= NOW() AND end_time >= NOW() " +
            "ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<Map<String, Object>> selectFullReductions(@Param("offset") int offset, @Param("size") int size);
    
    /**
     * 统计有效满减活动数量
     * @return 活动数量
     */
    @Select("SELECT COUNT(*) FROM shop_full_reduction WHERE status = 1 " +
            "AND start_time <= NOW() AND end_time >= NOW()")
    int countFullReductions();
    
    /**
     * 根据ID查询满减活动
     * @param reductionId 满减ID
     * @return 满减活动信息
     */
    @Select("SELECT * FROM shop_full_reduction WHERE reduction_id = #{reductionId}")
    Map<String, Object> selectFullReductionById(@Param("reductionId") Long reductionId);
    
    /**
     * 查询满减规则
     * @param reductionId 满减活动ID
     * @return 满减规则列表
     */
    @Select("SELECT * FROM shop_full_reduction_rule WHERE reduction_id = #{reductionId} " +
            "ORDER BY full_amount ASC")
    List<Map<String, Object>> selectFullReductionRules(@Param("reductionId") Long reductionId);
    
    /**
     * 查询拼团活动列表
     * @param offset 偏移量
     * @param size 查询数量
     * @return 拼团活动列表
     */
    @Select("SELECT * FROM shop_group_buy WHERE status = 1 " +
            "AND start_time <= NOW() AND end_time >= NOW() " +
            "ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<Map<String, Object>> selectGroupBuys(@Param("offset") int offset, @Param("size") int size);
    
    /**
     * 统计有效拼团活动数量
     * @return 活动数量
     */
    @Select("SELECT COUNT(*) FROM shop_group_buy WHERE status = 1 " +
            "AND start_time <= NOW() AND end_time >= NOW()")
    int countGroupBuys();
    
    /**
     * 根据ID查询拼团活动
     * @param groupBuyId 拼团ID
     * @return 拼团活动信息
     */
    @Select("SELECT * FROM shop_group_buy WHERE group_buy_id = #{groupBuyId}")
    Map<String, Object> selectGroupBuyById(@Param("groupBuyId") Long groupBuyId);
    
    /**
     * 根据活动类型和ID查询关联的商品
     * @param type 活动类型(discount: 折扣, full_reduction: 满减, group_buy: 拼团)
     * @param promotionId 活动ID
     * @return 商品ID列表
     */
    @Select("<script>" +
            "SELECT p.* FROM shop_product p " +
            "<choose>" +
            "  <when test=\"type == 'discount'\">" +
            "    INNER JOIN shop_discount d ON d.discount_id = #{promotionId} " +
            "    <choose>" +
            "      <when test=\"d.use_range == 1\">WHERE p.status = 1</when>" +
            "      <when test=\"d.use_range == 2\">WHERE p.status = 1 AND p.category_id IN (${d.range_values})</when>" +
            "      <when test=\"d.use_range == 3\">WHERE p.status = 1 AND p.product_id IN (${d.range_values})</when>" +
            "    </choose>" +
            "  </when>" +
            "  <when test=\"type == 'full_reduction'\">" +
            "    INNER JOIN shop_full_reduction fr ON fr.reduction_id = #{promotionId} " +
            "    <choose>" +
            "      <when test=\"fr.use_range == 1\">WHERE p.status = 1</when>" +
            "      <when test=\"fr.use_range == 2\">WHERE p.status = 1 AND p.category_id IN (${fr.range_values})</when>" +
            "      <when test=\"fr.use_range == 3\">WHERE p.status = 1 AND p.product_id IN (${fr.range_values})</when>" +
            "    </choose>" +
            "  </when>" +
            "  <when test=\"type == 'group_buy'\">" +
            "    INNER JOIN shop_group_buy gb ON gb.group_buy_id = #{promotionId} " +
            "    WHERE p.product_id = gb.product_id AND p.status = 1" +
            "  </when>" +
            "</choose>" +
            "LIMIT 10" +
            "</script>")
    List<Map<String, Object>> selectRelatedProducts(@Param("type") String type, @Param("promotionId") Long promotionId);
} 