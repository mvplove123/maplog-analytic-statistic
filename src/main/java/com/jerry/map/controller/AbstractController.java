package com.jerry.map.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public abstract class AbstractController {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected static final String DEFAULT_PAGE = "1";
    protected static final int DEFAULT_PAGE_SIZE = 30;
    protected static final int DEFULT_SUGGEST_SIZE = 10;
    protected static final String START = "0";
    protected static final String LIMIT = "20";
    private static String version = "1";
    
    private Map<Object, Object> data(Object... data) {
        Map<Object, Object> map = new LinkedHashMap<Object, Object>();
        if (data.length % 2 != 0) {
            throw new IllegalArgumentException("parameter count must be even");
        }

        for (int i = 0; i < data.length; i += 2) {
            map.put(data[i], data[i + 1]);
        }
        return map;
    }

    protected Map<Object, Object> dataJson(Object data) {
        return data("ver", "1", "ret", true, "data", data);
    }

    protected Map<Object, Object> errorJson(Object message) {
        return data("ver", 1, "ret", false, "errmsg", message);
    }
    
    protected Map<Object, Object> dataFormJson(Object data) {
        return data("ver", 1, "ret", true, "data", data, "success", true);
    }

    protected Map<Object, Object> errorFormJson(Object message) {
        return data("ver", 1, "ret", false, "errmsg", message, "success", false);
    }

    protected Map<Object, Object> errorJson(Object message, Object errcode) {
        return data("ver", 1, "ret", false, "errmsg", message, "errcode", errcode);
    }
    
    public String getClientRealIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");// 公司统一用X-Real-IP
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");// 这是一个可以伪造的头
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
        }
        // 最后一个为RemoteAddr
        int pos = ip.lastIndexOf(',');
        if (pos >= 0) {
            ip = ip.substring(pos);
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }
    
    protected void addError(Map<String, Object> map, 
            String errorMsg, int errCode) {
        map.put("ret", false);
        map.put("data", new TreeMap<Object, Object>());
        map.put("errcode", errCode);
        map.put("errmsg", errorMsg);
        map.put("ver", getVersion());
    }
    
    protected void addSuccess(Map<String, Object> map, Object data) {
        map.put("ret", true);
        map.put("data", data);
        map.put("ver", getVersion());
    }
    
    protected String getVersion() {
        return version;
    }
}