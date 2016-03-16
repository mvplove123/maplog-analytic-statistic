package com.jerry.map.model;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import java.math.BigInteger;

/**
 * Created by admin on 2016/1/14.
 */
public class Poi {


    private BigInteger uniqueId;

    private String dataId;

    private String caption;

    private String category;

    private String subCategory;

    private String city;

    private String alias;

    private String address;

    private String point;

    private JGeometry geometry;

    private String extendAlias;

    private Integer isAliasFlag;

    private double lat;

    private double lng;

    private String keyWord;

    private String classify;


    public BigInteger getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(BigInteger uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public JGeometry getGeometry() {
        return geometry;
    }

    public void setGeometry(JGeometry geometry) {
        this.geometry = geometry;

        if (geometry != null) {
            double pt[] = geometry.getPoint();
            if (pt != null) {
                double x = pt[0];
                double y = pt[1];
                String point = String.valueOf(x) + "," + String.valueOf(y);
                setPoint(point);
            }
        }
    }

    public String getExtendAlias() {
        return extendAlias;
    }

    public void setExtendAlias(String extendAlias) {
        this.extendAlias = extendAlias;
    }

    public Integer getIsAliasFlag() {
        return isAliasFlag;
    }

    public void setIsAliasFlag(Integer isAliasFlag) {
        this.isAliasFlag = isAliasFlag;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public String getClassify() {
        return classify;
    }

    public void setClassify(String classify) {
        this.classify = classify;
    }
}
