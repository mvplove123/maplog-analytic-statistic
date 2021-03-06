package com.jerry.map.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jerry.map.dao.LogStatisticsDao;
import com.jerry.map.model.Log;
import com.jerry.map.model.Poi;
import com.jerry.map.service.AbstractService;
import com.jerry.map.service.BasicDataService;
import com.jerry.map.service.LogExtractService;
import com.jerry.map.service.LogParseService;
import com.jerry.map.utils.Constants;
import com.jerry.map.utils.FileHandler;
import com.jerry.map.utils.PropertiesUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/1/7.
 */
@Service
public class LogParseServiceImpl extends AbstractService implements LogParseService {


    @Resource
    private LogStatisticsDao logStatisticsDao;

    @Resource
    private BasicDataService basicDataService;

    @Resource(name = "searchLogExtractServiceImpl")
    private LogExtractService searchLogExtractService;

    @Resource(name = "busLogExtractServiceImpl")
    private LogExtractService busLogExtractService;

    @Resource(name = "walkLogExtractServiceImpl")
    private LogExtractService walkLogExtractService;

    int count = 0;


    @Override
    public void logParse() throws IOException {

        long begintime = System.currentTimeMillis();
        String searchLogPath = PropertiesUtils.getPropertiesValue("searchLogPath");
        String busLogPath = PropertiesUtils.getPropertiesValue("busLogPath");
        String walkLogPath = PropertiesUtils.getPropertiesValue("walkLogPath");

        String city = PropertiesUtils.getPropertiesValue("city");
        String targetSerPath = searchLogPath + city;
        String targetBusPath = busLogPath + city;
        String targetWalkPath = walkLogPath + city;

        Map<Integer, String> pathMap = Maps.newHashMap();

        pathMap.put(Constants.SEARCH_LOG, targetSerPath);
        pathMap.put(Constants.BUS_LOG, targetBusPath);
        pathMap.put(Constants.WALK_LOG, targetWalkPath);


        List<Poi> words = Lists.newArrayList();
        Poi test1 = new Poi();
        test1.setDataId("1_D1000133541965");
        words.add(test1);

        for (Integer logSource : pathMap.keySet()) {

            String path = pathMap.get(logSource);

            File fileList = new File(path);
            File[] files = fileList.listFiles();

            if(files == null){
                continue;
            }

            for (File file : files) {
                String filePath = file.getPath();
                String fileName = file.getName();
                logger.info("{}文件，logSource 类型{}解析开始", fileName,logSource);
                readFile(filePath, logSource, city);
            }
            long end1time = System.currentTimeMillis();
            logger.info("所有文件，解析结束，用时：{}", (end1time - begintime));

        }

    }


    /**
     * 读取文件
     *
     * @param filePath
     * @return
     */
    private List<String> readFile(String filePath, Integer logSource, String targetCity) {
        Map<String, String> cateMap = basicDataService.loadCategoryInfo();

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

                List<Log> logs = null;

                switch (logSource) {
                    case 0:
                        logs = searchLogExtractService.logParseByCity(line, targetCity,"search");
                        break;
                    case 1:
                        logs = busLogExtractService.logParseByCity(line, targetCity,"bus");
                        break;
                    case 2:
                        logs = walkLogExtractService.logParseByCity(line, targetCity,"walk");
                        break;
                }

                if (CollectionUtils.isEmpty(logs)) {
                    continue;
                }

                for(Log log : logs){

                    log.setDetail(line);
                    if (log.getIsValid() != null && !log.getIsValid()) {
                        invalidLogs.add(log);
                        continue;
                    }
                    validLogs.add(log);
                }
            }
            result.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        long insertTime = System.currentTimeMillis();

        for (Log log : validLogs) {

            if (cateMap.get(log.getNormalizeQuery()) != null) {
                log.setClassify(cateMap.get(log.getNormalizeQuery()));
            } else {
                log.setClassify(Constants.POI);
            }

        }


        //批量插入有效数据
        logStatisticsDao.logBatchInsert(validLogs);
        //批量插入无效数据
        logStatisticsDao.invalidLogBatchInsert(invalidLogs);

        long insertFinishTime = System.currentTimeMillis();
        logger.info("插入用时:{}", (insertFinishTime - insertTime));

        return loglist;

    }


    public static void main(String[] args) {
        LogParseServiceImpl test = new LogParseServiceImpl();
        try {


            test.logParse();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
