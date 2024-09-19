package com.macro.mall.portal.service.bo;

import lombok.Data;

@Data
public class HotGameBO {
    // 热门游戏的id
    private Long sid;
    // 热门游戏名称
    private String gameName;
    // 热门游戏图片
    private String gamePic;
}
