package com.jerry.map.dao;

import com.jerry.map.model.Log;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.poi.ss.formula.functions.T;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/1/5.
 */
@Repository
public class LogStatisticsDao extends SqlSessionDaoSupport {


    @Resource
    private SqlSessionFactory sqlSessionFactory;


    /**
     * 获取热点poi数据列表
     *
     * @return
     */
    public List<Log> getHotPoiList(Log log) {

        return this.getSqlSession().selectList("getHotPoiList" ,log);

    }

    /**
     * 获取用户数量
     *
     * @param log
     * @return
     */
    public Log getHotPoiCount(Log log) {
        return this.getSqlSession().selectOne("getHotPoiCount", log);

    }


    /**
     * 获取query信息
     *
     * @param params
     * @return
     */
    public Log getHotPoiInfo(String params) {
        return this.getSqlSession().selectOne("getHotPoiInfo", params);

    }

    /**
     * 获取一天总量
     * @param log
     * @return
     */
    public Log getAllCount(Log log) {
        return this.getSqlSession().selectOne("getAllCount" , log);
    }


    /**
     * 获取不同类型数据列表
     *
     * @return
     */
    public List<Log> getPoiTypeStatistic(Log log) {

        return this.getSqlSession().selectList("getPoiTypeStatistic", log);

    }


    /**
     * 获取不同分类数据列表
     *
     * @return
     */
    public List<Log> getPoiSourceStatistic(Log log) {

        return this.getSqlSession().selectList("getPoiSourceStatistic", log);

    }


    /**
     * 批量插入分析日志
     * @param list
     */
    public void logBatchInsert(List<Log> list){

        String method = "logBatchInsert";
        batchInsert(method,list);
    }


    /**
     * 热门poi批量插入
     */
    public void hotPoiBatchInsert(List<Log> list){
        String method = "hotPoiBatchInsert";
        batchInsert(method,list);
    }


    /**
     * 无效poi批量插入
     * @param list
     */
    public void invalidLogBatchInsert(List<Log> list){
        String method = "invalidLogBatchInsert";
        batchInsert(method,list);
    }




    private void batchInsert(String method , List<Log> list){
        SqlSession batchSqlSession = null;
        try {

            batchSqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);//获取批量方式的sqlsession
            int batchCount = 1000;//每批commit的个数
            int batchLastIndex = batchCount;//每批最后一个的下标
            for(int index = 0; index < list.size();){
                if(batchLastIndex > list.size()){
                    batchLastIndex = list.size();
                    batchSqlSession.insert(method, list.subList(index, batchLastIndex));
                    batchSqlSession.commit();
                    System.out.println("index:"+index+"     batchLastIndex:"+batchLastIndex);
                    break;//数据插入完毕,退出循环

                }else{
                    batchSqlSession.insert(method, list.subList(index, batchLastIndex));
                    batchSqlSession.commit();
                    System.out.println("index:"+index+"     batchLastIndex:"+batchLastIndex);
                    index = batchLastIndex;//设置下一批下标
                    batchLastIndex = index + batchCount;
                }
            }
        }finally{
            batchSqlSession.close();
        }
    }

    /**
     * 获取所有热门数据
     *
     * @return
     */
    public List<Log> hotPoiStatistic() {
        return this.getSqlSession().selectList("hotPoiStatistic");
    }


    /**
     * 获取每天热门poi80%量
     * @return
     */
    public List<Log> hotPoiTimeStatistic(){
        return this.getSqlSession().selectList("hotPoiTimeStatistic");
    }


    /**
     * 分类统计poi频次
     * @return
     */
    public List<Log> catePoiStatistic(){
        return this.getSqlSession().selectList("catePoiStatistic");
    }

    /**
     * 根据有效id进行子类统计
     * @return
     */
    public List<Log> didPoiStatistic(){
        return this.getSqlSession().selectList("didPoiStatistic");
    }

    @Resource
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

}
