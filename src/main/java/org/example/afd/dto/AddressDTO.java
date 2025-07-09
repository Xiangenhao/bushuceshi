package org.example.afd.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户地址数据传输对象
 * 对应数据库表：user_address + user_address_japan
 * 
 * @author system
 * @date 2024-12-28
 */
public class AddressDTO implements Serializable {
    
    /** 地址ID */
    private Long addressId;
    
    /** 用户ID */
    private Long userId;
    
    /** 收货人姓名 */
    private String receiverName;
    
    /** 收货人电话 */
    private String receiverPhone;
    
    /** 邮政编码(例:123-4567) */
    private String postalCode;
    
    /** 都道府县(例:东京都) */
    private String prefecture;
    
    /** 市区町村(例:新宿区) */
    private String city;
    
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
    
    /** 是否默认，0-否，1-是 */
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
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

    // 兼容旧代码的方法
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

    public String getProvince() {
        return prefecture;
    }

    public void setProvince(String province) {
        this.prefecture = province;
    }

    public String getDistrict() {
        return city;
    }

    public void setDistrict(String district) {
        this.city = district;
    }

    public String getDetailAddress() {
        return addressLine1;
    }

    public void setDetailAddress(String detailAddress) {
        this.addressLine1 = detailAddress;
    }

    public String getZipCode() {
        return postalCode;
    }

    public void setZipCode(String zipCode) {
        this.postalCode = zipCode;
    }

    public boolean isDefault() {
        return isDefault != null ? isDefault : false;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    /**
     * 获取完整地址
     * @return 完整地址字符串
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (prefecture != null) sb.append(prefecture);
        if (city != null) sb.append(" ").append(city);
        if (town != null) sb.append(" ").append(town);
        if (chome != null) sb.append(" ").append(chome);
        if (banchi != null) sb.append(" ").append(banchi);
        if (building != null) sb.append(" ").append(building);
        if (roomNumber != null) sb.append(" ").append(roomNumber);
        return sb.toString().trim();
    }

    /**
     * 自动生成地址行
     */
    public void generateAddressLines() {
        // 生成地址第一行
        StringBuilder line1 = new StringBuilder();
        if (prefecture != null) line1.append(prefecture);
        if (city != null) line1.append(city);
        if (town != null) line1.append(town);
        if (chome != null) line1.append(chome);
        if (banchi != null) line1.append(banchi);
        this.addressLine1 = line1.toString();
        
        // 生成地址第二行
        StringBuilder line2 = new StringBuilder();
        if (building != null) line2.append(building);
        if (roomNumber != null) {
            if (line2.length() > 0) line2.append(" ");
            line2.append(roomNumber);
        }
        this.addressLine2 = line2.toString();
    }
} 