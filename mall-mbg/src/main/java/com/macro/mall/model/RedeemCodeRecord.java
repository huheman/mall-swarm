package com.macro.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Date;

public class RedeemCodeRecord implements Serializable {
    @Schema(title = "自增主键")
    private Long id;

    @Schema(title = "兑换码")
    private String redeemCode;

    @Schema(title = "兑换码对应的商品skuId")
    private Long skuId;

    @Schema(title = "对应的kolId")
    private String kolId;

    @Schema(title = "创建时间")
    private Date createTime;

    @Schema(title = "NOT_USED:未使用;USED:已使用")
    private String useStatus;

    @Schema(title = "使用人的手机号")
    private String usePhone;

    @Schema(title = "使用此兑换码的订单id")
    private String useOrderSn;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRedeemCode() {
        return redeemCode;
    }

    public void setRedeemCode(String redeemCode) {
        this.redeemCode = redeemCode;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public String getKolId() {
        return kolId;
    }

    public void setKolId(String kolId) {
        this.kolId = kolId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUseStatus() {
        return useStatus;
    }

    public void setUseStatus(String useStatus) {
        this.useStatus = useStatus;
    }

    public String getUsePhone() {
        return usePhone;
    }

    public void setUsePhone(String usePhone) {
        this.usePhone = usePhone;
    }

    public String getUseOrderSn() {
        return useOrderSn;
    }

    public void setUseOrderSn(String useOrderSn) {
        this.useOrderSn = useOrderSn;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", redeemCode=").append(redeemCode);
        sb.append(", skuId=").append(skuId);
        sb.append(", kolId=").append(kolId);
        sb.append(", createTime=").append(createTime);
        sb.append(", useStatus=").append(useStatus);
        sb.append(", usePhone=").append(usePhone);
        sb.append(", useOrderSn=").append(useOrderSn);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}