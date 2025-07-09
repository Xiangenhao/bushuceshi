package org.example.afd.service;

import org.example.afd.dto.CategoryDTO;
import org.example.afd.dto.MerchantDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 商家服务接口
 */
public interface MerchantService {

    /**
     * 获取商家信息
     *
     * @param merchantId 商家ID
     * @return 商家信息
     */
    MerchantDTO getMerchantInfo(Long merchantId);

    /**
     * 获取商家详情
     *
     * @param merchantId 商家ID
     * @return 商家详情
     */
    MerchantDTO getMerchantDetail(Long merchantId);

    /**
     * 根据用户ID获取商家信息
     *
     * @param userId 用户ID
     * @return 商家信息，如果用户不是商家则返回null
     */
    MerchantDTO getMerchantByUserId(Long userId);

    /**
     * 注册商家
     *
     * @param merchantDTO 商家信息
     * @return 注册后的商家信息
     */
    MerchantDTO registerMerchant(MerchantDTO merchantDTO);

    /**
     * 获取推荐商家列表
     *
     * @param page 页码
     * @param size 每页数量
     * @return 商家列表
     */
    List<MerchantDTO> getRecommendedMerchants(int page, int size);

    /**
     * 获取附近商家列表
     *
     * @param latitude 纬度
     * @param longitude 经度
     * @param distance 距离（单位：公里）
     * @param page 页码
     * @param size 每页数量
     * @return 商家列表
     */
    List<MerchantDTO> getNearbyMerchants(double latitude, double longitude, double distance, int page, int size);

    /**
     * 获取商家分类列表
     *
     * @param merchantId 商家ID
     * @return 分类列表
     */
    List<CategoryDTO> getMerchantCategories(Long merchantId);

    /**
     * 搜索商家
     *
     * @param keyword 关键词
     * @param page 页码
     * @param size 每页数量
     * @return 商家列表
     */
    List<MerchantDTO> searchMerchants(String keyword, int page, int size);

    /**
     * 关注商家
     *
     * @param merchantId 商家ID
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean followMerchant(Long merchantId, Long userId);

    /**
     * 取消关注商家
     *
     * @param merchantId 商家ID
     * @param userId 用户ID
     * @return 是否成功
     */
    Boolean unfollowMerchant(Long merchantId, Long userId);

    /**
     * 获取用户关注的商家列表
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 商家列表
     */
    List<MerchantDTO> getUserFollowedMerchants(Long userId, int page, int size);

    /**
     * 对商家进行评价
     *
     * @param merchantId 商家ID
     * @param reviewData 评价数据
     * @return 是否成功
     */
    Boolean reviewMerchant(Long merchantId, Map<String, Object> reviewData);

    /**
     * 更新商家信息
     *
     * @param merchantDTO 商家信息
     * @return 更新后的商家信息
     */
    MerchantDTO updateMerchantInfo(MerchantDTO merchantDTO);

    /**
     * 上传商家Logo
     *
     * @param merchantId 商家ID
     * @param file Logo文件
     * @return 更新后的商家信息
     */
    MerchantDTO uploadLogo(Long merchantId, MultipartFile file);

    /**
     * 上传商家营业执照
     *
     * @param merchantId 商家ID
     * @param file 营业执照文件
     * @return 更新后的商家信息
     */
    MerchantDTO uploadLicense(Long merchantId, MultipartFile file);

    /**
     * 更新商家支付信息
     *
     * @param merchantId 商家ID
     * @param paymentInfo 支付信息
     * @return 更新后的商家信息
     */
    MerchantDTO updatePaymentInfo(Long merchantId, Map<String, Object> paymentInfo);
}