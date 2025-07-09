package org.example.afd.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户关系数据访问接口
 */
@Mapper
public interface UserRelationMapper {

    /**
     * 添加商家关注关系
     * @param userId 用户ID
     * @param merchantId 商家ID
     * @return 影响行数
     */
    @Insert("INSERT INTO user_relation(user_id, target_id, relation_type, status, create_time) " +
            "VALUES(#{userId}, #{merchantId}, 1, 1, NOW()) " +
            "ON DUPLICATE KEY UPDATE status = 1, update_time = NOW()")
    int insertMerchantFollow(@Param("userId") Long userId, @Param("merchantId") Long merchantId);
    
    /**
     * 删除商家关注关系
     * @param userId 用户ID
     * @param merchantId 商家ID
     * @return 影响行数
     */
    @Delete("UPDATE user_relation SET status = 0, update_time = NOW() " +
            "WHERE user_id = #{userId} AND target_id = #{merchantId} AND relation_type = 1")
    int deleteMerchantFollow(@Param("userId") Long userId, @Param("merchantId") Long merchantId);
    
    /**
     * 检查是否关注了商家
     * @param userId 用户ID
     * @param merchantId 商家ID
     * @return 是否关注
     */
    @Select("SELECT COUNT(*) FROM user_relation WHERE user_id = #{userId} AND target_id = #{merchantId} " +
            "AND relation_type = 1 AND status = 1")
    int checkMerchantFollow(@Param("userId") Long userId, @Param("merchantId") Long merchantId);
    
    /**
     * 获取用户关注的商家ID列表
     * @param userId 用户ID
     * @return 商家ID列表
     */
    @Select("SELECT target_id FROM user_relation WHERE user_id = #{userId} AND relation_type = 1 AND status = 1")
    List<Long> selectUserFollowedMerchantIds(@Param("userId") Long userId);
} 