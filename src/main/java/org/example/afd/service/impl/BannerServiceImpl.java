package org.example.afd.service.impl;

import org.example.afd.dto.BannerDTO;
import org.example.afd.mapper.BannerMapper;
import org.example.afd.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 轮播图服务实现类
 */
@Service
public class BannerServiceImpl implements BannerService {

    @Autowired
    private BannerMapper bannerMapper;
    
    @Override
    public List<BannerDTO> getBanners(String position) {
        List<Map<String, Object>> banners = bannerMapper.selectBannersByPosition(position);
        List<BannerDTO> bannerDTOs = new ArrayList<>();
        
        if (banners != null && !banners.isEmpty()) {
            for (Map<String, Object> banner : banners) {
                BannerDTO bannerDTO = convertToBannerDTO(banner);
                bannerDTOs.add(bannerDTO);
            }
        }
        
        return bannerDTOs;
    }
    
    @Override
    public BannerDTO getBanner(Long bannerId) {
        Map<String, Object> banner = bannerMapper.selectBannerById(bannerId);
        if (banner == null) {
            return null;
        }
        return convertToBannerDTO(banner);
    }
    
    /**
     * 将Map转换为BannerDTO
     * @param banner 轮播图信息
     * @return BannerDTO
     */
    private BannerDTO convertToBannerDTO(Map<String, Object> banner) {
        BannerDTO bannerDTO = new BannerDTO();
        
        // 安全转换 bannerId
        Object bannerIdObj = banner.get("banner_id");
        if (bannerIdObj != null) {
            if (bannerIdObj instanceof Long) {
                bannerDTO.setBannerId((Long) bannerIdObj);
            } else if (bannerIdObj instanceof Integer) {
                bannerDTO.setBannerId(((Integer) bannerIdObj).longValue());
            }
        }
        
        // 安全转换 title
        bannerDTO.setTitle((String) banner.get("title"));
        
        // 安全转换 imageUrl
        bannerDTO.setImageUrl((String) banner.get("image_url"));
        
        // 安全转换 linkType
        Object linkTypeObj = banner.get("link_type");
        if (linkTypeObj != null) {
            if (linkTypeObj instanceof Integer) {
                bannerDTO.setLinkType((Integer) linkTypeObj);
            } else if (linkTypeObj instanceof Byte) {
                bannerDTO.setLinkType(((Byte) linkTypeObj).intValue());
            } else if (linkTypeObj instanceof String) {
                try {
                    bannerDTO.setLinkType(Integer.parseInt((String) linkTypeObj));
                } catch (NumberFormatException e) {
                    bannerDTO.setLinkType(1); // 默认值
                }
            }
        }
        
        // 安全转换 targetId
        Object targetIdObj = banner.get("target_id");
        if (targetIdObj != null) {
            if (targetIdObj instanceof Long) {
                bannerDTO.setTargetId((Long) targetIdObj);
            } else if (targetIdObj instanceof Integer) {
                bannerDTO.setTargetId(((Integer) targetIdObj).longValue());
            }
        }
        
        // 安全转换 linkUrl
        bannerDTO.setLinkUrl((String) banner.get("link_url"));
        
        // 安全转换 position
        bannerDTO.setPosition((String) banner.get("position"));
        
        // 安全转换 sortOrder
        Object sortOrderObj = banner.get("sort_order");
        if (sortOrderObj != null) {
            if (sortOrderObj instanceof Integer) {
                bannerDTO.setSortOrder((Integer) sortOrderObj);
            } else if (sortOrderObj instanceof Byte) {
                bannerDTO.setSortOrder(((Byte) sortOrderObj).intValue());
            }
        }
        
        // 安全转换 startTime
        Object startTimeObj = banner.get("start_time");
        if (startTimeObj != null) {
            if (startTimeObj instanceof Long) {
                bannerDTO.setStartTime((Long) startTimeObj);
            } else if (startTimeObj instanceof Integer) {
                bannerDTO.setStartTime(((Integer) startTimeObj).longValue());
            }
        }
        
        // 安全转换 endTime
        Object endTimeObj = banner.get("end_time");
        if (endTimeObj != null) {
            if (endTimeObj instanceof Long) {
                bannerDTO.setEndTime((Long) endTimeObj);
            } else if (endTimeObj instanceof Integer) {
                bannerDTO.setEndTime(((Integer) endTimeObj).longValue());
            }
        }
        
        // 安全转换 status
        Object statusObj = banner.get("status");
        if (statusObj != null) {
            if (statusObj instanceof Integer) {
                bannerDTO.setStatus((Integer) statusObj);
            } else if (statusObj instanceof Byte) {
                bannerDTO.setStatus(((Byte) statusObj).intValue());
            }
        }
        
        return bannerDTO;
    }
} 