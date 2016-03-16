package com.jerry.map.dao;

import com.google.common.collect.Maps;
import com.jerry.map.model.Poi;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/3/6.
 */
@Repository
public class PoiDao extends SqlSessionDaoSupport {


    @Resource
    private SqlSessionFactory sqlSessionFactory;

    /**
     * 查询poi总数
     * @return
     */
    public Integer queryPoiTotalCount(){
       return (Integer) this.getSqlSession().selectOne("queryPoiTotalCount");
    }

    /**
     * 查询某一页的poi数
     * @param page
     * @param start
     * @param end
     * @return
     */
    public List<Poi> queryPoiBypage(int page, int start, int end){

        Map<String,Integer> params = Maps.newHashMap();
        params.put("start",start);
        params.put("end",end);

        return this.getSqlSession().selectList("queryPoiBypage",params);


    }
    @Resource
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

}
