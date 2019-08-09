package com.ztesoft.config.compare.utils;

import com.ztesoft.config.compare.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileUtil {
    private static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private static String CHARSET = "UTF-8";

    /**
     * 根据服务器建立对应的文件夹
     *
     * @param hostInfo 服务器信息
     * @return 文件夹路径
     */
    private static String makeLocalDir(HostInfo hostInfo) {
        String hostip = hostInfo.getHostIp();
//        是否需要在当前ip目录下创建当前日期的文件夹
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println(dateFormat.format(calendar.getTime()));
//        File basePath = new File("E:\\test\\");
//        if(!basePath.exists()) {
//            basePath.mkdirs();
//        }
        String targetPath = "E:\\Documents\\test\\" + hostInfo.getHostIp();
        File targetDir = new File(targetPath);
        System.out.println(targetPath);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        return targetPath;
    }

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
            return new String(fileContent, "UTF-8");
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
    public static String generatePropertiesFile(FileInfo fileInfo, String basePath, List<HostDetail> hostDetails) {
        logger.info("basePath: " + basePath);
        logger.info("=====begin to generate propertied file:" + fileInfo.getSource());
        logger.info("=====current method:" + fileInfo.getMethod());
        Map<String, String> sourceMap = DiffProperties.getPropertyMap(fileInfo.getSource());
        Map<String, String> targetMap = DiffProperties.getPropertyMap(basePath + fileInfo.getTarget());
        Map<String, String> valueMap = getValueMapFromString(fileInfo, hostDetails);
        Map<String, String> resultMap = new HashMap<>();
        if (fileInfo.getMethod() == 1) {
            logger.info("=====begin method 1 map merge:-----------------");
            resultMap = mergeMap(sourceMap, valueMap);
        } else if (fileInfo.getMethod() == 2) {
            logger.info("=====begin method 2 map merge:-----------------");
            Map<String, String> tempMap = mergeMap4Exist(sourceMap, targetMap);
            resultMap = mergeMap(tempMap, valueMap);
        }
        StringBuilder stringBuffer = new StringBuilder();
        String fileName = fileInfo.getTarget().substring(fileInfo.getTarget().lastIndexOf(File.separator));
        String tempPath = basePath + "tmp";
        System.out.println("====================================TempPath: " + tempPath);
        File file = new File(tempPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String FilePath = basePath + "tmp" + fileName;
//        todo clear temp dir
//        deleteDir(tempPath);
        for (Map.Entry<String, String> entry : resultMap.entrySet()) {
            stringBuffer.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        String content = stringBuffer.toString();
        logger.info("=====file content: " + content);
        fileName = writeContent2File(content, FilePath);
        logger.info("=====write properties file end===== fileName; " + fileName);
        return fileName;
    }

    /**
     * 根据map键值对，生成properties配置文件
     * key=value
     *
     * @param fileInfo 文件信息
     * @param basePath 文件目录
     */
    public static String generateIniFile(FileInfo fileInfo, String basePath, List<HostDetail> hostDetails) {
        logger.info("basePath: " + basePath);

        logger.info("=====begin to generate propertied file:" + fileInfo.getSource());
        logger.info("=====current method:" + fileInfo.getMethod());
        Map<String, Map<String, String>> sourceMap = FileReaderUtils.readIni(fileInfo.getSource());
        Map<String, String> targetMap = DiffProperties.getPropertyMap(basePath + fileInfo.getTarget());
        List<Map<String, String>> valueList = getIniValueMapFromString(fileInfo, hostDetails);
        Map<String, Map<String, String>> resultMap = new HashMap<>();
        if (fileInfo.getMethod() == 1) {
            logger.info("=====begin method 1 map merge:-----------------");
            resultMap = mergeIniMap(sourceMap, valueList);
        } else if (fileInfo.getMethod() == 2) {
            logger.info("=====begin method 2 map merge:-----------------");
//            Map<String, String> tempMap = mergeMap4Exist(sourceMap, targetMap);
//            resultMap = mergeMap(tempMap, valueMap);
            resultMap = mergeIniMap(sourceMap, valueList);
        }
        String fileName = fileInfo.getTarget().substring(fileInfo.getTarget().lastIndexOf(File.separator));
        String tempPath = basePath + "tmp";
//        todo clear temp dir
//        deleteDir(tempPath);
        System.out.println("====================================TempPath: " + tempPath);
        File file = new File(tempPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        String FilePath = basePath + "tmp" + fileName;
        generateIniFileByMap(resultMap, FilePath);
        logger.info("=====generateIniFile end===== fileName; " + FilePath);
        return FilePath;
    }

    private static Map<String, Map<String, String>> mergeIniMap(Map<String, Map<String, String>> sourceMap, List<Map<String, String>> valueList) {
        if(valueList.size() == 0) {
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
//        print(sourceMap);
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
        if(valueMap.size() == 0) {
            return sourceMap;
        }
        for (Map.Entry<String, String> entry : valueMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println("valueMap的键值对：" + key + "-----------" + value);
            if (sourceMap.containsKey(key) && value != null) {
                System.out.println("替换的键和值：" + key + "=====" + value);
                sourceMap.put(key, value);
            }
        }
        return sourceMap;
    }

    private static Map<String, String> getValueMapFromString(FileInfo fileInfo, List<HostDetail> hostDetails) {
        String str = fileInfo.getValueMap();
        Map<String, String> valueMap = new HashMap<>();
        if (str.length() == 0) {
            return valueMap;
        }
        String[] strArr = str.split("\\|");
        for (String s : strArr) {
            if(s.length() == 0 || !s.contains("=")) {
                break;
            }
            String[] temp = s.split("=");
            for (HostDetail hostDetail : hostDetails) {
                if (temp[1].equalsIgnoreCase(hostDetail.getKey())) {
                    valueMap.put(temp[0], hostDetail.getValue());
                    break;
                }
            }
        }
//        System.out.println("valueMap size: " +valueMap.size());
//        for (String string : valueMap.keySet()) {
//            System.out.println("键值对：" + string + "-----" + valueMap.get(string));
//        }
        return valueMap;
    }

    private static List<Map<String, String>> getIniValueMapFromString(FileInfo fileInfo, List<HostDetail> hostDetails) {
        String str = fileInfo.getValueMap();
        String[] strArr = str.split("\\|");
        List<Map<String, String>> tempList = new ArrayList<>(strArr.length);
        if (str.length() == 0) {
            return tempList;
        }
        for (String s : strArr) {
            if(s.length() == 0 || !s.contains("=")) {
                break;
            }
            String[] temp = s.split("=");
            String t1 = temp[0];
            String sectionName = t1.substring(t1.lastIndexOf("[") + 1, t1.indexOf(']'));
            String key = t1.substring(t1.indexOf(']') + 2);
            String valueName = temp[1];
            Map<String, String> map = new HashMap<>();
            for (HostDetail hostDetail : hostDetails) {
                if (valueName.equalsIgnoreCase(hostDetail.getKey())) {
                    map.put("sectionName", sectionName);
                    map.put("key", key);
                    map.put("value", hostDetail.getValue());
                    tempList.add(map);
                }
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
        if(content == null || content.length ==0 ){
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
////        String target = "E:\\Documents\\test\\127.0.0.1\\system.cfg";
//        String target = "E:\\Documents\\test\\127.0.0.1\\itcom.ini";
//        Map<String, Map<String, String>> map = FileReaderUtils.readIni(target);
////        map.put("connect_url","111");
////        map.put("connect_user","222");
////        map.put("spring.host.port","8888");
////        map.put("4","444");
//        String filename = "E:\\Documents\\test\\127.0.0.1\\111.txt";
////        File file = new File("E:\\Documents\\test\\127.0.0.1\\111.txt");
////        generatePropertiesFile(map,filename);
//        generateIniFile(map, filename);
//        String fileName = "\\home\\wyx\\target\\itemoc.ini";
//        fileName = fileName.substring(fileName.lastIndexOf(File.separator));
//        System.out.println(fileName);


//        String s = "test111=sss";
//        String[] temp = s.split("\\|");
//        System.out.println(temp.length);
//        System.out.println(temp[0]);

//        FileInfo fileInfo = new FileInfo();
//        fileInfo.setValueMap("[wuyaxiong].test=port");
//        HostDetail hostDetail = new HostDetail();
//        hostDetail.setKey("port");
//        hostDetail.setValue("8080");
//        HostDetail hostDetail1 = new HostDetail();
//        hostDetail1.setKey("hostIp");
//        hostDetail1.setValue("127.0.0.1");
//        List<HostDetail> hostDetails = new ArrayList<>();
//        hostDetails.add(hostDetail);
//        hostDetails.add(hostDetail1);
//        List<Map<String, String>> list = getIniValueMapFromString(fileInfo, hostDetails);
//        for (Map<String, String> map : list) {
//            System.out.println(map.get("sectionName"));
//            System.out.println(map.get("key"));
//            System.out.println(map.get("value"));
//
//        }
        deleteDir("E:\\Documents\\test");
        String name = "E:\\Documents\\test\\1111.txt";
        File file = new File(name);
        System.out.println(file.getParent());
    }
}
