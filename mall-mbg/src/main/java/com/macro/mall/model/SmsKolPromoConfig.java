package com.macro.mall.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.Date;

public class SmsKolPromoConfig implements Serializable {
    private Long id;

    private String kolName;

    private String kolId;

    private String kolQrCode;

    private String kolH5Link;

    private Date createTime;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKolName() {
        return kolName;
    }

    public void setKolName(String kolName) {
        this.kolName = kolName;
    }

    public String getKolId() {
        return kolId;
    }

    public void setKolId(String kolId) {
        this.kolId = kolId;
    }

    public String getKolQrCode() {
        return kolQrCode;
    }

    public void setKolQrCode(String kolQrCode) {
        this.kolQrCode = kolQrCode;
    }

    public String getKolH5Link() {
        return kolH5Link;
    }

    public void setKolH5Link(String kolH5Link) {
        this.kolH5Link = kolH5Link;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", kolName=").append(kolName);
        sb.append(", kolId=").append(kolId);
        sb.append(", kolQrCode=").append(kolQrCode);
        sb.append(", kolH5Link=").append(kolH5Link);
        sb.append(", createTime=").append(createTime);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}