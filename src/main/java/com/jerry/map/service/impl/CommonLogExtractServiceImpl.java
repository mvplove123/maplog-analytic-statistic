package com.jerry.map.service.impl;

import com.jerry.map.service.AbstractService;
import com.jerry.map.service.LogExtractService;
import com.jerry.map.utils.FileHandler;
import com.jerry.map.utils.PropertiesUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 2016/2/2.
 */
@Service
public class CommonLogExtractServiceImpl extends AbstractService implements LogExtractService {
    @Override
    public void logExtractByCity() {


        final String city = PropertiesUtils.getPropertiesValue("city");
        String path = PropertiesUtils.getPropertiesValue("commonLogPath");
        ExecutorService service = Executors.newFixedThreadPool(5);

        File fileList = new File(path);
        File[] files = fileList.listFiles();

        for (final File file : files) {

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    extract(file, city);
                }
            });
            service.submit(thread);
        }
        try {
            service.awaitTermination(10l, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void extract(File file, String targetCity) {

        String filePath = file.getPath();
        String fileName = file.getName();
        long begin = System.currentTimeMillis();
        logger.info("{}文件，提取开始", fileName);

        BufferedReader result;
        BufferedWriter writer = null;

        String writePath = "D:\\logResult\\" + fileName;

        try {
            writer = new BufferedWriter(new FileWriter(writePath));
            result = FileHandler.getReader(filePath, "gb18030");
            String regEx = ".*what=(.*?)}?&.*city=(.*)$";//城市
            Pattern p = Pattern.compile(regEx);
            String line = null;

            while ((line = result.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    String city = m.group(2);
                    if (StringUtils.isNotEmpty(city) && targetCity.contains(city)) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
                long end = System.currentTimeMillis();
                logger.info("{}文件提取完成，用时{}", fileName, (end - begin));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
