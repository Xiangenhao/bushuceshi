package org.example.afd.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一地址数据传输对象
 * 支持多国地址格式，当前主要支持日本地址
 * 
 * @author system
 * @date 2025-01-03
 */
public class UnifiedAddressDTO implements Serializable {
    
    /** 地址ID */
    private Long addressId;
    
    /** 用户ID */
    private Long userId;
    
    /** 国家代码: JP-日本, CN-中国 */
    private String countryCode;
    
    /** 收货人姓名 */
    private String receiverName;
    
    /** 收货人电话 */
    private String receiverPhone;
    
    /** 是否默认地址，0-否，1-是 */
    private Boolean isDefault;
    
    /** 地址类型，1-家庭，2-公司，3-其他 */
    private Integer addressType;
    
    /** 配送说明 */
    private String deliveryInstructions;
    
    /** 最后使用时间 */
    private LocalDateTime lastUsedTime;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    // === 日本地址专用字段 ===
    /** 邮政编码(例:123-4567) */
    private String postalCode;
    
    /** 都道府県(例:東京都) */
    private String prefecture;
    
    /** 市区町村(例:新宿区) */
    private String municipality;
    
    /** 町名(例:西新宿) */
    private String town;
    
    /** 丁目 */
    private String chome;
    
    /** 番地(门牌号) */
    private String banchi;
    
    /** 建筑名/公寓名 */
    private String building;
    
    /** 房间号 */
    private String roomNumber;
    
    /** 地址第一行(自动生成) */
    private String addressLine1;
    
    /** 地址第二行(建筑/房间) */
    private String addressLine2;
    
    // === 中国地址专用字段（预留） ===
    /** 省份 */
    private String province;
    
    /** 城市 */
    private String city;
    
    /** 区县 */
    private String district;
    
    /** 街道 */
    private String street;
    
    /** 详细地址 */
    private String detailAddress;

    public UnifiedAddressDTO() {
        this.countryCode = "JP"; // 默认日本
    }

    // === Getters and Setters ===
    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Integer getAddressType() {
        return addressType;
    }

    public void setAddressType(Integer addressType) {
        this.addressType = addressType;
    }

    public String getDeliveryInstructions() {
        return deliveryInstructions;
    }

    public void setDeliveryInstructions(String deliveryInstructions) {
        this.deliveryInstructions = deliveryInstructions;
    }

    public LocalDateTime getLastUsedTime() {
        return lastUsedTime;
    }

    public void setLastUsedTime(LocalDateTime lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    // 日本地址字段
    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPrefecture() {
        return prefecture;
    }

    public void setPrefecture(String prefecture) {
        this.prefecture = prefecture;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getChome() {
        return chome;
    }

    public void setChome(String chome) {
        this.chome = chome;
    }

    public String getBanchi() {
        return banchi;
    }

    public void setBanchi(String banchi) {
        this.banchi = banchi;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    // 中国地址字段（预留）
    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    // === 兼容性方法（兼容现有代码） ===
    public String getReceiver() {
        return receiverName;
    }

    public void setReceiver(String receiver) {
        this.receiverName = receiver;
    }

    public String getPhone() {
        return receiverPhone;
    }

    public void setPhone(String phone) {
        this.receiverPhone = phone;
    }

    public boolean isDefault() {
        return isDefault != null ? isDefault : false;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * 获取完整的格式化地址
     * 根据国家代码返回不同格式的地址
     * 
     * @return 完整地址字符串
     */
    public String getFullAddress() {
        if ("JP".equals(countryCode)) {
            return getJapanFormattedAddress();
        } else if ("CN".equals(countryCode)) {
            return getChinaFormattedAddress();
        }
        return "";
    }

    /**
     * 获取日本格式的完整地址
     * 格式: 〒postal_code prefecture municipality town chome banchi building room_number
     * 
     * @return 日本格式地址
     */
    private String getJapanFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        
        // 邮政编码
        if (postalCode != null && !postalCode.isEmpty()) {
            sb.append("〒").append(postalCode).append(" ");
        }
        
        // 都道府県
        if (prefecture != null && !prefecture.isEmpty()) {
            sb.append(prefecture);
        }
        
        // 市区町村
        if (municipality != null && !municipality.isEmpty()) {
            sb.append(municipality);
        }
        
        // 町名
        if (town != null && !town.isEmpty()) {
            sb.append(town);
        }
        
        // 丁目
        if (chome != null && !chome.isEmpty()) {
            sb.append(chome);
        }
        
        // 番地
        if (banchi != null && !banchi.isEmpty()) {
            sb.append(banchi);
        }
        
        // 建筑名
        if (building != null && !building.isEmpty()) {
            sb.append(" ").append(building);
        }
        
        // 房间号
        if (roomNumber != null && !roomNumber.isEmpty()) {
            sb.append(" ").append(roomNumber);
        }
        
        return sb.toString().trim();
    }

    /**
     * 获取中国格式的完整地址（预留）
     * 
     * @return 中国格式地址
     */
    private String getChinaFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        
        if (province != null) sb.append(province);
        if (city != null) sb.append(city);
        if (district != null) sb.append(district);
        if (street != null) sb.append(street);
        if (detailAddress != null) sb.append(detailAddress);
        
        return sb.toString();
    }

    /**
     * 自动生成地址行
     * 根据日本地址格式自动生成addressLine1和addressLine2
     */
    public void generateJapanAddressLines() {
        if (!"JP".equals(countryCode)) {
            return;
        }
        
        // 生成地址第一行: prefecture + municipality + town + chome + banchi
        StringBuilder line1 = new StringBuilder();
        if (prefecture != null && !prefecture.isEmpty()) {
            line1.append(prefecture);
        }
        if (municipality != null && !municipality.isEmpty()) {
            line1.append(municipality);
        }
        if (town != null && !town.isEmpty()) {
            line1.append(town);
        }
        if (chome != null && !chome.isEmpty()) {
            line1.append(chome);
        }
        if (banchi != null && !banchi.isEmpty()) {
            line1.append(banchi);
        }
        this.addressLine1 = line1.toString();
        
        // 生成地址第二行: building + room_number
        StringBuilder line2 = new StringBuilder();
        if (building != null && !building.isEmpty()) {
            line2.append(building);
        }
        if (roomNumber != null && !roomNumber.isEmpty()) {
            if (line2.length() > 0) line2.append(" ");
            line2.append(roomNumber);
        }
        this.addressLine2 = line2.toString();
    }

    /**
     * 获取地址类型显示名称
     * 
     * @return 地址类型名称
     */
    public String getAddressTypeName() {
        if (addressType == null) return "その他";
        switch (addressType) {
            case 1: return "自宅";
            case 2: return "会社";
            case 3: return "その他";
            default: return "その他";
        }
    }

    /**
     * 验证日本地址是否完整
     * 
     * @return 是否完整
     */
    public boolean isJapanAddressValid() {
        if (!"JP".equals(countryCode)) {
            return false;
        }
        
        return postalCode != null && !postalCode.isEmpty() &&
               prefecture != null && !prefecture.isEmpty() &&
               municipality != null && !municipality.isEmpty();
    }

    @Override
    public String toString() {
        return "UnifiedAddressDTO{" +
                "addressId=" + addressId +
                ", countryCode='" + countryCode + '\'' +
                ", receiverName='" + receiverName + '\'' +
                ", fullAddress='" + getFullAddress() + '\'' +
                ", isDefault=" + isDefault +
                '}';
    }
} 