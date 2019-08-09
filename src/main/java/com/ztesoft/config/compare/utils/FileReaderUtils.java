package com.ztesoft.config.compare.utils;

import com.ztesoft.config.compare.dto.ContentValueInfo;
import com.ztesoft.config.compare.entity.HostInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileReaderUtils {
    public static Logger logger = LoggerFactory.getLogger(FileReaderUtils.class);

    /**
     * 判断两个文件的内容是否相同，文件名要用绝对路径
     *
     * @param fileName1 ：文件1的绝对路径
     * @param fileName2 ：文件2的绝对路径
     * @return 相同返回true，不相同返回false
     */
    public static boolean isSameFile(String fileName1, String fileName2) {
        FileInputStream fis1 = null;
        FileInputStream fis2 = null;
        try {
            fis1 = new FileInputStream(fileName1);
            fis2 = new FileInputStream(fileName2);

            int len1 = fis1.available();//返回总的字节数
            int len2 = fis2.available();

            if (len1 == len2) {//长度相同，则比较具体内容
                //建立两个字节缓冲区
                byte[] data1 = new byte[len1];
                byte[] data2 = new byte[len2];

                //分别将两个文件的内容读入缓冲区
                fis1.read(data1);
                fis2.read(data2);

                //依次比较文件中的每一个字节
                for (int i = 0; i < len1; i++) {
                    //只要有一个字节不同，两个文件就不一样
                    if (data1[i] != data2[i]) {
                        System.out.println("文件内容不一样");
                        return false;
                    }
                }
                System.out.println("两个文件完全相同");
                return true;
            } else {
                //长度不一样，文件肯定不同
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {//关闭文件流，防止内存泄漏
            if (fis1 != null) {
                try {
                    fis1.close();
                } catch (IOException e) {
                    //忽略
                    e.printStackTrace();
                }
            }
            if (fis2 != null) {
                try {
                    fis2.close();
                } catch (IOException e) {
                    //忽略
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 判断两个文件的内容是否相同，文件名要用绝对路径
     *
     * @param fileName1 ：文件1的绝对路径
     * @param fileName2 ：文件2的绝对路径
     * @return 相同返回true，不相同返回false
     */
    public static boolean compareByLine(String fileName1, String fileName2) {
        List<ContentValueInfo> result = new ArrayList<>();
        List<String> contents1 = getStringListFromFile(fileName1);
        List<String> contents2 = getStringListFromFile(fileName2);
        boolean flag = true;
        int index = Math.max(contents1.size(), contents2.size());
        if(contents1.size() >= contents2.size()) {
            for (int i = 0; i < index; i++) {
                ContentValueInfo contentValueInfo = new ContentValueInfo();
                contentValueInfo.setName(String.valueOf(i));

//                if(i<contents2.size()) {
//
//                }
            }
        }
        return false;
    }

    public static List<String> getStringListFromFile(String fileName1) {
        List<String> result = new ArrayList<>();
        try {
            FileInputStream fileInputStream = new FileInputStream(fileName1);
            DataInputStream in = new DataInputStream(fileInputStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                result.add(strLine);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public static String getFileName(String sourceFile) {
        String[] arr = sourceFile.split(File.separator);
//        System.out.println(arr);
//        if (sourceFile.endsWith(File.separator)) {
//            return arr[arr.length - 2];
//        }
        String fileName = arr[arr.length - 1];
        return fileName;
    }

    /**
     * 去除ini文件中的注释，以";"或"#"开头，顺便去除UTF-8等文件的BOM头
     *
     * @param source
     * @return
     */
    private static String removeIniComments(String source) {
        String result = source;

        if (result.contains(";")) {
            result = result.substring(0, result.indexOf(";"));
        }

        if (result.contains("#")) {
            result = result.substring(0, result.indexOf("#"));
        }

        return result.trim();
    }

    public static Map<String, Map<String, String>> readIni(String filename) {
        Map<String, List<String>> listResult = new HashMap<>();
        Map<String, Map<String, String>> result = new HashMap<>();
        String globalSection = "global";

        File file = new File(filename);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String str = null;
            //处理缺省的section
            String currentSection = globalSection;
            List<String> currentProperties = new ArrayList<>();
            boolean lineContinued = false;
            String tempStr = null;

            //一次读入一行（非空），直到读入null为文件结束
            //先全部放到listResult<String, List>中
            while ((str = reader.readLine()) != null) {
                //去掉尾部的注释、去掉首尾空格
                str = removeIniComments(str).trim();

                if ("".equals(str)) {
                    continue;
                }

                //如果前一行包括了连接符'\'
                if (lineContinued) {
                    str = tempStr + str;
                }

                //处理行连接符'\'
                if (str.endsWith("\\")) {
                    lineContinued = true;
                    tempStr = str.substring(0, str.length() - 1);
                    continue;
                } else {
                    lineContinued = false;
                }

                //是否一个新section开始了
                if (str.startsWith("[") && str.endsWith("]")) {
                    String newSection = str.substring(1, str.length() - 1).trim();

                    //如果新section不是现在的section，则把当前section存进listResult中
                    if (!currentSection.equals(newSection)) {
                        listResult.put(currentSection, currentProperties);
                        currentSection = newSection;

                        //新section是否重复的section
                        //如果是，则使用原来的list来存放properties
                        //如果不是，则new一个List来存放properties
                        currentProperties = listResult.get(currentSection);
                        if (currentProperties == null) {
                            currentProperties = new ArrayList<>();
                        }
                    }
                } else {
                    currentProperties.add(str);
                }
            }
            //把最后一个section存进listResult中
            listResult.put(currentSection, currentProperties);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //整理拆开name=value对，并存放到MAP中：
        //从listResult<String, List>中，看各个list中的元素是否包含等号“=”，如果包含，则拆开并放到Map中
        //整理后，把结果放进result<String, Object>中
        for (String key : listResult.keySet()) {
            List<String> tempList = listResult.get(key);

            //空section不放到结果里面
            if (tempList == null || tempList.size() == 0) {
                continue;
            }
            //name=value对，存放在MAP里面
            if (tempList.get(0).contains("=")) {
                Map<String, String> properties = new HashMap<>();
                for (String s : tempList) {
                    int delimiterPos = s.indexOf("=");
                    //处理等号前后的空格
                    properties.put(s.substring(0, delimiterPos).trim(), s.substring(delimiterPos + 1, s.length()).trim());
                }
                result.put(key, properties);
            }
//            else { //只有value，则获取原来的list
//                result.put(key, listResult.get(key));
//            }
        }
        return result;
    }

    public static void main(String[] args) {
//        String fileName1 = "/home/wyx/source/dir2/itcom.ini";
//        String fileName2 = "E:\\Documents\\project\\filecompare\\src\\main\\resources\\test_1.properties";
//        String flag = FileReaderUtils.getFileType(fileName1);
//        System.out.println(flag);
    }
}
