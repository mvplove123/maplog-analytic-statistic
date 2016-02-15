package com.jerry.map.utils;

import com.google.common.io.Files;
import org.apache.commons.io.Charsets;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

public class FileCommonUtils {

    /**
     * 读取文件
     * @param filePath
     * @param decodeCharset
     * @return
     */
    public static List<String> fileRead(String filePath, Charset decodeCharset) {

        File file = new File(filePath);
        List<String> fileLines=null;
        try {
            fileLines = Files.readLines(file, decodeCharset == null ? decodeCharset : Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileLines;

    }
    
    /**
     * 写入文件
     * @param fileName
     * @param fileContent
     */
    public static void fileWrite(String fileName , String fileContent){
        
        
        File file = new File(fileName);
        
        try {
            Files.write(fileContent.getBytes(), file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        
    }


    //根据key读取value
    public static String readValue(String filePath,String key) {
        Properties props = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(filePath));
            props.load(in);
            String value = props.getProperty (key);
            System.out.println(key+value);
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
