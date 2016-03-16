package com.jerry.map.service.impl;

import com.jerry.map.model.Log;
import com.jerry.map.model.Poi;
import com.jerry.map.service.AbstractService;
import com.jerry.map.utils.DateUtils;
import com.jerry.map.utils.WordUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 2016/2/17.
 */
@Service
public class CommonLogParseServiceImpl extends AbstractService {


    String regExU = ".*u:([0-9]{16,}).*";//用户id
    String regTime = ".*\\[([0-9]{4}-[0-9]{2}-[0-9]{2}).*";//日志时间
    String regVersion = ".*,v:([0-9]{5,}).*";//版本

    public void logParse(String line, String targetCity, Log log) {

        Pattern pUser = Pattern.compile(regExU);
        Pattern pTime = Pattern.compile(regTime);
        Pattern pVersion = Pattern.compile(regVersion);

        //设置city
        log.setCity(targetCity);

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

        //解析版本
        Matcher mVersion = pVersion.matcher(line);
        if (mVersion.matches()) {
            String version = mVersion.group(1);
            log.setVersion(NumberUtils.toInt(version));
        }

        //规则化
        String normalizeQuery = WordUtils.normalize(log.getOriginalQuery());
        log.setNormalizeQuery(normalizeQuery);

    }

    /**
     * 通过did 设值
     * @param did
     * @param log
     * @param didMap
     * @return
     */
    public boolean setLogById(String did, Log log, Map<String, Poi> didMap) {


        Poi poi = didMap.get(did);
        log.setDid(did);
        if (poi != null && StringUtils.isNotEmpty(poi.getCaption())) {
            String originalQuery = poi.getCaption();
            log.setPoint(poi.getPoint());
            log.setOriginalQuery(originalQuery);
            log.setCategory(poi.getCategory());
            log.setSubCategory(poi.getSubCategory());
            log.setIsDidValid(1);
            log.setDataId(poi.getDataId());
            log.setUniqueId(poi.getUniqueId().toString());
            return true;
        } else {
            log.setIsDidValid(0);
            return false;
        }
    }


    /**
     * 通过name设值
     * @param orginalQuery
     * @param log
     * @param queryMap
     * @return
     */
    public boolean setLogByName(String orginalQuery, Log log, Map<String, List<Poi>> queryMap) {
        if (StringUtils.isEmpty(orginalQuery)) {
            return false;
        }
        String normalizeQuery = WordUtils.normalize(orginalQuery);
        log.setOriginalQuery(orginalQuery);
        log.setNormalizeQuery(normalizeQuery);
        List<Poi> pois = queryMap.get(normalizeQuery);
        if (CollectionUtils.isNotEmpty(pois) && pois.size() == 1) {
            //设置坐标
            log.setPoint(pois.get(0).getPoint());
            log.setCategory(pois.get(0).getCategory());
            log.setSubCategory(pois.get(0).getSubCategory());
            log.setDataId(pois.get(0).getDataId());
            log.setUniqueId(pois.get(0).getUniqueId().toString());
            log.setIsAliasValid(pois.get(0).getIsAliasFlag());
        }
        return true;
    }

}
