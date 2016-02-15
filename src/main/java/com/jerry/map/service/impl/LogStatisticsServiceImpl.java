package com.jerry.map.service.impl;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.jerry.map.dao.LogStatisticsDao;
import com.jerry.map.model.Log;
import com.jerry.map.model.Poi;
import com.jerry.map.model.StatisticReport;
import com.jerry.map.service.AbstractService;
import com.jerry.map.service.BasicDataService;
import com.jerry.map.service.LogStatisticsService;
import com.jerry.map.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 2016/1/5.
 */


@Service
public class LogStatisticsServiceImpl extends AbstractService implements LogStatisticsService {
    @Resource
    private LogStatisticsDao logStatisticsDao;

    @Resource
    private BasicDataService basicDataService;

    String beginTime = PropertiesUtils.getPropertiesValue("beginTime");
    String endTime = PropertiesUtils.getPropertiesValue("endTime");
    String blackWord = "停车场";
    List<String> blackWords = Lists.newArrayList("停车场", "[位置]");


    @Override
    public List<StatisticReport> createReport(String time) {

        long start = System.currentTimeMillis();

        String excelpath = PropertiesUtils.getPropertiesValue("excelPath");
        String templatePath = PropertiesUtils.getPropertiesValue("templatePath");
        excelpath = excelpath + time + "result.xls";


        Double totalRate = 0.00;

        Log querylog = new Log();


        querylog.setBeginTime(beginTime);
        querylog.setEndTime(endTime);
//        querylog.setTime(time);
        List<Log> hotPoiListlist = logStatisticsDao.getHotPoiList(querylog);

        int allcount = logStatisticsDao.getAllCount(querylog).getNum();


        List<Log> list = hotPoiListlist.subList(0, 50000);
        List<String[]> tsheet0 = Lists.newArrayList();
        List<String[]> tsheet1 = Lists.newArrayList();
        List<String[]> tsheet2 = Lists.newArrayList();
        List<String[]> tsheet3 = Lists.newArrayList();
        List<String[]> tsheet4 = Lists.newArrayList();

        int count = 0;


        for (Log log : list) {


            String[] firstSheet = new String[15];
            String poi = log.getNormalizeQuery();

            if (blackWords.contains(poi) || StringUtils.isEmpty(poi)) {
                continue;
            }

//            log.setTime(time);
            log.setBeginTime(beginTime);
            log.setEndTime(endTime);


            if (StringUtils.isNotEmpty(poi) && 1 == poi.length() && !WordUtils.containsSequentialAlphabeticNumeric(poi)) {
                continue;
            }


            Log logInfo = logStatisticsDao.getHotPoiInfo(poi);


            String city = logInfo.getCity();
            log.setCity(city);

            firstSheet[0] = poi;
            firstSheet[1] = city;
            firstSheet[2] = log.getNum().toString();
            Log userCount = logStatisticsDao.getHotPoiCount(log);
            firstSheet[3] = userCount.getNum().toString();

            Double validNum = NumberUtils.toDouble(log.getNum().toString());

            NumberFormat nt = NumberFormat.getPercentInstance();
            nt.setMinimumFractionDigits(2);
            Double oneRate = validNum / allcount;
            totalRate = totalRate + oneRate;


            firstSheet[4] = nt.format(oneRate);
            firstSheet[5] = nt.format(totalRate);


            List<Log> result = logStatisticsDao.getPoiTypeStatistic(log);

            for (Log logtype : result) {
                if (logtype.getType() == null) {
                    firstSheet[9] = logtype.getNum().toString();
                    continue;

                }
                if (logtype.getType() == 0) {
                    firstSheet[6] = logtype.getNum().toString();
                    continue;
                }
                if (logtype.getType() == 1) {
                    firstSheet[7] = logtype.getNum().toString();
                    continue;

                }
                if (logtype.getType() == 2) {
                    firstSheet[8] = logtype.getNum().toString();
                    continue;

                }

            }

            tsheet0.add(firstSheet);

            List<Log> type0 = queryPoiSource(log.getNormalizeQuery(), 0, time);
            String[] type0result = setTypeStatistic(type0, 1, poi, city);
            tsheet1.add(type0result);

            List<Log> type1 = queryPoiSource(log.getNormalizeQuery(), 1, time);
            String[] type2result = setTypeStatistic(type1, 2, poi, city);
            tsheet2.add(type2result);

            List<Log> type2 = queryPoiSource(log.getNormalizeQuery(), 2, time);
            String[] type3result = setTypeStatistic(type2, 3, poi, city);
            tsheet3.add(type3result);

            List<Log> typeNull = queryPoiSource(log.getNormalizeQuery(), null, time);
            String[] type4result = setTypeStatistic(typeNull, 4, poi, city);
            tsheet4.add(type4result);
            count++;
            System.out.println(count);

        }

        try {

            FileHandler.writeExcelFile(excelpath, templatePath, tsheet0, 0);
            FileHandler.writeExcelFile(excelpath, excelpath, tsheet1, 1);
            FileHandler.writeExcelFile(excelpath, excelpath, tsheet2, 2);
            FileHandler.writeExcelFile(excelpath, excelpath, tsheet3, 3);
            FileHandler.writeExcelFile(excelpath, excelpath, tsheet4, 4);


        } catch (Exception e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        logger.info("创建完成,用时{}", (end - start));
        return null;
    }

    /**
     * 热门poi数据统计
     *
     * @param time
     */
    public void hotPoiStatistic(String time, String city) {
        Double totalRate = 0.00;
        List<Log> hotlist = Lists.newArrayList();
        java.sql.Date date = DateUtils.getSqlDateByStr(time, DateUtils.ymd);
        Log querylog = new Log();
        querylog.setBeginTime(beginTime);
        querylog.setEndTime(endTime);
        List<Log> hotPoiListlist = logStatisticsDao.getHotPoiList(querylog);
        int allcount = logStatisticsDao.getAllCount(querylog).getNum();


        for (Log log : hotPoiListlist) {


            if (blackWords.contains(log.getNormalizeQuery())) {
                continue;
            }


            Double validNum = NumberUtils.toDouble(log.getNum().toString());

            Double oneRate = validNum / allcount;
            totalRate = totalRate + oneRate;
            log.setCity(city);
            log.setDate(date);
            hotlist.add(log);

            if (totalRate >= 0.80) {
                break;
            }

        }

        //批量插入占比80%的热门poi
        logStatisticsDao.hotPoiBatchInsert(hotlist);

    }


    private List<Log> queryPoiSource(String poi, Integer type, String time) {

        Log log = new Log();

        log.setNormalizeQuery(poi);
        log.setType(type);
        log.setBeginTime(beginTime);
        log.setEndTime(endTime);
        List<Log> result = logStatisticsDao.getPoiSourceStatistic(log);
        return result;

    }


    private String[] setTypeStatistic(List<Log> type, int sheet, String poi, String city) {

        String[] tsource = new String[25];

        tsource[0] = poi;
        tsource[1] = city;

        for (Log t : type) {

            if (t.getTsource() == null) {
                tsource[2] = t.getNum().toString();
                continue;
            }


            switch (t.getTsource()) {

                case 0:
                    tsource[3] = t.getNum().toString();
                    break;
                case 1:
                    tsource[4] = t.getNum().toString();
                    break;
                case 2:
                    tsource[5] = t.getNum().toString();
                    break;
                case 3:
                    tsource[6] = t.getNum().toString();
                    break;
                case 4:
                    tsource[7] = t.getNum().toString();
                    break;
                case 5:
                    tsource[8] = t.getNum().toString();
                    break;
                case 6:
                    tsource[9] = t.getNum().toString();
                    break;
                case 7:
                    tsource[10] = t.getNum().toString();
                    break;
                case 8:
                    tsource[11] = t.getNum().toString();
                    break;
                case 9:
                    tsource[12] = t.getNum().toString();
                    break;
                case 10:
                    tsource[13] = t.getNum().toString();
                    break;
                case 11:
                    tsource[14] = t.getNum().toString();
                    break;
                case 12:
                    tsource[15] = t.getNum().toString();

                    break;
                case 13:
                    tsource[16] = t.getNum().toString();

                    break;
                case 14:
                    tsource[17] = t.getNum().toString();

                    break;
                case 15:
                    tsource[18] = t.getNum().toString();

                    break;
                case 16:
                    tsource[19] = t.getNum().toString();

                    break;
                case 17:
                    tsource[20] = t.getNum().toString();

                    break;
                case 18:
                    tsource[21] = t.getNum().toString();

                    break;
                case 197:
                    tsource[22] = t.getNum().toString();

                    break;

            }

        }

        return tsource;


    }


    /**
     * 80%热门数据相等筛选
     */
    public void hotPoiOccupies() {

        long begin = System.currentTimeMillis();

        String excelpath = PropertiesUtils.getPropertiesValue("hotExcelPath") + begin + "hotPoi.xls";
        String templatePath = PropertiesUtils.getPropertiesValue("hotPoiTemplatePath");

        List<String> blackWord = Lists.newArrayList("停车场", "加油站");


        String firstWeekDay = "2015-12-05";
        String secondWeekDay = "2016-01-05";
        List<Log> hotPoiList = logStatisticsDao.hotPoiStatistic();
        long middle = System.currentTimeMillis();
        logger.info("查询用时,用时{}" , (middle - begin));
        int firstSearchCount = 0;
        int secondSearchCount = 0;
        Map<String, Log> firstWeek = Maps.newHashMap();
        Map<String, Log> secondWeek = Maps.newHashMap();

        List<Log> firstNoEqual = Lists.newArrayList();
        List<Log> secondNoEqual = Lists.newArrayList();


        for (Log log : hotPoiList) {

            String poi = log.getNormalizeQuery();

            if (StringUtils.isEmpty(poi)) {
                continue;
            }


            if (1 == poi.length() && !WordUtils.containsSequentialAlphabeticNumeric(poi)) {
                continue;
            }

//            if(blackWord.contains(poi)){
//                continue;
//            }

            java.sql.Date date = DateUtils.getSqlDateByStr(firstWeekDay, DateUtils.ymd);
            if (date.equals(log.getTime())) {
                firstWeek.put(log.getNormalizeQuery(), log);
            } else {
                secondWeek.put(log.getNormalizeQuery(), log);
            }
        }

        int firstCount = firstWeek.size();
        int secondCount = secondWeek.size();

        List<StatisticReport> equalPoi = Lists.newArrayList();


        for (String key : firstWeek.keySet()) {

            if (secondWeek.get(key) != null) {

                StatisticReport poi = new StatisticReport();
                Log firstLog = firstWeek.get(key);
                Log secondLog = secondWeek.get(key);

                poi.setHotPoi(key);
                poi.setFirstNum(firstLog.getNum());
                poi.setSecondNum(secondLog.getNum());
                poi.setTotalNum(firstLog.getNum() + secondLog.getNum());
                poi.setCity(firstLog.getCity());


                firstSearchCount += poi.getFirstNum();
                secondSearchCount += poi.getSecondNum();
                equalPoi.add(poi);

            } else {
                firstNoEqual.add(firstWeek.get(key));
            }
        }


        for (String key : secondWeek.keySet()) {
            if (firstWeek.get(key) == null) {
                secondNoEqual.add(secondWeek.get(key));
            }
        }


        Ordering<Log> ordering = Ordering.natural().nullsFirst().onResultOf(new Function<Log, Integer>() {
            public Integer apply(Log input) {
                if (input.getNum() == null) {
                    return null;
                }
                return input.getNum();
            }

        });
        List<Log> sortFirstNoEqual = ordering.reverse().immutableSortedCopy(firstNoEqual);

        List<Log> sortSecondNoEqual = ordering.reverse().immutableSortedCopy(secondNoEqual);


        Ordering<StatisticReport> statisticOrdering = Ordering.natural().nullsFirst().onResultOf(new Function<StatisticReport, Integer>() {
            public Integer apply(StatisticReport input) {
                if (input.getTotalNum() == null) {
                    return null;
                }
                return input.getTotalNum();
            }

        });

        List<StatisticReport> sortStatisticReport = statisticOrdering.reverse().immutableSortedCopy(equalPoi);


        int notEqualFirst = firstNoEqual.size();
        int notEqualSecond = secondNoEqual.size();
        int equalHot = equalPoi.size();
        long end = System.currentTimeMillis();

        Double firstRate = equalHot * 1.0 / firstCount;
        Double secondRate = equalHot * 1.0 / secondCount;

        NumberFormat nt = NumberFormat.getPercentInstance();
        nt.setMinimumFractionDigits(2);


        System.out.println("处理用时" + (end - middle));

        List<String[]> sheet0 = Lists.newArrayList();
        List<String[]> sheet1 = Lists.newArrayList();
        List<String[]> sheet2 = Lists.newArrayList();

        for (StatisticReport statistic : sortStatisticReport) {
            String[] report = new String[5];
            report[0] = statistic.getHotPoi();
            report[1] = statistic.getCity();
            report[2] = statistic.getFirstNum().toString();
            report[3] = statistic.getSecondNum().toString();
            report[4] = statistic.getTotalNum().toString();
            sheet0.add(report);
        }

        for (Log log : sortFirstNoEqual) {
            String[] report = new String[3];
            report[0] = log.getNormalizeQuery();
            report[1] = log.getCity();
            report[2] = log.getNum().toString();
            sheet1.add(report);
        }


        for (Log log : sortSecondNoEqual) {
            String[] report = new String[3];
            report[0] = log.getNormalizeQuery();
            report[1] = log.getCity();
            report[2] = log.getNum().toString();
            sheet2.add(report);
        }

        try {
            FileHandler.writeExcelFile(excelpath, templatePath, sheet0, 0);
            FileHandler.writeExcelFile(excelpath, excelpath, sheet1, 1);
            FileHandler.writeExcelFile(excelpath, excelpath, sheet2, 2);

        } catch (Exception e) {
            e.printStackTrace();
        }


        System.out.println("totalFirst:" + firstCount + " equal count:" + equalHot + "占比" + nt.format(firstRate) + "notEqualFirst" + notEqualFirst);
        System.out.println("totalSecond:" + secondCount + " equal count:" + equalHot + "占比" + nt.format(secondRate) + "notEqualSecond" + notEqualSecond);
        System.out.println("equalWord:第一期：" + firstSearchCount + "第二期数量" + secondSearchCount);

    }

    /**
     * 分类poi统计
     */
    public void catePoiStatistic() {

        String beginTime = PropertiesUtils.getPropertiesValue("beginTime");
        String endTime = PropertiesUtils.getPropertiesValue("endTime");

        List<Log> loglist = logStatisticsDao.catePoiStatistic();

        Map<String, String> cateInfo = basicDataService.loadCategoryInfo();


        String writePath = "D:\\logResult\\statisticCateInfo.txt";
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(writePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Log> logs = sortLogList(loglist);
        for (Log log : logs) {
            try {
                StringBuilder str = new StringBuilder();
                String query = log.getNormalizeQuery();
                Integer num = log.getNum();
                String cate = "";
                String source = "";
                if (cateInfo.get(query) == null) {
                    cate = "poi";
                } else {
                    cate = cateInfo.get(query);
                }

                if (log.getIsDidValid() == null) {
                    source = "key";
                } else {
                    source = "id";
                }

                str.append(query);
                str.append("\t");
                str.append(num);
                str.append("\t");
                str.append(source);
                str.append("\t");
                str.append(cate);

                writer.write(str.toString());
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("finish");

    }

    /**
     * 根据did进行统计
     */
    public void didPoiStatistic() {
        List<Log> logList = logStatisticsDao.didPoiStatistic();
        List<Log> logs = sortLogList(logList);

        Map<String, String> cateInfo = basicDataService.loadCategoryInfo();
        Map<String, Poi> extendAliasMap = basicDataService.loadPoiAliasByCity();
        Map<String, Poi> aliasMap = basicDataService.loadPoiByDid();
        String writePath = "D:\\logResult\\statisticDidCateInfo.csv";

        List<String[]> strList = Lists.newArrayList();

        for (Log log : logs) {

            String[] str = new String[8];
            String query = log.getNormalizeQuery();
            Integer num = log.getNum();
            String cate = "";
            String alias = "";
            String extendAlias = "";
            if (cateInfo.get(query) == null) {
                cate = "poi";
            } else {
                cate = cateInfo.get(query);
            }

            if (extendAliasMap.get(log.getDataId()) != null) {

                Poi poi = extendAliasMap.get(log.getDataId());
                extendAlias = poi.getExtendAlias();
            }

            if (aliasMap.get(log.getDataId()) != null) {
                Poi poi = aliasMap.get(log.getDataId());
                alias = poi.getAlias();
            }
            str[0] = log.getDataId();
            str[1] = query;
            str[2] = cate;
            str[3] = log.getCategory();
            str[4] = log.getSubCategory();
            str[5] = num.toString();
            str[6] = alias;
            str[7] = extendAlias;
            strList.add(str);
        }

        ExcelHandler.string2Csv(strList, writePath);
        System.out.println("finish");


    }

    private List<Log> sortLogList(List<Log> logList) {
        Map<String, List<Log>> cateMap = Maps.newHashMap();
        for (Log log : logList) {
            if (cateMap.get(log.getNormalizeQuery()) != null) {
                List<Log> logs = cateMap.get(log.getNormalizeQuery());
                logs.add(log);
            } else {
                List<Log> logs = Lists.newArrayList();
                logs.add(log);
                cateMap.put(log.getNormalizeQuery(), logs);
            }
        }

        List<Log> logs = Lists.newArrayList();
        int preNum = 0;
        for (String key : cateMap.keySet()) {
            List<Log> catList = cateMap.get(key);
            int num = 0;
            for (Log log : catList) {
                num += log.getNum();
            }
            if (num > preNum) {
                for (Log log : catList) {
                    logs.add(0, log);
                }
            } else {
                for (Log log : catList) {
                    logs.add(log);
                }
            }

            if (num > preNum) {
                preNum = num;
            }
        }
        return logs;

    }


    public static void main(String[] args) {
        LogStatisticsServiceImpl test = new LogStatisticsServiceImpl();

        test.catePoiStatistic();
    }

}
