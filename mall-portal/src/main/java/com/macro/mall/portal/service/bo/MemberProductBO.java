package com.macro.mall.portal.service.bo;

import com.macro.mall.model.PmsProductCategory;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Data
public class MemberProductBO {
    private List<PmsProductCategory> hotGames;
    private List<PmsProductCategory> historyGames;
    private TreeMap<String,List<PmsProductCategory>> allGames;
}
