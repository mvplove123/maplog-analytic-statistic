package com.jerry.map.service.impl;

import com.jerry.map.model.Log;
import com.jerry.map.model.Poi;
import com.jerry.map.service.AbstractService;
import com.jerry.map.service.BasicDataService;
import com.jerry.map.service.LogExtractService;
import com.jerry.map.utils.Constants;
import com.jerry.map.utils.DateUtils;
import com.jerry.map.utils.PropertiesUtils;
import com.jerry.map.utils.WordUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
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
    private BasicDataService basicDataService;

    /**
     * 搜索日志提取
     */
    public void logExtractByCity() {

        String city = PropertiesUtils.getPropertiesValue("city");
        commonLogExtractService.logExtractByCity(city,"searchLogPath", Constants.SEARCH_LOG);
    }

    /**
     * searh log word parse
     * @param line
     * @return
     */
    public Log logParseByCity(String line) {



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
}
