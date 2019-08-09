package com.ztesoft.config.compare.utils;

import com.ztesoft.config.compare.entity.FileInfo;

import java.io.File;
import java.lang.reflect.Array;
import java.util.*;

public class FileInfoUtil {
    private FileInfoUtil() {
    }

    public static Map<String, String> transPropValueMap(String valueMap) {
        Map<String, String> map = new HashMap<>();
        String[] valueArr = valueMap.split("|");
        for (String str : valueArr) {
            String[] temp = str.split("=");
            String key = temp[0];
            String value = temp[1];
            map.put(temp[0], temp[1]);
        }

        return map;
    }

    public static List<String> getExcludeFile(FileInfo fileInfo) {
        String exclude = fileInfo.getExclude();
        if (exclude.length() == 0) {
            return null;
        }
        return Arrays.asList(exclude.split("\\|"));
    }

    public static FileInfo deleteEndSeparator(FileInfo fileInfo) {
        String target = fileInfo.getTarget();
        String source = fileInfo.getSource();
        if (target.endsWith(File.separator)) {
            fileInfo.setTarget(target.substring(0, target.length() - 1));
        }
        if (target.endsWith(File.separator)) {
            fileInfo.setSource(source.substring(0, source.length() - 1));
        }
        return fileInfo;
    }
}
