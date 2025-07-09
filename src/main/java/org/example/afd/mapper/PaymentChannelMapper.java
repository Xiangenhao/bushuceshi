package org.example.afd.mapper;

import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

/**
 * 支付渠道数据访问层
 * @author AI Assistant
 * @date 2025-06-02
 */
@Mapper
public interface PaymentChannelMapper {
    
    /**
     * 获取所有启用的支付渠道
     * @return 支付渠道列表
     */
    @Select("SELECT channel_id, channel_code, channel_name, icon_url, description, sort_order " +
            "FROM afd.pay_channels WHERE is_enabled = 1 ORDER BY sort_order ASC, channel_id ASC")
    List<Map<String, Object>> getEnabledPaymentChannels();
    
    /**
     * 获取所有支付渠道（管理后台用）
     * @return 支付渠道列表
     */
    @Select("SELECT channel_id, channel_code, channel_name, icon_url, description, is_enabled, " +
            "sort_order, config, create_time, update_time " +
            "FROM afd.pay_channels ORDER BY sort_order ASC, channel_id ASC")
    List<Map<String, Object>> getAllPaymentChannels();
    
    /**
     * 根据ID获取支付渠道详情
     * @param channelId 渠道ID
     * @return 支付渠道详情
     */
    @Select("SELECT channel_id, channel_code, channel_name, icon_url, description, is_enabled, " +
            "sort_order, config, create_time, update_time " +
            "FROM afd.pay_channels WHERE channel_id = #{channelId}")
    Map<String, Object> getPaymentChannelById(@Param("channelId") Integer channelId);
    
    /**
     * 创建支付渠道
     */
    @Insert("INSERT INTO afd.pay_channels (channel_code, channel_name, icon_url, description, " +
            "is_enabled, sort_order, config, create_time, update_time) " +
            "VALUES (#{channelCode}, #{channelName}, #{iconUrl}, #{description}, " +
            "#{isEnabled}, #{sortOrder}, #{config}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "channelId")
    int insertPaymentChannel(@Param("channelCode") String channelCode, 
                            @Param("channelName") String channelName,
                            @Param("iconUrl") String iconUrl, 
                            @Param("description") String description,
                            @Param("isEnabled") Integer isEnabled, 
                            @Param("sortOrder") Integer sortOrder,
                            @Param("config") String config, 
                            @Param("channelId") Integer channelId);
    
    /**
     * 更新支付渠道
     */
    @Update("UPDATE afd.pay_channels SET channel_code = #{channelCode}, channel_name = #{channelName}, " +
            "icon_url = #{iconUrl}, description = #{description}, is_enabled = #{isEnabled}, " +
            "sort_order = #{sortOrder}, config = #{config}, update_time = NOW() " +
            "WHERE channel_id = #{channelId}")
    int updatePaymentChannel(@Param("channelId") Integer channelId,
                            @Param("channelCode") String channelCode, 
                            @Param("channelName") String channelName,
                            @Param("iconUrl") String iconUrl, 
                            @Param("description") String description,
                            @Param("isEnabled") Integer isEnabled, 
                            @Param("sortOrder") Integer sortOrder,
                            @Param("config") String config);
    
    /**
     * 删除支付渠道
     * @param channelId 渠道ID
     * @return 删除条数
     */
    @Delete("DELETE FROM afd.pay_channels WHERE channel_id = #{channelId}")
    int deletePaymentChannel(@Param("channelId") Integer channelId);
    
    /**
     * 更新支付渠道状态
     * @param channelId 渠道ID
     * @param isEnabled 是否启用
     * @return 更新条数
     */
    @Update("UPDATE afd.pay_channels SET is_enabled = #{isEnabled}, update_time = NOW() " +
            "WHERE channel_id = #{channelId}")
    int updatePaymentChannelStatus(@Param("channelId") Integer channelId, @Param("isEnabled") Integer isEnabled);
} 