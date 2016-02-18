package com.jerry.map.service;

import com.jerry.map.model.Poi;

import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/1/25.
 */
public interface BasicDataService {

    /**
     * 加载分类信息
     *
     * @return
     */
    Map<String, String> loadCategoryInfo();

    /**
     * 加载指定城市下poi别名
     *
     * @return
     */
    Map<String, Poi> loadPoiAliasByCity();

    /**
     * 加载did对应的poi数据
     *
     * @return
     */
    Map<String, Poi> loadPoiByDid();

    /**
     * 加载queryName对应的poi列表数据
     *
     * @return
     */
    Map<String, List<Poi>> loadPoiByQueryName();

}
