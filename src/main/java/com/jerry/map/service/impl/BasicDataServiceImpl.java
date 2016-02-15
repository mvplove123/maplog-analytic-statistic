package com.jerry.map.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jerry.map.dao.LogAnalyzeDao;
import com.jerry.map.model.Poi;
import com.jerry.map.service.AbstractService;
import com.jerry.map.service.BasicDataService;
import com.jerry.map.utils.PropertiesUtils;
import com.jerry.map.utils.WordUtils;
import oracle.spatial.geometry.JGeometry;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.springframework.stereotype.Service;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.annotation.Resource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by admin on 2016/1/25.
 */
@Service
public class BasicDataServiceImpl extends AbstractService implements BasicDataService {

    @Resource
    private LogAnalyzeDao logAnalyzeDao;

    public Map<String, String> cateMap = Maps.newHashMap();

    public Map<String, Poi> didMap = Maps.newHashMap();

    public Map<String, List<Poi>> queryMap = Maps.newHashMap();

    public Map<String, Poi> aliasPoiMap = Maps.newHashMap();

    /**
     * 加载excel分类信息
     *
     * @return
     */
    public Map<String, String> loadCategoryInfo() {

        if (MapUtils.isNotEmpty(cateMap)) {
            return cateMap;
        }

        String path = PropertiesUtils.getPropertiesValue("excelCategoryPath");
        readXlsx(path);
        return cateMap;
    }

    /**
     * 加载指定城市下poi别名
     *
     * @return
     */
    public Map<String, Poi> loadPoiAliasByCity() {

        if (MapUtils.isNotEmpty(aliasPoiMap)) {
            return aliasPoiMap;
        }
        long begin = System.currentTimeMillis();
        String city =PropertiesUtils.getPropertiesValue("city");
        logger.info("开始加载别名");
        queryPoiAliasByCity(city);
        logger.info("结束加载别名,用时"+(System.currentTimeMillis()-begin));
        return aliasPoiMap;
    }

    /**
     * 加载did对应的poi数据
     *
     * @return
     */
    public Map<String, Poi> loadPoiByDid() {

        if (MapUtils.isNotEmpty(didMap)) {
            return didMap;
        }
        long begin = System.currentTimeMillis();

        String city =PropertiesUtils.getPropertiesValue("city");

        logger.info("开始did加载poi数据");
        creatDidQueryMap(city);
        logger.info("结束did加载poi数据 {}", (System.currentTimeMillis()-begin));
        return didMap;
    }

    /**
     * 加载queryName对应的poi列表数据
     *
     * @return
     */
    public Map<String, List<Poi>> loadPoiByQueryName() {
        if (MapUtils.isNotEmpty(queryMap)) {
            return queryMap;
        }
        String city =PropertiesUtils.getPropertiesValue("city");
        creatDidQueryMap(city);
        return queryMap;
    }

    /**
     * 读取excel
     *
     * @param path
     * @return
     * @throws IOException
     */
    public void readXlsx(String path) {
        long begin = System.currentTimeMillis();
        logger.info("开始读取excel类别数据");
        try {

            process(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();
        logger.info("结束读取excel类别数据，用时 {}", (end - begin));
    }


    /**
     * Initiates the processing of the XLS workbook file to CSV.
     *
     * @throws IOException
     * @throws org.apache.poi.openxml4j.exceptions.OpenXML4JException
     * @throws javax.xml.parsers.ParserConfigurationException
     * @throws org.xml.sax.SAXException
     */
    public void process(String filename) throws Exception {

        InputStream stream = null;
        try {
            OPCPackage xlsxPackage = OPCPackage.open(filename);

            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(xlsxPackage);
            XSSFReader xssfReader = new XSSFReader(xlsxPackage);
            StylesTable styles = xssfReader.getStylesTable();
            XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
            while (iter.hasNext()) {
//                curRow = 0;
//                sheetIndex++;
                stream = iter.next();
                processSheet(styles, strings, stream);
                stream.close();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("读取文件失败，此文件可能已损坏，请参照模板重新上传！");
        } finally {
            if (null != stream) {
                stream.close();
            }
        }
    }


    /**
     * Parses and shows the content of one sheet
     * using the specified styles and shared-strings tables.
     *
     * @param styles
     * @param strings
     * @param sheetInputStream
     */
    public void processSheet(StylesTable styles, ReadOnlySharedStringsTable strings, InputStream sheetInputStream)
            throws IOException, ParserConfigurationException, SAXException {

        InputSource sheetSource = new InputSource(sheetInputStream);
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxFactory.newSAXParser();
        XMLReader sheetParser = saxParser.getXMLReader();
        int minColumns = 6;

        XssfSheetHandler handler = new XssfSheetHandler(styles, strings, minColumns);
        sheetParser.setContentHandler(handler);
        sheetParser.parse(sheetSource);
        Map<String, String> map = handler.getMap();
        cateMap.putAll(map);
    }


    private void queryPoiAliasByCity(String city) {

        List<Poi> poiList = logAnalyzeDao.queryPoiAliasByCity(city);

        for (Poi poi : poiList) {

            String key = poi.getDataId();
            aliasPoiMap.put(key, poi);
        }
    }


    private void creatDidQueryMap(String city) {
        long begintime = System.currentTimeMillis();

        logger.info("开去读取城市{}的poi全量数据",city);
        List<Poi> wordList = logAnalyzeDao.queryPoiByCity(city);
        logger.info("结束读取城市{}的poi全量数据，用时{}", city,(System.currentTimeMillis()-begintime));

        for (Poi word : wordList) {

            JGeometry jGeometry = word.getGeometry();
            if (jGeometry != null) {
                double pt[] = jGeometry.getPoint();
                if (pt != null) {
                    double x = pt[0];
                    double y = pt[1];
                    String point = String.valueOf(x) + "," + String.valueOf(y);
                    word.setPoint(point);
                }
            }

            String norCaption = WordUtils.normalize(word.getCaption());

            if (CollectionUtils.isNotEmpty(queryMap.get(norCaption))) {
                List<Poi> pois = queryMap.get(norCaption);
                pois.add(word);
            } else {
                List<Poi> pois = Lists.newArrayList();
                pois.add(word);
                queryMap.put(norCaption, pois);
            }

            if (word.getUniqueId() != null) {
                didMap.put(word.getUniqueId().toString(), word);
            }
            didMap.put(word.getDataId(), word);
        }

    }


    public void createFileDict(String city){

        try {
            BufferedWriter writer =  new BufferedWriter(new FileWriter("d:/result1.txt"));

            List<Poi> wordList = logAnalyzeDao.queryPoiByCity(city);



            for (Poi word : wordList) {

                JGeometry jGeometry = word.getGeometry();
                if (jGeometry != null) {
                    double pt[] = jGeometry.getPoint();
                    if (pt != null) {
                        double x = pt[0];
                        double y = pt[1];
                        String point = String.valueOf(x) + "," + String.valueOf(y);
                        word.setPoint(point);
                    }
                }

                writer.write(word.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }



    }


    public static void main(String[] args) {
        BasicDataServiceImpl test = new BasicDataServiceImpl();
        Map<String, String> map = test.loadCategoryInfo();

        System.out.println(map);
    }


}
