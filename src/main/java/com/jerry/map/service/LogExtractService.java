package com.jerry.map.service;

import com.jerry.map.model.Log;

import java.util.List;

/**
 * Created by admin on 2016/2/2.
 */
public interface LogExtractService {


    /**
     * 提取指定城市log日志
     */
    void logExtractByCity();


    /**
     * 解析log 字段
     *
     * @param line
     * @return
     */
    List<Log> logParseByCity(String line, String targetCity ,String logSource);


}
