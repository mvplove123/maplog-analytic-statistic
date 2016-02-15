package com.jerry.map.service.impl;

import com.jerry.map.service.AbstractService;
import com.jerry.map.service.LogExtractService;
import com.jerry.map.utils.Constants;
import com.jerry.map.utils.PropertiesUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by admin on 2016/2/15.
 */
@Service
public class SearchLogExtractServiceImpl extends AbstractService implements LogExtractService {

    @Resource
    private CommonLogExtractServiceImpl commonLogExtractService;

    /**
     * 搜索日志提取
     */
    public void logExtractByCity() {

        String city = PropertiesUtils.getPropertiesValue("city");

        commonLogExtractService.logExtractByCity(city,"searchLogPath", Constants.SEARCH_LOG);


    }
}
