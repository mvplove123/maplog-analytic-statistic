package com.jerry.map.service.impl;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jerry.map.dao.LogAnalyzeDao;
import com.jerry.map.dao.PoiDao;
import com.jerry.map.model.Pagination;
import com.jerry.map.model.Poi;
import com.jerry.map.model.QuadTree;
import com.jerry.map.model.QuadTree.Box;
import com.jerry.map.model.QuadTree.Node;

import com.jerry.map.service.AbstractService;
import com.jerry.map.service.PoiService;
import com.jerry.map.utils.*;
import oracle.spatial.geometry.JGeometry;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by admin on 2016/2/29.
 */
@Service
public class PoiServiceImpl extends AbstractService implements PoiService {

    @Resource
    private LogAnalyzeDao logAnalyzeDao;

    @Resource
    private PoiDao poiDao;

    private static final double MAX_L2M = 55000.0;

    private static final double EARTH_RADIUS = 6378.137;

    private static int threadNumber = 10;

    private static final Object lock = new Object();

    List<Poi> allpoi = Lists.newArrayList();


    private QuadTree<Poi> poiQuadTree = null;

    Map<String, List<String>> poiMap = Maps.newConcurrentMap();


    @Override
    public void computeDependPoi(String city) throws Exception {
        String targetCity = city;
        if (StringUtils.isEmpty(targetCity)) {
            targetCity = PropertiesUtils.getPropertiesValue("city");

        }


        List<Poi> poiList = logAnalyzeDao.queryPoiByCity(targetCity);

//        List<Poi> poiList = new ArrayList<Poi>();
        String path = "D:\\100000bjpoi";
        try {
            BufferedReader bufferedReader = FileHandler.getReader(path, "gb18030");
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {

                String[] poistr = line.trim().split("\\t");

                Poi poi = new Poi();

                poi.setCaption(poistr[0]);
                poi.setCity(poistr[1]);
                poi.setPoint(poistr[2]);
                poiList.add(poi);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 对poi创建四叉树
        QuadTree<Poi> newTree = new QuadTree<Poi>();
        for (Poi poi : poiList) {

            if (poi.getPoint() != null) {
                String pt[] = poi.getPoint().split(",");
                if (pt != null) {

                    double x = Double.parseDouble(pt[0]);
                    double y = Double.parseDouble(pt[1]);
                    double[] result = Convertor_LL_Mer.Mer2LL(x, y);


                    poi.setLat(result[0]);
                    poi.setLng(result[1]);
                    newTree.insert(poi.getLat(), poi.getLng(), poi);
                }
            }
        }
        poiQuadTree = newTree;
        ExecutorService service = Executors.newFixedThreadPool(threadNumber);

        List<Future<Boolean>> futureList = new ArrayList<Future<Boolean>>();

        List<String> poi1 = Lists.newArrayList();
        BufferedWriter writer = new BufferedWriter(new FileWriter("d:/testresult1.txt"));

        for (Poi poi : poiList) {
            if(!poi.getCaption().equals(("东高地"))){
                continue;
            }

            for(Poi comPoi : poiList){


                if(poi.getCaption().equals(comPoi.getCaption())){
                    continue;
                }


                String point = poi.getPoint();
                String comPoint = comPoi.getPoint();

                String distance = point+","+comPoint;
                double result = CheckUtil.getLen(distance);

                if(result<=5000.0){
                    poi1.add(comPoi.getCaption());
                    writer.write(comPoi.getCaption()+"\t"+comPoi.getPoint());
                    writer.newLine();
                }



            }
            writer.flush();
            writer.close();



            futureList.add(service.submit(new PoiComputeThread(poi)));
        }

        // check status,this will block until all finished
        // or error have to quit.
        for (Future<Boolean> future : futureList) {
            Boolean publisResult = future.get();
            if (!publisResult) {    // error
                service.shutdownNow();
                throw new Exception("exportPoiData error,terminal!");
            }
        }

        logger.info("export finished");

        System.out.println(poiMap);


    }


    // PoiInfoPageThread分页查询线程
    class PoiComputeThread implements Callable<Boolean> {
        private Poi poi;

        public PoiComputeThread(Poi poi) {
            this.poi = poi;
        }

        @Override
        public Boolean call() throws Exception {
            long t1 = System.currentTimeMillis();
            try {
                poiMatch(poi);
                Thread.sleep(50);
                long t2 = System.currentTimeMillis();
                logger.info(" 匹配完成,耗时:" + (t2 - t1) + "ms");
                return true;
            } catch (Exception e) {
                throw e;
            }
        }
    }


    public void poiMatch(Poi poi) {

//        logger.info("源poi：{}", poi.getCaption());
        Box box = makeBox(poi.getLat(), poi.getLng(), 5.0 * 1000);
        List<Node<Poi>> nodes = poiQuadTree.query(box);


        if (CollectionUtils.isNotEmpty(nodes)) {

            List<String> pois = Lists.newArrayList();
            for (Node n : nodes) {
                double d = Convertor_LL_Mer.DistanceLL(poi.getLat(), poi.getLng(), n.x, n.y);
                if (d <= 5000.0) {

                    Poi compoi = (Poi) n.s;
                    if (poi.getCaption().equals(compoi.getCaption())) {
                        continue;
                    }
                    List<Integer> matches = match(poi.getCaption(), compoi.getCaption());
                    if (CollectionUtils.isNotEmpty(matches)) {
                        pois.add(compoi.getCaption());
                        System.out.println("匹配成功");
                        for (Integer integer : matches) {
                            System.out.println("Match at: " + integer);
                        }
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(pois)) {
                poiMap.put(poi.getCaption(), pois);
            }

        }


    }


    @Override
    public void exportPoiData() {
        try {
            // 查询数据库获得总数
            int totalCount = poiDao.queryPoiTotalCount();
            Pagination pagination = new Pagination(totalCount, 100000);//1个线程10万条
            int totalPage = pagination.getPages();    // 总分页数

            ExecutorService service = Executors.newFixedThreadPool(threadNumber);
            List<Future<Boolean>> futureList = new ArrayList<Future<Boolean>>();
            for (int i = 1; i <= totalPage; i++) {
                pagination.setCurrentPage(i);
                int start = pagination.getStart() + 1;
                int end = pagination.getEnd() + 1;
                futureList.add(service.submit(new PoiInfoPageThread(i, start, end)));
            }
            // check status,this will block until all finished
            // or error have to quit.
            for (Future<Boolean> future : futureList) {
                Boolean publisResult = future.get();
                if (!publisResult) {    // error
                    service.shutdownNow();
                    throw new Exception("exportPoiData error,terminal!");
                }
            }

            logger.info("export finished");


        } catch (Exception e) {
            logger.info("异常", e);
        }

    }


    // PoiInfoPageThread分页查询线程
    class PoiInfoPageThread implements Callable<Boolean> {
        private int page, start, end;

        public PoiInfoPageThread(int page, int start, int end) {
            this.page = page;
            this.start = start;
            this.end = end;
        }

        @Override
        public Boolean call() throws Exception {
            long t1 = System.currentTimeMillis();
            logger.info("处理PoiInfoPageThread数据第【" + page + "】页导出开始");
            try {
                List<Poi> poiList = poiDao.queryPoiBypage(page, start, end);
                export2File(poiList, page);
                long t2 = System.currentTimeMillis();
                logger.info(page + " 导出:" + poiList.size() + ",耗时:" + (t2 - t1) + "ms");
                return true;
            } catch (Exception e) {
                throw e;
            }
        }
    }


    private void export2File(List<Poi> poiList, int page) throws IOException {
        synchronized (lock) {
            String path = "D:\\bjsgslog\\bsgsallLog";
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path, true), "GB18030");

            if (CollectionUtils.isNotEmpty(poiList)) {
                for (Poi poi : poiList) {

                    String name = poi.getCaption();
                    String city = poi.getCity();

                    if (StringUtils.isEmpty(name)) {
                        name = "";
                    }

//                    String point = poi.getPoint();

//                    String uniqueId = poi.getUniqueId().toString();
//                    String dataId = poi.getDataId();
//                    String keyWord = poi.getKeyWord();
//
//                    if(StringUtils.isEmpty(keyWord)){
//                        continue;
//                    }
//                    if(StringUtils.isNotEmpty(keyWord)){
//                        if(keyWord.startsWith(",")){
//                            keyWord=keyWord.substring(1);
//                        }
//                        if(keyWord.endsWith(",")){
//                            keyWord = keyWord.substring(0,keyWord.length()-1);
//                        }
//                    }

//                    String category = poi.getCategory();
//                    if(StringUtils.isEmpty(category)){
//                        category="";
//                    }
//                    String subCategory = poi.getSubCategory();
//                    if(StringUtils.isEmpty(subCategory)){
//                        subCategory="";
//                    }

//                    String address = poi.getAddress();
//                    if(StringUtils.isEmpty(address)){
//                        address="";
//                    }

                    String classify = poi.getClassify();

                    Joiner joiner = Joiner.on("\t");
                    String poiStr = joiner.join(new String[]{name, city,
                            classify});
                    writer.write(poiStr + "\n");

                }
                writer.flush();
                writer.close();
            }
        }


    }


    public static List<Integer> match(String pattern, String text) {
        List<Integer> matches = new ArrayList<Integer>();
        int m = text.length();
        int n = pattern.length();
        Map<Character, Integer> rightMostIndexes = preprocessForBadCharacterShift(pattern);
        int alignedAt = 0;
        while (alignedAt + (n - 1) < m) {
            for (int indexInPattern = n - 1; indexInPattern >= 0; indexInPattern--) {
                int indexInText = alignedAt + indexInPattern;
                char x = text.charAt(indexInText);
                char y = pattern.charAt(indexInPattern);
                if (indexInText >= m) break;
                if (x != y) {
                    Integer r = rightMostIndexes.get(x);
                    if (r == null) {
                        alignedAt = indexInText + 1;
                    } else {
                        int shift = indexInText - (alignedAt + r);
                        alignedAt += shift > 0 ? shift : 1;
                    }
                    break;
                } else if (indexInPattern == 0) {
                    matches.add(alignedAt);
                    alignedAt++;
                }
            }
        }
        return matches;
    }

    private static Map<Character, Integer> preprocessForBadCharacterShift(
            String pattern) {
        Map<Character, Integer> map = new HashMap<Character, Integer>();
        for (int i = pattern.length() - 1; i >= 0; i--) {
            char c = pattern.charAt(i);
            if (!map.containsKey(c)) map.put(c, i);
        }
        return map;
    }

    /**
     * 创建计算box
     *
     * @param x
     * @param y
     * @param distance
     * @return
     */
    public static Box makeBox(double x, double y, double distance) {
        double r = distance / MAX_L2M;
        return new Box(x - r, y - r, x + r, y + r);
    }


    public static double computeDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);

        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        return s;
    }

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }


    public static void main(String[] args) {


//        String point = "1.29462972736471E7,4835061.40605144";
//
//        String pt[] = point.split(",");
//        if (pt != null) {
//
//            double x = Double.parseDouble(pt[0]);
//            double y = Double.parseDouble(pt[1]);
//            double[] result = Convertor_LL_Mer.Mer2LL(x,y);
////            double x = Double.parseDouble(pt[0]) / 20037508.34 * 180;
////            double y = Double.parseDouble(pt[1]) / 20037508.34 * 180;
////            y = 180 / Math.PI * (2 * Math.atan(Math.exp(y * Math.PI / 180)) - Math.PI / 2);
//            System.out.println(result[0]+","+result[1]);
//        }

        PoiServiceImpl poiService = new PoiServiceImpl();
        try {
            poiService.computeDependPoi("北京");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
