package com.jerry.map.model;

/**
 * Created by admin on 2016/1/5.
 */
public class StatisticReport {

    private String time;
    private String hotPoi;
    private String city;

    private Integer firstNum;

    private Integer secondNum;

    private Integer totalNum;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getHotPoi() {
        return hotPoi;
    }

    public void setHotPoi(String hotPoi) {
        this.hotPoi = hotPoi;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getFirstNum() {
        return firstNum;
    }

    public void setFirstNum(Integer firstNum) {
        this.firstNum = firstNum;
    }

    public Integer getSecondNum() {
        return secondNum;
    }

    public void setSecondNum(Integer secondNum) {
        this.secondNum = secondNum;
    }

    public Integer getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(Integer totalNum) {
        this.totalNum = totalNum;
    }
}
