package com.jerry.map.service.impl;

import com.google.common.collect.Lists;
import com.jerry.map.model.Log;
import com.jerry.map.model.Poi;
import com.jerry.map.service.AbstractService;
import com.jerry.map.service.BasicDataService;
import com.jerry.map.service.LogExtractService;
import com.jerry.map.utils.Constants;
import com.jerry.map.utils.PropertiesUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 2016/2/15.
 */
@Service
public class SearchLogExtractServiceImpl extends AbstractService implements LogExtractService {

    @Resource
    private CommonLogExtractServiceImpl commonLogExtractService;

    @Resource
    private CommonLogParseServiceImpl commonLogParseService;

    @Resource
    private BasicDataService basicDataService;

    String regEx = ".*what=(.*?)}?&.*city=(.*)$";//城市
    String regExsearchKey = ".*keyword:(.*).*";//关键词
    String regExsearchType = ".*type:([0-9]).*";//类型
    String regExt = ".*,t:([0-9]+).*";//分类

    /**
     * 搜索日志提取
     */
    public void logExtractByCity() {

        String city = PropertiesUtils.getPropertiesValue("city");
        commonLogExtractService.logExtractByCity(city, "searchLogPath", Constants.SEARCH_LOG);
    }

    /**
     * searh log word parse
     *
     * @param line
     * @return
     */
    public List<Log> logParseByCity(String line, String targetCity ,String logSource) {


        Map<String, Poi> didMap = basicDataService.loadPoiByDid();
        Map<String, List<Poi>> queryMap = basicDataService.loadPoiByQueryName();

        Pattern p = Pattern.compile(regEx);
        Pattern pkeyword = Pattern.compile(regExsearchKey);
        Pattern ptype = Pattern.compile(regExsearchType);
        Pattern ptcagerory = Pattern.compile(regExt);

        Matcher m = p.matcher(line);
        if (m.matches()) {

            Log log = new Log();
            String searchWord = m.group(1);
            Matcher mkeyowrd = pkeyword.matcher(searchWord);

            if (mkeyowrd.matches()) {//关键词解析
                String orginalQuery = mkeyowrd.group(1);
                commonLogParseService.setLogByName(orginalQuery, log, queryMap);

            } else if (searchWord.contains("id:")) {

                if (searchWord.contains("$")) { //id 内$解析

                    String[] querys = searchWord.split("\\$");
                    String did = querys[0].substring(3);

                    if (!commonLogParseService.setLogById(did, log, didMap)) {
                        if (querys.length > 1) {
                            String originalQuery = querys[1];
                            commonLogParseService.setLogByName(originalQuery, log, queryMap);
                            log.setIsDidValid(0);
                        }
                    }
                } else {//id 内id解析
                    String did = searchWord.substring(3);
                    if (!commonLogParseService.setLogById(did, log, didMap)) {
                        log.setIsValid(false);
                    }
                }
            } else {//非id，关键词其他类型解析
                log.setOriginalQuery(searchWord);
                log.setIsValid(false);
            }
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

            log.setLogSource(logSource);
            commonLogParseService.logParse(line, targetCity, log);
            List<Log> logList = Lists.newArrayList(log);
            return logList;
        }
        return null;
    }
}
