package com.ztesoft.config.compare.utils;

import com.alibaba.fastjson.JSON;
import com.ztesoft.config.compare.dto.ReplaceValue;
import com.ztesoft.config.compare.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;

public class FileUtil {
    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private static String CHARSET = "UTF-8";


    public static String file2String(String fileName) {
        logger.info("=====file2String begin===== filename; " + fileName);
        String encoding = "UTF-8";
        File file = new File(fileName);
        if (!file.exists()) {
            logger.warn("file is not exists: " + file.getName());
            return "";
        }
        long fileLength = file.length();
        logger.info("file length; " + fileLength);
        byte[] fileContent = new byte[(int) fileLength];
        try {
            FileInputStream fis = new FileInputStream(file);
            fis.read(fileContent);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(fileContent, CHARSET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            logger.info("Unsupported Encoding: " + "UTF-8");
            return "";
        }

    }


    /**
     * 根据配置信息生成配置文件，方便shell脚本调用
     * 文件每行格式 user@hostIp targetFile rootPath
     *
     * @param fileInfos 系统配置信息
     * @param hostInfo  服务器信息 user@127.0.0.1
     */
    public static String generateConfigFile(List<FileInfo> fileInfos, String hostInfo, String basePath) {
        File rootDir = new File(basePath);
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
        String fileName = getContentFormFile(fileInfos, basePath, hostInfo);
        return fileName;
    }

    /**
     * 根据map键值对，生成properties配置文件
     * key=value
     *
     * @param fileInfo 文件信息
     * @param basePath 文件目录
     */
    public static String generatePropertiesFile(FileInfo fileInfo, String basePath, Map<String, String> hostMap) {
        logger.info("basePath: " + basePath);
        logger.info("=====begin to generate propertied file:" + fileInfo.getSource());
        logger.info("=====current method:" + fileInfo.getMethod());
        Map<String, String> sourceMap = DiffProperties.getPropertyMap(fileInfo.getSource());
        Map<String, String> targetMap = DiffProperties.getPropertyMap(basePath + fileInfo.getTarget());
        Map<String, String> valueMap = getPropValueMapFromString(fileInfo, hostMap);
        Map<String, String> resultMap = new HashMap<>();
        printMap(valueMap);
        if (fileInfo.getMethod() == 1) {
            logger.info("=====begin method 1 map merge:-----------------");
            resultMap = mergeMap(sourceMap, valueMap);
        } else if (fileInfo.getMethod() == 2) {
            logger.info("=====begin method 2 map merge:-----------------");
            Map<String, String> tempMap = mergeMap4Exist(sourceMap, targetMap);
            resultMap = mergeMap(tempMap, valueMap);
        }
        StringBuilder stringBuffer = new StringBuilder();
        String FilePath = getTempFilePath(basePath, fileInfo);
        for (Map.Entry<String, String> entry : resultMap.entrySet()) {
            stringBuffer.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        String content = stringBuffer.toString();
//        logger.info("=====file content: " + content);
        String fileName = writeContent2File(content, FilePath);
        logger.info("=====write properties file end===== fileName; " + fileName);
        return fileName;
    }

    private static void printMap(Map<String, String> valueMap) {
        logger.info("begin to print value map : =========================");
        for (Map.Entry<String, String> entry : valueMap.entrySet()) {
            logger.info(entry.getKey() + "=======" + entry.getValue());
        }
    }

    /**
     * 根据map键值对，生成properties配置文件
     * key=value
     *
     * @param fileInfo 文件信息
     * @param basePath 文件目录
     */
    public static String generateIniFile(FileInfo fileInfo, String basePath, Map<String, String> hostMap) {
        logger.info("basePath: " + basePath);

        logger.info("=====begin to generate propertied file:" + fileInfo.getSource());
        logger.info("=====current method:" + fileInfo.getMethod());
        Map<String, Map<String, String>> sourceMap = FileReaderUtils.readIni(fileInfo.getSource());
        Map<String, Map<String, String>> targetMap = FileReaderUtils.readIni(fileInfo.getTarget());
        List<Map<String, String>> valueList = getIniValueMapFromString(fileInfo, hostMap);
        Map<String, Map<String, String>> resultMap = new HashMap<>();
        if (fileInfo.getMethod() == 1) {
            logger.info("=====begin method 1 map merge:-----------------");
            resultMap = mergeIniMap(sourceMap, valueList);
        } else if (fileInfo.getMethod() == 2) {
            logger.info("=====begin method 2 map merge:-----------------");
            Map<String, Map<String, String>> tempMap = mergeIniMap4Exists(sourceMap, targetMap);
            resultMap = mergeIniMap(tempMap, valueList);
        }
        String FilePath = getTempFilePath(basePath, fileInfo);
        generateIniFileByMap(resultMap, FilePath);
        logger.info("=====generateIniFile end===== fileName; " + FilePath);
        return FilePath;
    }

    private static Map<String, Map<String, String>> mergeIniMap4Exists(Map<String, Map<String, String>> sourceMap, Map<String, Map<String, String>> targetMap) {
        Map<String, Map<String, String>> result = new HashMap<>();
        Set<String> totalSet = new HashSet<>();
        totalSet.addAll(sourceMap.keySet());
        totalSet.addAll(targetMap.keySet());
        for (String key : totalSet) {
            if (sourceMap.containsKey(key) && !targetMap.containsKey(key)) {
                result.put(key, sourceMap.get(key));
            } else if (sourceMap.containsKey(key) && targetMap.containsKey(key)) {
                Map<String, String> sourceTemp = sourceMap.get(key);
                Map<String, String> targetTemp = targetMap.get(key);
                Set<String> tempSet = new HashSet<>();
                tempSet.addAll(sourceTemp.keySet());
                tempSet.addAll(targetTemp.keySet());
                for (String tempKey : tempSet) {
                    if (sourceTemp.containsKey(tempKey) && !targetTemp.containsKey(tempKey)) {
                        targetMap.put(tempKey, sourceMap.get(tempKey));
                    }
                }
                result.put(key, targetTemp);
            } else {
                result.put(key, targetMap.get(key));
            }
        }
        return result;
    }

    /**
     * 根据文件信息，创建临时文件父目录，返回临时文件路径
     *
     * @param fileInfo 文件信息
     * @return 临时文件路径
     */
    private static String getTempFilePath(String basePath, FileInfo fileInfo) {
        String tempPath = basePath + "tmp" + File.separator;
//        todo clear temp dir
//        deleteDir(tempPath);
        System.out.println("====================================TempPath: " + tempPath);
        File file = new File(tempPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String fileName = fileInfo.getTarget().substring(fileInfo.getTarget().lastIndexOf(File.separator) + 1);
        return tempPath + fileName;
    }

    private static Map<String, Map<String, String>> mergeIniMap(Map<String, Map<String, String>> sourceMap, List<Map<String, String>> valueList) {
        if (valueList == null || valueList.size() == 0) {
            return sourceMap;
        }
        for (Map<String, String> map : valueList) {
            String sectionName = map.get("sectionName");
            if (sourceMap.containsKey(sectionName)) {
                logger.info("find a sectionName: " + sectionName);
                Map<String, String> subsMap = sourceMap.get(sectionName);
                String key = map.get("key");
                logger.info("key-----------------`````" + key);
                if (subsMap.containsKey(key)) {
                    logger.info("find a key [" + sectionName + "]." + key + "=" + map.get("value"));
                    System.out.println(subsMap.get(key));
                    subsMap.put(key, map.get("value"));
                    System.out.println(subsMap.get(key));
                }
            }
        }
        return sourceMap;
    }

    private static void print(Map<String, Map<String, String>> sourceMap) {
        for (Map.Entry<String, Map<String, String>> entry : sourceMap.entrySet()) {
            System.out.println("[" + entry.getKey() + "]");
            Map<String, String> map = entry.getValue();
            for (Map.Entry<String, String> temp : map.entrySet()) {
                System.out.println(temp.getKey() + "=" + temp.getValue());
            }
        }
    }


    /**
     * 融合两个map，把target在source中不存在的值存入target，对于存在的值不足欧修改
     *
     * @param sourceMap 源端map
     * @param targetMap 目标端map
     * @return 结果map
     */
    private static Map<String, String> mergeMap4Exist(Map<String, String> sourceMap, Map<String, String> targetMap) {
        for (Map.Entry<String, String> entry : sourceMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!targetMap.containsKey(key) && value != null) {
                logger.info("插入目标端：" + key + "--------" + value);
                targetMap.put(key, value);
            }
        }
        return targetMap;
    }

    /**
     * 融合两个map，把valuemap中在source中存在的值存入target
     *
     * @param sourceMap 源端map
     * @param valueMap  目标端map
     * @return 结果map
     */
    private static Map<String, String> mergeMap(Map<String, String> sourceMap, Map<String, String> valueMap) {
        if (valueMap == null || valueMap.size() == 0) {
            return sourceMap;
        }
        for (Map.Entry<String, String> entry : valueMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println("valueMap的键值对：" + key + "-----------" + value);
            if (sourceMap.containsKey(key) && value != null) {
                logger.info("替换的键和值：" + key + "=====" + sourceMap.get(key) + "，替换后的值：" + value);
                sourceMap.put(key, value);
            }
        }
        return sourceMap;
    }

    private static Map<String, String> getPropValueMapFromString(FileInfo fileInfo, Map<String, String> hostMap) {
        String str = fileInfo.getValueMap();
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        List<ReplaceValue> replaceValues = JSON.parseArray(str, ReplaceValue.class);
        Map<String, String> valueMap = new HashMap<>();

        for (ReplaceValue replaceValue : replaceValues) {
            String attrName = replaceValue.getAttrName();
            if (hostMap.containsKey(attrName)) {
                valueMap.put(replaceValue.getKey(), hostMap.get(attrName));
            }
        }
        logger.info("valueMap size: " + valueMap.size());
        for (String string : valueMap.keySet()) {
            logger.info("键值对：" + string + "-----" + valueMap.get(string));
        }
        return valueMap;
    }

    private static List<Map<String, String>> getIniValueMapFromString(FileInfo fileInfo, Map<String, String> hostMap) {
        String str = fileInfo.getValueMap();
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        List<ReplaceValue> replaceValues = JSON.parseArray(str, ReplaceValue.class);
        List<Map<String, String>> tempList = new ArrayList<>(replaceValues.size());

        for (ReplaceValue replaceValue : replaceValues) {
            String attrName = replaceValue.getAttrName();
            Map<String, String> map = new HashMap<>();
            if (hostMap.containsKey(attrName)) {
                map.put("sectionName", replaceValue.getSectionName());
                map.put("key", replaceValue.getKey());
                map.put("value", hostMap.get(attrName));
                tempList.add(map);
            }
        }
//        for (Map<String, String> map : tempList) {
//            System.out.println(map.get("sectionName"));
//            System.out.println(map.get("key"));
//            System.out.println(map.get("value"));
//        }
        return tempList;
    }

    /**
     * 根据map键值对，生成properties配置文件
     * key=value
     *
     * @param map      键值对
     * @param FilePath 文件目录
     */
    public static String generateIniFileByMap(Map<String, Map<String, String>> map, String FilePath) {
        StringBuilder stringBuffer = new StringBuilder();
        for (Map.Entry<String, Map<String, String>> section : map.entrySet()) {
            stringBuffer.append("[").append(section.getKey()).append("]\n");
            for (Map.Entry<String, String> entry : section.getValue().entrySet()) {
                stringBuffer.append("    ").append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
            }
            stringBuffer.append("\n");
        }
        String content = stringBuffer.toString();
//        logger.info("=====file content: " + content);
        String fileName = writeContent2File(content, FilePath);
        logger.info("=====generate ini file end===== fileName; " + fileName);
        return fileName;
    }

    private static String getContentFormFile(List<FileInfo> fileInfos, String rootPath, String hostInfo) {
        StringBuilder stringBuffer = new StringBuilder();
        System.out.println(fileInfos.size());
        logger.info("file count: " + fileInfos.size());
        for (FileInfo fileInfo : fileInfos) {
            stringBuffer.append(hostInfo).append(" ").
                    append(rootPath).append(" ").
                    append(fileInfo.getTarget()).append("\n");
        }
        String content = stringBuffer.toString();
        FileOutputStream outputStream = null;
        OutputStreamWriter writer = null;
        String fileName = rootPath + File.separator + "config";
        String charset = "UTF-8";
        // 写字符换转成字节流
        try {
            outputStream = new FileOutputStream(fileName);
            writer = new OutputStreamWriter(
                    outputStream, charset);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("=====generateConfigFile end===== fileName; " + fileName);
        return fileName;
    }

    public static String writeContent2File(String content, String fileName) {
        FileOutputStream outputStream = null;
        OutputStreamWriter writer = null;
        // 写字符换转成字节流
        try {
            outputStream = new FileOutputStream(fileName);
            writer = new OutputStreamWriter(
                    outputStream, CHARSET);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("=====write content 2 file end===== fileName; " + fileName);
        return fileName;
    }

    //    public static Map<String, Object> getFileByDir(String dirName) {
    public static List<Map<String, Object>> getFileByDir(String dirName) {
        Map<String, Object> map = new HashMap<>();
        File file = new File(dirName);
        File files[] = file.listFiles();
        if (files == null) {
            map.put(dirName, null);
            return null;
        }
        if (files.length == 0) {
            System.out.println("no files found in: " + dirName);
        }
        List<Map<String, Object>> fileList = new ArrayList<>();
        for (File f : files) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("fileName", f.getName());
            temp.put("filePath", f.getAbsolutePath());
            temp.put("length", f.length());
            temp.put("content", file2String(f.getAbsolutePath()));
            fileList.add(temp);
        }
        return fileList;
//        map.put(dirName, fileList);
//        return map;
    }

    public static boolean deleteDir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        if (!file.isDirectory()) {
            return false;
        }

        String[] content = file.list();//取得当前目录下所有文件和文件夹
        if (content == null || content.length == 0) {
            return true;
        }
        for (String name : content) {
            File temp = new File(path, name);
            if (temp.isDirectory()) {//判断是否是目录
                deleteDir(temp.getAbsolutePath());//递归调用，删除目录里的内容
                temp.delete();//删除空目录
            } else {
                if (!temp.delete()) {//直接删除文件
                    System.err.println("Failed to delete " + name);
                }
            }
        }
        return true;
    }

    public static void main(String[] args) throws IOException {
//        FileInfo fileInfo = new FileInfo();
//        fileInfo.setTarget("C:\\Users\\wuyaxiong\\Documents\\targetimp.ini");
//        fileInfo.setSource("C:\\Users\\wuyaxiong\\Documents\\sourceimp.ini");
//        fileInfo.setMethod(2);
//        fileInfo.setValueMap("[KPI].Path=port");
//        Map<String,String> hostmap = new HashMap<>();
//        hostmap.put("port","8888");
//        String basepath = "C:\\Users\\wuyaxiong\\Downloads";
//        generateIniFile(fileInfo,basepath,hostmap);

//        String a = "[{fdsgsgd}]";
//        System.out.println(a.startsWith("[{") && a.endsWith("}]"));

    }
}
