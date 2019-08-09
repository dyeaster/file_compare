package com.ztesoft.config.compare.utils;

import com.ztesoft.config.compare.entity.HostInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseUtil {
    private ResponseUtil() {
    }

    public static Map<String, Object> renderTableResponse(List list) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 200);
        map.put("message", "");
        map.put("total", list.size());
        map.put("item", list);
        return map;
    }

    public static Map<String, Object> renderResponse(Integer code, String msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("message", msg);
        return map;
    }
}
