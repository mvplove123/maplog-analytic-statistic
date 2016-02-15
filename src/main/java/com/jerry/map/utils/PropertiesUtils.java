package com.jerry.map.utils;

import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Created by admin on 2016/1/11.
 */
public class PropertiesUtils {


    public static String getPropertiesValue(String key) {
        String value = "";
        Properties prop = new Properties();


        try {
            //读取加载属性文件properties
            InputStreamReader ir = new InputStreamReader(PropertiesUtils.class.getClassLoader().getResourceAsStream("config.properties"), "GBK");

            prop.load(ir);
            value = prop.getProperty(key).toString().trim();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;


    }


//
//    /**
//     * 自动加载配置文件机制，可在修改配置文件后，不用重启服务也能得到配置文件的新内容
//     */
//    public static String load2()throws Exception{
//        String file_name = ResolverUtil.Test.class.getClassLoader().getResource("config/config.properties").getFile();
//        Properties p = new Properties();
//        PropertiesConfiguration propconfig =null;//创建自动加载的机制
//        propconfig = new PropertiesConfiguration();
//        propconfig.setEncoding("UTF-8");//设置编码
//        propconfig.setReloadingStrategy(new FileChangedReloadingStrategy());//设置自动冲加载机制
//        p.load(new FileInputStream(file_name));
//        return p.getProperty("user_name").toString().trim();//每次调用这个方法都会从配置文件里取到最新的参数
//    }


}
