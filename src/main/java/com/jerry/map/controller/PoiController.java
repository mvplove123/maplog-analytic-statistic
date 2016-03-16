package com.jerry.map.controller;

import com.jerry.map.service.PoiService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by admin on 2016/2/29.
 */
@Controller
@RequestMapping("poiCompute")
public class PoiController extends AbstractController {


    @Resource
    private PoiService poiService;

    @ResponseBody
    @RequestMapping(value = "computeDependPoi")
    public Map<Object, Object> computeDependPoi() {
        try {
            poiService.computeDependPoi("北京市");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataJson("poi依赖统计完成");
    }


    @ResponseBody
    @RequestMapping(value = "exportPoiData")
    public Map<Object, Object> exportPoiData() {
        poiService.exportPoiData();
        return dataJson("数据导出完成");
    }

}
