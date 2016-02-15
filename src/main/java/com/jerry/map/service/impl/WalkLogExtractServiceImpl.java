package com.jerry.map.service.impl;

import com.jerry.map.service.LogExtractService;
import com.jerry.map.utils.Constants;
import com.jerry.map.utils.PropertiesUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by admin on 2016/2/15.
 * walk log extract
 */
@Service
public class WalkLogExtractServiceImpl implements LogExtractService {

    @Resource
    private CommonLogExtractServiceImpl commonLogExtractService;
    public void logExtractByCity() {
        String city = PropertiesUtils.getPropertiesValue("city");

        commonLogExtractService.logExtractByCity(city,"commonPath", Constants.WALK_LOG);
    }
}
