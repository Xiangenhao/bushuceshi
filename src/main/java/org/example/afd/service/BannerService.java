package org.example.afd.service;

import org.example.afd.dto.BannerDTO;
import java.util.List;

/**
 * 轮播图服务接口
 */
public interface BannerService {

    /**
     * 获取轮播图列表
     * @param position 轮播图位置，如home-首页，category-分类页
     * @return 轮播图列表
     */
    List<BannerDTO> getBanners(String position);
    
    /**
     * 获取轮播图详情
     * @param bannerId 轮播图ID
     * @return 轮播图详情
     */
    BannerDTO getBanner(Long bannerId);
} 