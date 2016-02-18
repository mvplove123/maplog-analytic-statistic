package com.jerry.map.controller;


import com.jerry.map.service.LogAnalysisService;
import com.jerry.map.service.LogParseService;
import com.jerry.map.service.LogStatisticsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * Created by admin on 2016/1/5.
 */
@Controller
@RequestMapping("logStatisticReport")
public class LogStatisticReportController extends AbstractController {

    @Resource
    private LogStatisticsService logStatisticsService;

    @Resource
    private LogParseService logParseService;

    @Resource
    private LogAnalysisService logAnalysisService;

    @ResponseBody
    @RequestMapping(value = "createReport")
    public Map<Object, Object> createReport(String time) {
        logStatisticsService.createReport(time);
        return dataJson("创建完成");
    }


    @ResponseBody
    @RequestMapping(value = "logAnalyze")
    public Map<Object, Object> logAnalyze() {
        try {
            logParseService.logParse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataJson("日志分析完成");
    }

    @ResponseBody
    @RequestMapping(value = "hotPoiAnalyze")
    public Map<Object, Object> hotPoiAnalyze(@RequestParam(value = "city", required = false, defaultValue = "北京市") String city ,
                                             @RequestParam(value = "time", required = true) String time) {
        logStatisticsService.hotPoiStatistic(time,city);
        return dataJson("热门日志分析完成");
    }


    @ResponseBody
    @RequestMapping(value = "hotPoiOccupies")
    public Map<Object, Object> hotPoiOccupies() {
        logStatisticsService.hotPoiOccupies();
        return dataJson("热门poi占比统计完成");
    }

    @ResponseBody
    @RequestMapping(value = "splitWord")
    public Map<Object, Object> splitWord() {
        logAnalysisService.splitWord();
        return dataJson("热门poi分词完成");
    }


    @ResponseBody
    @RequestMapping(value = "catePoiStatistic")
    public Map<Object, Object> catePoiStatistic() {
        logStatisticsService.catePoiStatistic();
        return dataJson("分类词统计完成");
    }

    @ResponseBody
    @RequestMapping(value = "didPoiStatistic")
    public Map<Object, Object> didPoiStatistic() {
        logStatisticsService.didPoiStatistic();
        return dataJson("did分类词统计完成");
    }
}
