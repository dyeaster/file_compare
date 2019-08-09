package com.ztesoft.config.compare.service.impl;

import ch.ethz.ssh2.Connection;
import com.ztesoft.config.compare.dto.ContentValueInfo;
import com.ztesoft.config.compare.dto.FileValueInfo;
import com.ztesoft.config.compare.entity.*;
import com.ztesoft.config.compare.repository.FileInfoRepository;
import com.ztesoft.config.compare.repository.HostDetailRepository;
import com.ztesoft.config.compare.repository.HostInfoRepository;
import com.ztesoft.config.compare.repository.ProjectRepository;
import com.ztesoft.config.compare.service.FileService;
import com.ztesoft.config.compare.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class FileServiceImpl implements FileService {
    private static Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);
    @Autowired
    private FileInfoRepository fileInfoRepository;

    @Autowired
    private HostInfoRepository hostInfoRepository;

    @Autowired
    private HostDetailRepository hostDetailRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Override
    public Map<String, Object> syncFile2Server(FileInfo fileInfo) {
        HostInfo hostinfo = hostInfoRepository.getOne(fileInfo.getHostId());
        Project project = projectRepository.getOne(hostinfo.getProjectId());
        List<HostDetail> hostDetails = hostDetailRepository.findByHostId(fileInfo.getHostId());
        Map<String, Object> hostMap = HostUtil.hostDetailList2Map(hostDetails);
//        1. 首先判断文件类型，文件夹，文件（三种类型）
//        2. 根据不同类型的文件执行不同的处理
//        3. 文件根据valueMap生成临时文件在本地
//        4. 备份远程文件，传输本地文件到远程
//        5. 删除本地文件
        String bathPath = SysUtil.getBasePath(hostinfo, project.getName());
        String tempPath = bathPath + File.separator + "tmp";
        if (fileInfo.getDirFlag() == 1) {
            return this.scpDir2Sever(fileInfo, hostinfo, hostMap);
        }
        String localFile = null;
        switch (fileInfo.getType()) {
            case 1:
//                生成临时配置文件
//                fileInfo.setTarget(bathPath + fileInfo.getTarget());
                localFile = FileUtil.generatePropertiesFile(fileInfo, bathPath, hostDetails);
                break;
            case 2:
//                生成临时配置文件
//                fileInfo.setTarget(bathPath + fileInfo.getTarget());
                localFile = FileUtil.generateIniFile(fileInfo, bathPath, hostDetails);
                break;
            case 0:
                localFile = fileInfo.getSource();
                break;
            default:
                break;
        }
        //                传输到指定位置
        logger.info("localFile: " + localFile);
        String target = fileInfo.getTarget();
        String password = HostUtil.decryptDES((String) hostMap.get("password"));
        return scpFile2Server(localFile, target, (String) hostMap.get("hostIp"),
                (String) hostMap.get("user"), password);
    }

    private Map<String, Object> scpDir2Sever(FileInfo fileInfo, HostInfo hostinfo, Map<String, Object> hostMap) {
        Connection connection = new Connection(hostinfo.getHostIp());
        Map<String, Object> map = new HashMap<>();
        try {
            connection.connect();
            System.out.println("开始登录");
            boolean isAuthenticated = connection.authenticateWithPassword((String) hostMap.get("user"), (String) hostMap.get("password"));
            if (!isAuthenticated) {
                map.put("code", -1);
                map.put("msg", "connect to server failed.Authentication failed.");
                return map;
            }
        } catch (IOException e) {
            e.printStackTrace();
            map.put("code", -1);
            map.put("msg", "connect to server failed.");
            return map;
        }
        return ScpTools.uploadDir(fileInfo, connection, null);
    }

    private Map<String, Object> compareOther(FileInfo fileInfo) {
        logger.info("begin to compare other file: ==========================" + fileInfo.getTarget());
        Map<String, Object> map = new HashMap<>();
        String sourceFileName = fileInfo.getSource();
        String targetFileName = fileInfo.getTarget();
        map.put("result", FileReaderUtils.isSameFile(sourceFileName, targetFileName));
        map.put("comments", "full");
//        map.put("source", FileUtil.file2String(sourceFileName));
//        map.put("target", FileUtil.file2String(targetFileName));
        map.put("source", FileReaderUtils.getStringListFromFile(sourceFileName));
        map.put("target", FileReaderUtils.getStringListFromFile(targetFileName));
        return map;

    }

    private Map<String, Object> compareIni(FileInfo fileInfo) {
        logger.info("begin to compare ini file: " + fileInfo.getSource());
        String sourceFile = fileInfo.getSource();
        String targetFile = fileInfo.getTarget();
        Integer method = fileInfo.getMethod();
        logger.info("compare ini method: " + method);
        Map<String, Map<String, String>> sourceMap = FileReaderUtils.readIni(sourceFile);
        Map<String, Map<String, String>> targetMap = FileReaderUtils.readIni(targetFile);
        return this.compareIniMap(sourceMap, targetMap, method);
    }

    /**
     * 比较ini文件的hashMap
     *
     * @param sourceMap 源端hashmap
     * @param targetMap 目标端hashmap
     * @return
     */
    private Map<String, Object> compareIniMap(Map<String, Map<String, String>> sourceMap, Map<String, Map<String, String>> targetMap, Integer type) {
//        Set<String> sourceSet = sourceMap.keySet();
//        Set<String> targetSet = targetMap.keySet();
        List<ContentValueInfo> list = new ArrayList<>();
        Set<String> totalSet = new HashSet<>();
        totalSet.addAll(sourceMap.keySet());
        totalSet.addAll(targetMap.keySet());
        Map<String, Object> map = new HashMap<>();
        for (String key : totalSet) {
            Map<String, String> sourceValueMap = sourceMap.get(key);
            Map<String, String> targetValueMap = targetMap.get(key);
            list.addAll(this.dealIniSection(key, sourceValueMap, targetValueMap, type));
        }
        map.put("result", list);
        map.put("code", 0);
        return map;
    }

    private List<ContentValueInfo> dealIniSection(String sectionName, Map<String, String> sourceValueMap, Map<String, String> targetValueMap, Integer method) {
        List<ContentValueInfo> contentValueInfos = new ArrayList<>();
        Set<String> totalSet = new HashSet<>();
        totalSet.addAll(sourceValueMap.keySet());
        totalSet.addAll(targetValueMap.keySet());
        boolean flag = true;
        for (String key : totalSet) {
            String sourceValue = sourceValueMap.get(key);
            String targetValue = targetValueMap.get(key);
            ContentValueInfo contentValueInfo = new ContentValueInfo();
            contentValueInfo.setName(key);
            contentValueInfo.setSectionName(sectionName);
            contentValueInfo.setSourceValue(sourceValue);
            contentValueInfo.setTargetValue(targetValue);
            contentValueInfo.setMethod(method);
            String status = getStatus(sourceValue, targetValue);
            if (flag || (!status.equals("1") && method == 1) || (method == 2) && status.equals("4")) {
                flag = false;
            }
            contentValueInfo.setStatus(status);
            contentValueInfos.add(contentValueInfo);
        }
        return contentValueInfos;
    }

    private String getStatus(String sourceValue, String targetValue) {
        String status = "";
        if (targetValue != null && sourceValue != null) {
            status = targetValue.equals(sourceValue) ? "1" : "2";
        } else if (sourceValue == null) {
            status = "4";
        } else {
            status = "3";
        }
        return status;
    }


    public Map<String, Object> compareConfig(FileInfo fileInfo) {
        String sourceFile = fileInfo.getSource();
        String targetFile = fileInfo.getTarget();
        File file = new File(targetFile);
        Map<String, Object> map = new HashMap<>();
        if (!file.exists()) {
            map.put("code", -1);
            map.put("msg", "the target File [" + targetFile + "] is not exists");
            return map;
        }
        Map<String, String> sourceValueMap = DiffProperties.getPropertyMap(sourceFile);
        Map<String, String> targetValueMap = DiffProperties.getPropertyMap(targetFile);
        Integer method = fileInfo.getMethod();
        boolean flag = true;
        List<ContentValueInfo> contentValueInfos = new ArrayList<>();
        if (method == 1 || method == 2) {
            contentValueInfos = new ArrayList<>();
            Set<String> totalSet = new HashSet<>();
            totalSet.addAll(sourceValueMap.keySet());
            totalSet.addAll(targetValueMap.keySet());
            for (String key : totalSet) {
                String sourceValue = sourceValueMap.get(key);
                String targetValue = targetValueMap.get(key);
                ContentValueInfo contentValueInfo = new ContentValueInfo();
                contentValueInfo.setName(key);
                contentValueInfo.setMethod(method);
                contentValueInfo.setSourceValue(sourceValue);
                contentValueInfo.setTargetValue(targetValue);
                String status = getStatus(sourceValue, targetValue);
                if (flag || (!status.equals("1") && method == 1) || (method == 2) && status.equals("4")) {
                    flag = false;
                }
                contentValueInfo.setStatus(status);
                contentValueInfos.add(contentValueInfo);
            }
            map.put("result", contentValueInfos);
            map.put("code", flag ? 0 : 1);
        } else if (method == 3) {
            map.put("code", 0);
            map.put("msg", "This file compare method is ignore");
        }
//        List<ContentValueInfo> contentValueInfos = DiffProperties.compareProperties(sourceFile, targetFile);

        map.put("result", contentValueInfos);
//        map.put("comments", "111");
        return map;
    }

    private Map<String, Object> compareDir(FileInfo fileInfo, String basePath) {
        Map<String, Object> map = new HashMap<>();
        String targetDir = basePath + fileInfo.getTarget();
        logger.info("compare dir, targetDir: " + targetDir);
        String sourceDir = fileInfo.getSource();
        logger.info("begin to compare dir: targetDir: " + targetDir);
        logger.info("begin to compare dir: sourceDir: " + sourceDir);
        File source = new File(sourceDir);
        File[] sourceFiles = source.listFiles();
        File target = new File(targetDir);
        File[] targetFiles = target.listFiles();
//        logger.info("targetDir file count:" + targetFiles.length);
        if (targetFiles == null || targetFiles.length == 0) {
            System.out.println("no files found in: " + targetDir);
        }
        List<FileValueInfo> fileValueInfos = new ArrayList<>();
        assert sourceFiles != null;
        List<String> excludeFiles = FileInfoUtil.getExcludeFile(fileInfo);
        for (File f : sourceFiles) {
            if (excludeFiles != null && excludeFiles.contains(f.getName())) {
                continue;
            }
            // todo 添加跳过的文件
            String fileName = f.getName();
            FileValueInfo fileValueInfo = new FileValueInfo();
            fileValueInfo.setFileName(f.getName());
            fileValueInfo.setSourceLength(f.length());
            String targetFilePath = targetDir + File.separator + fileName;
            logger.info("targetFilePath: " + targetDir);
            File temp = new File(targetFilePath);
            if (!temp.exists()) {
                fileValueInfo.setStatus("3");
                fileValueInfo.setTargetLength(0L);
            } else {
                fileValueInfo.setStatus(FileReaderUtils.isSameFile(targetFilePath, f.getAbsolutePath()) ? "1" : "2");
                fileValueInfo.setTargetLength(temp.length());
            }
            fileValueInfos.add(fileValueInfo);
        }
        logger.info("end to compare dir ");
//        return fileValueInfos;
        map.put("result", fileValueInfos);
        map.put("type", "dir");
        return map;
    }

    public void transfer2Server(String localFile, String remoteFile, String host, String user) {
        logger.info("begin to transfer: " + localFile);
        if (localFile.endsWith(File.separator)) {
            transferDir2Server(localFile, remoteFile, host, user);
        } else {
            transferFile2Server(localFile, remoteFile, host, user);
        }
    }

    @Override
    public Map<String, Object> compareFile(HostInfo hostInfo, FileInfo fileInfo) {
        logger.info("=====begin to compare file===== fileName:" + fileInfo.getSource());
        String sourceFile = fileInfo.getSource();
        Project project = projectRepository.getOne(hostInfo.getProjectId());
        String basePath = SysUtil.getBasePathWithoutSeparator(hostInfo, project.getName());
        if (fileInfo.getDirFlag() == 1) {
            return compareDir(fileInfo, basePath);
        }
        String targetFile = basePath + fileInfo.getTarget();
        fileInfo.setTarget(targetFile);
        logger.info("target absolute path: " + targetFile);
        logger.info("fileName: " + sourceFile);
        logger.info("file type: " + fileInfo.getType());
        File file = new File(targetFile);
        Map<String, Object> map = new HashMap<>();
        if (!file.exists()) {
            map.put("code", -1);
            map.put("msg", "the target File [" + targetFile + "] is not exists");
            return map;
        }
        switch (fileInfo.getType()) {
            case 1:
                return compareConfig(fileInfo);
            case 2:
                return compareIni(fileInfo);
            case 0:
                return compareOther(fileInfo);
            default:
                return null;
        }
    }

    @Override
    public List<Map<String, Object>> collectFileById(Long id) {
        logger.info("begin to collect file by id: " + id);
        HostInfo hostInfo = hostInfoRepository.getOne(id);
        Project project = projectRepository.getOne(hostInfo.getProjectId());
        String projectName = project.getName();
        List<HostDetail> hostDetails = hostDetailRepository.findByHostId(id);
        Map<String, Object> map = HostUtil.hostDetailList2Map(hostDetails);
        map.put("hostId", hostInfo.getHostId());
        map.put("hostIp", hostInfo.getHostIp());
        map.put("projectId", hostInfo.getProjectId());
        String basePath = SysUtil.getBasePathWithoutSeparator(hostInfo, projectName);
//        收集文件前先清空目录
        FileUtil.deleteDir(basePath);
//        获得ssh2连接
        Connection connection = new Connection(hostInfo.getHostIp());
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            connection.connect();
            System.out.println("开始登录");
            String password = HostUtil.decryptDES((String) map.get("password"));
            boolean isAuthenticated = connection.authenticateWithPassword((String) map.get("user"), password);
            if (!isAuthenticated) {
                System.out.println("登录服务器失败！");
                return null;
            }
            List<FileInfo> fileInfos = fileInfoRepository.findByHostId(id);
            logger.info("file size :" + fileInfos.size());
            for (FileInfo fileInfo : fileInfos) {
                //            int index = targetName.lastIndexOf(File.separator);
//            String targetPath = targetName.substring(0, index);
//            String fileName = targetName.substring(index+1);
                String targetName = fileInfo.getTarget();
                String localFile = basePath + targetName;
                logger.info("begin to scp file to local,fileName: " + fileInfo.getTarget() + "; localFile: " + localFile);
                logger.info("fileType: " + fileInfo.getDirFlag());
                logger.info("fileType: " + (fileInfo.getDirFlag() == 1 ? "dir" : "file"));
                if (fileInfo.getDirFlag() == 1) {
                    logger.info("dir ==1");
                    ScpTools.getRemoteDir(fileInfo, basePath, connection);
                    continue;
                }
                list.add(ScpTools.getRemoteFile(fileInfo.getTarget(), localFile, connection));
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("登录服务器失败！");
            return null;
        } finally {
            connection.close();
        }

        return list;
    }

    public Map<String, Object> scpFile2Server(String localFile, String target, String host, String user, String passwd) {
        String remotePath = target.substring(0, target.lastIndexOf(File.separator));
        Connection connection = new Connection(host);
        Map<String, Object> map = new HashMap<>();
        try {
            connection.connect();
            System.out.println("开始登录");
            boolean isAuthenticated = connection.authenticateWithPassword(user, passwd);
            if (!isAuthenticated) {
                map.put("code", -1);
                map.put("msg", "connect to server failed.Authentication failed.");
                return map;
            }
        } catch (IOException e) {
            e.printStackTrace();
            map.put("code", -1);
            map.put("msg", "connect to server failed.");
            return map;
        }
        File file = new File(localFile);
        String retCode = ScpTools.backupFile(target, connection);
//        todo  不管成功与否 都将文件推送过去
        System.out.println("backup return code: " + retCode);
        if (!retCode.equalsIgnoreCase("0")) {
            map.put("fileName", file.getName());
            map.put("code", -1);
            map.put("msg", "backup file failed;");
            return map;
        }
        Map<String, Object> result = ScpTools.uploadFile(file, remotePath, connection, null);
        connection.close();
//        file.delete();
        return result;
    }

    public void transferFile2Server(String localFile, String remoteFile, String host, String user) {
        logger.info("begin to transfer file: " + localFile);
        int index = remoteFile.lastIndexOf(File.separator);
        String remotePath = remoteFile.substring(0, index);
        String remoteFileName = remoteFile.substring(index + 1);
        remoteFile = SysUtil.getRootPath() + host + remoteFile;
        int index2 = remoteFile.lastIndexOf(File.separator);
        String localPath = remoteFile.substring(0, index2);
        String localFileName = remoteFile.substring(index2 + 1);
        String bakName = localFileName + "." + getTimeStr() + ".bak";
        String cmd = "cd " + localPath + "; tar -zcv -f " + bakName + " " + localFileName + "; ";
        String cmd1 = "scp " + localPath + File.separator + bakName + " " + user + "@" + host + ":" + remotePath + "; ";
        String cmd2 = "scp " + localFile + " " + user + "@" + host + ":" + remotePath + "; ";

        System.out.println(cmd);
        System.out.println(cmd1);
        System.out.println(cmd2);
        Process process = null;
        logger.info("transfer file command : " + cmd + "\t" + cmd1 + "\t" + cmd2);

        int status = -1;
        try {
            process = Runtime.getRuntime().exec(cmd + cmd1 + cmd2);
            status = process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("transfer file status: " + status);
    }

    public void transferDir2Server(String localFile, String remoteFile, String host, String user) {
        logger.info("begin to transfer dir: " + localFile);
        remoteFile = remoteFile.substring(0, remoteFile.length() - 1);
        int index = remoteFile.lastIndexOf(File.separator);
        String remotePath = remoteFile.substring(0, index);
        String remoteFileName = remoteFile.substring(index + 1);
        remoteFile = SysUtil.getRootPath() + host + remoteFile;
        int index2 = remoteFile.lastIndexOf(File.separator);
        String localPath = remoteFile.substring(0, index2);
        String localFileName = remoteFile.substring(index2 + 1);
        String bakName = localFileName + "." + getTimeStr() + ".bak";
        String cmd = "cd " + localPath + "; tar -zcv -f " + bakName + " " + localFileName + "; ";
        String cmd1 = "scp " + localPath + File.separator + bakName + " " + user + "@" + host + ":" + remotePath + "; ";
        String cmd2 = "scp " + localFile + " " + user + "@" + host + ":" + remotePath + "; ";

        Process process = null;
        logger.info("transfer file command : " + cmd + "\t" + cmd1 + "\t" + cmd2);
        int status = -1;
        try {
            process = Runtime.getRuntime().exec(cmd + cmd1 + cmd2);
            status = process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("transfer dir status: " + status);
    }

    public String getTimeStr() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date d = new Date();
        String str = sdf.format(d);
        return str;
    }

    public static void main(String[] args) {
        FileServiceImpl fileService = new FileServiceImpl();
        String localFile = "dir/";
        System.out.println(localFile.endsWith("/"));
        System.out.println(localFile.substring(localFile.length() - 1).equals(File.separator));
    }
}
