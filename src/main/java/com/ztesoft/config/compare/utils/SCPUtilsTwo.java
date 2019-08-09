package com.ztesoft.config.compare.utils;

import ch.ethz.ssh2.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SCPUtilsTwo {
    private static int port = 22;//目标服务器端口
    private static String basePath = "/home/wyx/source/dir2";//目标服务器目录
    private static String copyHost = "120.79.191.173";//目标服务器地址
    private static String copyUsername = "wyx"; //目标服务器用户名
    private static String copyPassword = "q03081189";//目标服务器密码
    private static String localFile = "E:\\Documents\\test\\";//本地服务器指定目录//liux环境下会创建到根目录下

    public static Boolean isDir(SFTPv3Client sftpv3Client, String path) {
        if (sftpv3Client != null && path != null && path.length() != 0) {
            SFTPv3FileAttributes sFTPv3FileAttributes;
            try {
                sFTPv3FileAttributes = sftpv3Client.lstat(path);
            } catch (IOException e) {
                System.out.println("文件加不存在：" + e.getLocalizedMessage());
                return false;
            }
            return sFTPv3FileAttributes.isDirectory();
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println();
        //                        todo 修改文件分隔符
        File file = new File(localFile + "/");
        if (!file.exists()) {
            file.mkdirs();
        }
        System.out.println("创建成功！");
        Connection conn = null;
        System.out.println("copyHost===" + copyHost);
        System.out.println("copyUsername===" + copyUsername);
        System.out.println("copyPassword===" + copyPassword);
        System.out.println("port===" + port);
        try {
            conn = new Connection(copyHost, port);
            System.out.println("开始连接");
            conn.connect();
            System.out.println("开始登录");
            boolean isAuthenticated = conn.authenticateWithPassword(copyUsername, copyPassword);
            if (!isAuthenticated)
                throw new IOException("Authentication failed.");

            SFTPv3Client sftpv3Client = new SFTPv3Client(conn);

            //查看目标服务器此文件夹是否存在
            Boolean b = SCPUtilsTwo.isDir(sftpv3Client, basePath);
            List<String> remoteFiles = new ArrayList<String>();
            if (!b) {
                System.out.println("文件夹不存在");
            } else {
                //获取目标文件夹中所有文件
                //                        todo 修改文件分隔符
                List<SFTPv3DirectoryEntry> v = sftpv3Client.ls(basePath + "/");
                for (SFTPv3DirectoryEntry entry : v) {
                    SFTPv3FileAttributes attributes = entry.attributes;
                    String filename = entry.filename;
                    if (attributes.isRegularFile()){
//                        todo 修改文件分隔符
                        System.out.println(filename);
//                        SCPClient scpClient = conn.createSCPClient();
//                        ScpTools.getRemoteFile(filename,basePath,localFile,conn);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("文件上传异常" + e);
        } finally {
            if(conn != null) {
                conn.close();
            }
        }

    }
}
