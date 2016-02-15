package com.jerry.map.service.impl;

import com.jerry.map.service.AbstractService;
import com.jerry.map.service.LogExtractService;
import com.jerry.map.utils.Constants;
import com.jerry.map.utils.FileHandler;
import com.jerry.map.utils.PropertiesUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 2016/2/15.
 * bus log extract
 */
@Service
public class BusLogExtractServiceImpl extends AbstractService implements LogExtractService {

    @Resource
    private CommonLogExtractServiceImpl commonLogExtractService;
    /**
     * bus log extract
     */
    public void logExtractByCity() {

        String city = PropertiesUtils.getPropertiesValue("city");
        commonLogExtractService.logExtractByCity(city,"commonPath", Constants.BUS_LOG);

    }



}
