package com.jerry.map.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jerry.map.model.Log;
import com.jerry.map.model.Poi;
import com.jerry.map.service.AbstractService;
import com.jerry.map.service.BasicDataService;
import com.jerry.map.service.LogExtractService;
import com.jerry.map.utils.Constants;
import com.jerry.map.utils.PropertiesUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
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

    @Resource
    private CommonLogParseServiceImpl commonLogParseService;

    @Resource
    private BasicDataService basicDataService;


    String regToId = ".*to=.*uid:(.*?)[$&].*cps=.*";//query id
    String regToName = ".*to=name:(.*?)[$&].*cps=.*";//query Name

    String regFromId = ".*from=.*uid:(.*?)[$&].*maxwalk.*";
    String regFromName = ".*from=.*name:(.*?)[$&].*maxwalk.*";

    List<String> blackWords = Lists.newArrayList("我的位置");

    /**
     * bus log extract
     */
    public void logExtractByCity() {

        String city = PropertiesUtils.getPropertiesValue("city");
        commonLogExtractService.logExtractByCity(city, "busLogPath", Constants.BUS_LOG);

    }

    /**
     * bus log parse
     *
     * @param line
     * @return
     */
    public List<Log> logParseByCity(String line, String targetCity, String logSource) {
        List<Log> logs = Lists.newArrayList();
        Log fromLog = new Log();
        Log toLog = new Log();
        fromLog.setLogSource(logSource);
        toLog.setLogSource(logSource);
//        Map<String, Poi> didMap = Maps.newHashMap();
//        Map<String, List<Poi>> queryMap = Maps.newHashMap();
        Map<String, Poi> didMap = basicDataService.loadPoiByDid();
        Map<String, List<Poi>> queryMap = basicDataService.loadPoiByQueryName();


        Pattern pFromId = Pattern.compile(regFromId);
        Pattern pFromName = Pattern.compile(regFromName);

        Matcher mFromId = pFromId.matcher(line);
        Matcher mFromName = pFromName.matcher(line);

        setValue(mFromId, mFromName, fromLog, didMap, queryMap);

        Pattern pToId = Pattern.compile(regToId);
        Pattern pToName = Pattern.compile(regToName);

        Matcher mToId = pToId.matcher(line);
        Matcher mToName = pToName.matcher(line);

        setValue(mToId, mToName, toLog, didMap, queryMap);


        commonLogParseService.logParse(line, targetCity, fromLog);
        commonLogParseService.logParse(line, targetCity, toLog);

        logs.add(fromLog);
        logs.add(toLog);
        return logs;
    }


    private void setValue(Matcher mId, Matcher mName, Log log, Map<String, Poi> didMap, Map<String, List<Poi>> queryMap) {
        if (mId.matches() && mName.matches()) {
            String did = mId.group(1);
            if (!commonLogParseService.setLogById(did, log, didMap)) {
                String orginalQuery = mName.group(1);
                if (blackWords.contains(orginalQuery) || !commonLogParseService.setLogByName(orginalQuery, log, queryMap)) {
                    log.setIsValid(false);
                }
            }

        } else if (!mId.matches() && mName.matches()) {
            String orginalQuery = mName.group(1);
            if (blackWords.contains(orginalQuery) || !commonLogParseService.setLogByName(orginalQuery, log, queryMap)) {
                log.setIsValid(false);
            }
        } else if (mId.matches() && !mName.matches()) {
            String did = mId.group(1);
            if (!commonLogParseService.setLogById(did, log, didMap)) {
                log.setIsValid(false);
            }
        } else {
            log.setIsValid(false);
        }
    }


    public static void main(String[] args) {

        String line = "[2016-01-01 23:51:45][INFO][REQ]<bus.transfer.plan:d427fea6bcc14f10b3b1e2ab0a0a39ee>to=name:$uid:1_D_20052560501&dt=1&cps=1&tactic=00&loc=1.2960502E7,4826254.0&from=name:我的位置$coord:1.2960502E7,4826254.0&maxwalk=10000&moblog=bsns:850,process:1,vn:7.2.0,dzid:0,mf:samsung,ps:10.32,op:460-00,m:352324077485744_E8:B4:C8:81:39:4F_17d342fb599e8b8c,ptype:sogou,net:wifi,ptoken:2591c1465172e591b6c0abe416f7232a,nd:0,density:3.0,os:Android5.1.1,openid:,v:70200000,u:1451568799767518,sid:3,md:SM-A8000,d:352324077485744,pd:1,apn:\"changyue\",loginid:,l:2,s:0,et:5,p:0,bt:0,timestamp:1451663505505,reqId:9ebe3637-2eda-4173-820f-1b22e1cdf5cd&city=北京";

        BusLogExtractServiceImpl test = new BusLogExtractServiceImpl();
        String city = "北京市";
        test.logParseByCity(line, city, "bus");

    }


}
