package org.example.afd.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 轮播图数据访问接口
 */
@Mapper
public interface BannerMapper {
    
    /**
     * 根据位置查询轮播图列表
     * @param position 轮播图位置
     * @return 轮播图列表
     */
    @Select("SELECT * FROM t_banner WHERE position = #{position} AND status = 1 " +
            "AND (start_time IS NULL OR start_time <= UNIX_TIMESTAMP() * 1000) " +
            "AND (end_time IS NULL OR end_time >= UNIX_TIMESTAMP() * 1000) " +
            "ORDER BY sort_order ASC")
    List<Map<String, Object>> selectBannersByPosition(@Param("position") String position);
    
    /**
     * 根据ID查询轮播图信息
     * @param bannerId 轮播图ID
     * @return 轮播图信息
     */
    @Select("SELECT * FROM t_banner WHERE banner_id = #{bannerId}")
    Map<String, Object> selectBannerById(@Param("bannerId") Long bannerId);
} 