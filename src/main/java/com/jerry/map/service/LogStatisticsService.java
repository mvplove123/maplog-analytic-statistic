package com.jerry.map.service;


import com.jerry.map.model.StatisticReport;

import java.util.List;

/**
 * Created by admin on 2016/1/5.
 */
public interface LogStatisticsService {


    public List<StatisticReport> createReport(String time);

    public void hotPoiStatistic(String time , String city);


    public void hotPoiOccupies();

    public void catePoiStatistic();

    public void didPoiStatistic();

}
