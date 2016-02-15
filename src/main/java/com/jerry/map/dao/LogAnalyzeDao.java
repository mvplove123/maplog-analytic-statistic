package com.jerry.map.dao;

import com.jerry.map.model.Log;
import com.jerry.map.model.Poi;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/1/7.
 */
@Repository
public class LogAnalyzeDao extends SqlSessionDaoSupport {


    public Map<String ,String> queryWordByUidList(List<Integer> uidList){
        this.getSqlSession().selectMap("queryWordByUidList",uidList,"uid");
        return null;
    }


    public Map<String ,String> queryWordByDataidList(List<Integer> dataidList){
        return null;

    }


    /**
     * 查询指定城市下的poi
     * @param city
     * @return
     */
    public List<Poi> queryPoiByCity(String city){

       List<Poi> poiList = this.getSqlSession().selectList("queryPoiByCity",city);
        return  poiList;

    }

    /**
     * 查询poi扩展别名
     * @param city
     * @return
     */
    public List<Poi> queryPoiAliasByCity(String city){
        List<Poi> poiList = this.getSqlSession().selectList("queryPoiAliasByCity",city);
        return poiList;
    }


    @Resource
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }


}
