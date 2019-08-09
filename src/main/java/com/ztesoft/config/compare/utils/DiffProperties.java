package com.ztesoft.config.compare.utils;

import com.ztesoft.config.compare.dto.ContentValueInfo;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

public class DiffProperties {

    public static void main(String[] args) {
        try {
            String file1 = "D:\\code\\yrx\\yrx-supplier\\src\\test\\java\\com\\yrx\\supplier\\diff\\app1.properties";
            String file2 = "D:\\code\\yrx\\yrx-supplier\\src\\test\\java\\com\\yrx\\supplier\\diff\\app2.properties";
            DiffProperties.compareProperties(file1, file2);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * 比较两个properties文件
     *
     * @param sourceFile
     * @param targetFile
     */
    public static List<ContentValueInfo> compareProperties(String sourceFile, String targetFile) {
        Map<String, String> sourceValueMap = getPropertyMap(sourceFile);
        Map<String, String> targetValueMap = getPropertyMap(targetFile);
        List<ContentValueInfo> contentValueInfos = new ArrayList<>();
        Set<String> totalSet = new HashSet<>();
        totalSet.addAll(sourceValueMap.keySet());
        totalSet.addAll(targetValueMap.keySet());
        for (String key : totalSet) {
            String sourceValue = sourceValueMap.get(key);
            String targetValue = targetValueMap.get(key);
            ContentValueInfo contentValueInfo = new ContentValueInfo();
            contentValueInfo.setName(key);
            contentValueInfo.setSourceValue(sourceValue);
            contentValueInfo.setTargetValue(targetValue);
            if (targetValue != null && sourceValue != null) {
                contentValueInfo.setStatus(targetValue.equals(sourceValue) ? "1" : "2");
            } else if (sourceValue == null) {
                contentValueInfo.setStatus("4");
            } else {
                contentValueInfo.setStatus("3");
            }
            contentValueInfos.add(contentValueInfo);
        }
        return contentValueInfos;
    }

    /**
     * 获取文件中k&v
     *
     * @param file
     * @return
     */
    public static ArrayList<Property> getPropList(String file) {
        ArrayList<Property> pList = new ArrayList<Property>();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fileInputStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                //过滤注释
                if (!strLine.contains("#") && strLine.contains("=")) {
                    pList.add(new Property(strLine));
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pList;
    }

    public static Map<String, String> getPropertyMap(String file) {
        Map<String, String> map = new HashMap<>();
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fileInputStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                //过滤注释

                if (!strLine.contains("#") && strLine.contains("=")) {
                    String[] tmpArr = strLine.split("=", 2);
                    map.put(tmpArr[0].trim(), tmpArr[1].trim());
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    static class Property {
        private String key;
        private String value;

        public Property(String str) {
            super();
            str = str.trim();
            String[] tmpArr = str.split("=", 2);
            this.key = tmpArr[0].trim();
            this.value = tmpArr[1].trim();
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}