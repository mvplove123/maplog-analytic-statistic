package com.jerry.map.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;


/**
 * Created by admin on 2016/1/5.
 */
public class Log {

    private Integer id;

    private String did;

    private String uniqueId;

    private String dataId;

    private String userId;

    private Integer type;

    private String city;

    private String originalQuery;

    private String normalizeQuery;
    private Integer isDidValid;

    private String point;
    private Integer tsource;

    private Date date;

    private Integer num;

    private String detail;

    private String beginTime;

    private String endTime;

    private Date time;

    private Boolean isValid;

    private String category;

    private String subCategory;

    private String classify;

    private Integer version;

    private String logSource;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getOriginalQuery() {
        return originalQuery;
    }

    public void setOriginalQuery(String originalQuery) {
        this.originalQuery = originalQuery;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getTsource() {
        return tsource;
    }

    public void setTsource(Integer tsource) {
        this.tsource = tsource;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }


    public Boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    public String getNormalizeQuery() {
        return normalizeQuery;
    }

    public void setNormalizeQuery(String normalizeQuery) {
        this.normalizeQuery = normalizeQuery;
    }


    public Integer getIsDidValid() {
        return isDidValid;
    }

    public void setIsDidValid(Integer isDidValid) {
        this.isDidValid = isDidValid;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public String getLogSource() {
        return logSource;
    }

    public void setLogSource(String logSource) {
        this.logSource = logSource;
    }
}
