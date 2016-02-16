package com.jerry.map.service;


import com.jerry.map.model.StatisticReport;

import java.util.List;

/**
 * Created by admin on 2016/1/5.
 */
public interface LogStatisticsService {


    List<StatisticReport> createReport(String time);

    void hotPoiStatistic(String time, String city);

    void hotPoiOccupies();

    void catePoiStatistic();

    void didPoiStatistic();

}
