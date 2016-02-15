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

    @Resource
    private LogExtractService logExtractService;

    @ResponseBody
    @RequestMapping(value = "commonLogExtract")
    public Map<Object, Object> commonLogExtract(){

        logExtractService.logExtractByCity();
        return dataJson("日志提取完成");


    }


}
