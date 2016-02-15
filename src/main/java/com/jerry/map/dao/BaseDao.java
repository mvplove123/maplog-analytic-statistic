package com.jerry.map.dao;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.poi.ss.formula.functions.T;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.beans.factory.annotation.Autowired;
public abstract class BaseDao extends SqlSessionDaoSupport {
 //保护类型，子类可直接访问
 protected T mapper;

    @Resource
    private SqlSessionFactory sqlSessionFactory;

    private SqlSession batchSession;


    private Integer BATCH_DEAL_NUM=0;
    @PostConstruct
    public void SqlSessionFactory() {
        super.setSqlSessionFactory(sqlSessionFactory);
    }


    public int batchInsert(String statement, List<?> list) {
        batchSession = sqlSessionFactory.openSession(ExecutorType.BATCH, false);
        int i = 0;
        for(int cnt = list.size(); i < cnt; i++) {
            batchSession.insert(statement, list.get(i));
            if((i + 1) % BATCH_DEAL_NUM == 0) {//Constants.BATCH_DEAL_NUM为批量提交的条数
                batchSession.flushStatements();
            }
        }
        batchSession.flushStatements();
        batchSession.close();
        return i;
    }

}