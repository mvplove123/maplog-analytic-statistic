package com.jerry.map.service.impl;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.jerry.map.dao.LogAnalyzeDao;
import com.jerry.map.dao.LogStatisticsDao;
import com.jerry.map.model.Log;
import com.jerry.map.model.Poi;
import com.jerry.map.service.AbstractService;
import com.jerry.map.service.BasicDataService;
import com.jerry.map.service.LogAnalyzeService;
import com.jerry.map.utils.*;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 2016/1/7.
 */
@Service
public class LogAnalyzeServiceImpl extends AbstractService implements LogAnalyzeService {

    @Resource
    private LogAnalyzeDao LogAnalyzeDao;

    @Resource
    private LogStatisticsDao logStatisticsDao;

    @Resource
    private BasicDataService basicDataService;

    int count = 0;


    @Override
    public void logParse() throws IOException {

        long begintime = System.currentTimeMillis();
        String path = PropertiesUtils.getPropertiesValue("logParsePath");



        List<Poi> words = Lists.newArrayList();
        Poi test1 = new Poi();
        test1.setDataId("1_D1000133541965");
        words.add(test1);



        File fileList = new File(path);
        File[] files = fileList.listFiles();

        for (File file : files) {
            String filePath = file.getPath();
            String fileName = file.getName();
            logger.info("{}文件，解析开始", fileName);
            List<String> extractResult = readFile(filePath);

            String writePath = "D:\\logResult\\" + fileName;
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(writePath));
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (String str : extractResult) {
                try {
                    writer.write(str);
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
        }
        long end1time = System.currentTimeMillis();
        logger.info("所有文件，解析结束，用时：",(end1time - begintime));
    }


    /**
     * 读取文件
     * @param filePath
     * @return
     */
    private List<String> readFile(String filePath) {

        BufferedReader result = null;
        try {
            result = FileHandler.getReader(filePath, "gb18030");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String line = null;
        List<String> loglist = Lists.newArrayList();
        List<Log> validLogs = new ArrayList<Log>();
        List<Log> invalidLogs = Lists.newArrayList();
        try {

            while ((line = result.readLine()) != null) {

                Log log = extractLog(line);
                if (log == null) {
                    continue;
                }
                log.setDetail(line);
                if (log.getIsValid() != null && !log.getIsValid()) {
                    invalidLogs.add(log);
                    continue;
                }
                validLogs.add(log);


//                String[] logStr = new String[8];
//
//                logStr[0] = StringUtils.defaultString(log.getId(),"");
//                logStr[1] = StringUtils.defaultString(log.getUserId(),"");
//                if (null == log.getType()) {
//                    logStr[2] = "";
//
//                } else {
//                    logStr[2] = log.getType().toString();
//
//                }
//                logStr[3] = StringUtils.defaultString(log.getCity(),"");
//                logStr[4] = StringUtils.defaultString(log.getQuery(),"");
//                if (null == log.getTsource()) {
//                    logStr[5] = "";
//                } else {
//                    logStr[5] = log.getTsource().toString();
//                }
//                logStr[6] = StringUtils.defaultString(log.getTime(),"");
//
//                logStr[7] = StringUtils.defaultString(line,"");
//                log.setDetail(line);
//
//                String filelog = Joiner.on("|").join(logStr);
//                count++;
//                System.out.println(count);
//                loglist.add(filelog);


            }
            result.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        long insertTime = System.currentTimeMillis();
        Map<String, String> cateMap = basicDataService.loadCategoryInfo();

        for (Log log : validLogs) {

            if (cateMap.get(log.getNormalizeQuery()) != null) {
                log.setClassify(cateMap.get(log.getNormalizeQuery()));
            } else {
                log.setClassify(ExcelCommon.POI);
            }

        }


        //批量插入有效数据
        logStatisticsDao.logBatchInsert(validLogs);
        //批量插入无效数据
        logStatisticsDao.invalidLogBatchInsert(invalidLogs);

        long insertFinishTime = System.currentTimeMillis();
        logger.info("插入用时:",(insertFinishTime - insertTime));

        return loglist;

    }

    /**
     * 解析日志
     *
     * @param line
     * @return
     */
    private Log extractLog(String line) {



        Map<String, Poi> didMap = basicDataService.loadPoiByDid();
        Map<String, List<Poi>> queryMap = basicDataService.loadPoiByQueryName();



        String regEx = ".*what=(.*?)}?&.*city=(.*)$";//城市
        String regExsearchKey = ".*keyword:(.*).*";//关键词
        String regExsearchType = ".*type:([0-9]).*";//类型
        String regExt = ".*,t:([0-9]+).*";//分类
        String regExU = ".*u:([0-9]{16,}).*";//用户id
        String regTime = ".*\\[([0-9]{4}-[0-9]{2}-[0-9]{2}).*";//日志时间
        String regVersion = ".*,v:([0-9]{5,}).*";//版本

        Pattern p = Pattern.compile(regEx);
        Pattern pkeyword = Pattern.compile(regExsearchKey);
        Pattern ptype = Pattern.compile(regExsearchType);
        Pattern ptcagerory = Pattern.compile(regExt);
        Pattern pUser = Pattern.compile(regExU);
        Pattern pTime = Pattern.compile(regTime);
        Pattern pVersion = Pattern.compile(regVersion);

        Matcher m = p.matcher(line);
        if (m.matches()) {

            Log log = new Log();

            String searchWord = m.group(1);
            Matcher mkeyowrd = pkeyword.matcher(searchWord);

            if (mkeyowrd.matches()) {//关键词解析

                String orginalQuery = mkeyowrd.group(1);
                String normalizeQuery = WordUtils.normalize(orginalQuery);
                log.setNormalizeQuery(normalizeQuery);
                List<Poi> pois = queryMap.get(normalizeQuery);
                if (CollectionUtils.isNotEmpty(pois) && pois.size()==1) {
                    //设置坐标
                    log.setPoint(pois.get(0).getPoint());
                }
                log.setOriginalQuery(mkeyowrd.group(1));
            } else if (searchWord.contains("id:")) {

                String did = "";
                if (searchWord.contains("$")) { //id 内$解析

                    String[] querys = searchWord.split("\\$");
                    did = querys[0].substring(3);
                    Poi poi = didMap.get(did);

                    if (poi != null) {
                        log.setOriginalQuery(poi.getCaption());
                        log.setPoint(poi.getPoint());
                        log.setIsDidValid(1);
                        log.setCategory(poi.getCategory());
                        log.setSubCategory(poi.getSubCategory());

                        log.setDataId(poi.getDataId());
                        log.setUniqueId(poi.getUniqueId().toString());

                    } else {
                        if (querys.length > 1) {
                            String originalQuery = querys[1];
                            List<Poi>pois  = queryMap.get(WordUtils.normalize(originalQuery));
                            if (CollectionUtils.isNotEmpty(pois) && pois.size()==1) {
                                //设置坐标
                                log.setPoint(pois.get(0).getPoint());
                            }
                            log.setOriginalQuery(originalQuery);
                            log.setIsDidValid(0);
                        }
                    }

                } else {//id 内id解析
                    did = searchWord.substring(3);
                    Poi poi = didMap.get(did);
                    if (poi != null && StringUtils.isNotEmpty(poi.getCaption())) {
                        String originalQuery = poi.getCaption();
                        log.setPoint(poi.getPoint());
                        log.setOriginalQuery(originalQuery);
                        log.setCategory(poi.getCategory());
                        log.setSubCategory(poi.getSubCategory());
                        log.setIsDidValid(1);
                        log.setDataId(poi.getDataId());
                        log.setUniqueId(poi.getUniqueId().toString());
                    } else {
                        log.setIsDidValid(0);
                        log.setIsValid(false);
                    }
                }
                log.setDid(did);


            } else {//非id，关键词其他类型解析

                log.setOriginalQuery(searchWord);
                log.setIsValid(false);
            }


            //解析城市city
            String city = m.group(2);
            log.setCity(city);

            //解析来源type
            Matcher mtype = ptype.matcher(line);
            if (mtype.matches()) {
                String type = mtype.group(1);
                log.setType(NumberUtils.toInt(type));

            }

            //解析分类t
            Matcher mtcageory = ptcagerory.matcher(line);
            if (mtcageory.matches()) {
                String tcategory = mtcageory.group(1);
                log.setTsource(NumberUtils.toInt(tcategory));

            }

            //解析用户id
            Matcher mUser = pUser.matcher(line);
            if (mUser.matches()) {
                String userId = mUser.group(1);
                log.setUserId(userId);
            }

            //解析时间
            Matcher mTime = pTime.matcher(line);
            if (mTime.matches()) {
                String time = mTime.group(1);
                java.sql.Date date = DateUtils.getSqlDateByStr(time, DateUtils.ymd);
                log.setDate(date);
            }


            Matcher mVersion = pVersion.matcher(line);
            if (mVersion.matches()) {
                String version = mVersion.group(1);
                log.setVersion(NumberUtils.toInt(version));
            }

            //规则化

            String normalizeQuery = WordUtils.normalize(log.getOriginalQuery());
            log.setNormalizeQuery(normalizeQuery);

            return log;
        }

        return null;
    }

    /**
     * 分词
     */
    public void splitWord() {

        final List<Log> hotPoiList = logStatisticsDao.hotPoiStatistic();

        ExecutorService service = Executors.newFixedThreadPool(20);

        final Map<String, List<Log>> hotmap = Maps.newConcurrentMap();
        long begin = System.currentTimeMillis();
        final AtomicInteger count = new AtomicInteger(0);
        for (final Log targetLog : hotPoiList) {
            count.getAndIncrement();
            System.out.println(count.get());
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    String targetQuery = targetLog.getNormalizeQuery();

                    List<Log> commonList = Lists.newArrayList();
                    for (final Log log : hotPoiList) {

                        String query = log.getNormalizeQuery();

                        if (!targetQuery.equals(query)) {

                            SimilarityEnum similar = WordUtils.getSimilarity(targetQuery, query);

                            if (!similar.isSubstring()) {
                                continue;
                            }

                            Set<String> commonWords = WordUtils.getCommonSubstrings(targetQuery, query, 2);
                            if (commonWords.size() > 0) {
                                log.setNum(commonWords.size());
                                commonList.add(log);
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
                        List<Log> sortCommonList = ordering.reverse().immutableSortedCopy(commonList);
                        hotmap.put(targetQuery, sortCommonList);
                    }
                }
            });
            service.submit(thread);

        }

        service.shutdown();

        try {
            service.awaitTermination(1l, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        logger.info("统计完成，用时{}",(end - begin));


    }

    public static void main(String[] args) {
        LogAnalyzeServiceImpl test = new LogAnalyzeServiceImpl();
        try {


            test.logParse();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
