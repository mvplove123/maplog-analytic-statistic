package com.jerry.map.controller;

import com.jerry.map.service.LogExtractService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by admin on 2016/2/2.
 */
@Controller
@RequestMapping("logExtract")
public class LogExtractController extends AbstractController {

    @Resource(name = "searchLogExtractServiceImpl")
    private LogExtractService searchLogExtractService;

    @Resource(name = "busLogExtractServiceImpl")
    private LogExtractService busLogExtractService;

    @Resource(name = "walkLogExtractServiceImpl")
    private LogExtractService walkLogExtractService;

    @ResponseBody
    @RequestMapping(value = "searchLogExtract")
    public Map<Object, Object> searchLogExtract(){

        searchLogExtractService.logExtractByCity();
        return dataJson("searchLog日志提取完成");

    }

    @ResponseBody
    @RequestMapping(value = "busLogExtract")
    public Map<Object, Object> busLogExtract(){

        busLogExtractService.logExtractByCity();
        return dataJson("busLog日志提取完成");

    }

    @ResponseBody
    @RequestMapping(value = "walkLogExtract")
    public Map<Object, Object> walkLogExtract(){

        walkLogExtractService.logExtractByCity();
        return dataJson("walkLog日志提取完成");

    }
}
