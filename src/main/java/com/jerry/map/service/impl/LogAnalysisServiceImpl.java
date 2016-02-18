package com.jerry.map.service.impl;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.jerry.map.dao.LogStatisticsDao;
import com.jerry.map.model.Log;
import com.jerry.map.service.AbstractService;
import com.jerry.map.service.LogAnalysisService;
import com.jerry.map.utils.SimilarityEnum;
import com.jerry.map.utils.WordUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by admin on 2016/2/16.
 */
@Service
public class LogAnalysisServiceImpl extends AbstractService implements LogAnalysisService {


    @Resource
    private LogStatisticsDao logStatisticsDao;
    /**
     * 分词
     */
    public void splitWord() {

        final List<Log> hotPoiList = logStatisticsDao.hotPoiStatistic();

        ExecutorService service = Executors.newFixedThreadPool(20);

        final Map<String, List<Log>> hotmap = Maps.newConcurrentMap();
        long begin = System.currentTimeMillis();
        final AtomicInteger count = new AtomicInteger(0);
        for (final Log targetLog : hotPoiList) {
            count.getAndIncrement();
            System.out.println(count.get());
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    String targetQuery = targetLog.getNormalizeQuery();

                    List<Log> commonList = Lists.newArrayList();
                    for (final Log log : hotPoiList) {

                        String query = log.getNormalizeQuery();

                        if (!targetQuery.equals(query)) {

                            SimilarityEnum similar = WordUtils.getSimilarity(targetQuery, query);

                            if (!similar.isSubstring()) {
                                continue;
                            }

                            Set<String> commonWords = WordUtils.getCommonSubstrings(targetQuery, query, 2);
                            if (commonWords.size() > 0) {
                                log.setNum(commonWords.size());
                                commonList.add(log);
                            }
                        }

                        Ordering<Log> ordering = Ordering.natural().nullsFirst().onResultOf(new Function<Log, Integer>() {
                            public Integer apply(Log input) {
                                if (input.getNum() == null) {
                                    return null;
                                }
                                return input.getNum();
                            }

                        });
                        List<Log> sortCommonList = ordering.reverse().immutableSortedCopy(commonList);
                        hotmap.put(targetQuery, sortCommonList);
                    }
                }
            });
            service.submit(thread);

        }

        service.shutdown();

        try {
            service.awaitTermination(1l, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        logger.info("统计完成，用时{}",(end - begin));


    }

}
