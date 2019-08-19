package com.ztesoft.config.compare.utils;

import ch.ethz.ssh2.*;
import com.ztesoft.config.compare.dto.FileValueInfo;
import com.ztesoft.config.compare.entity.FileInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.transform.Source;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScpTools {
    private static Logger logger = LoggerFactory.getLogger(ScpTools.class);


    private static String charset = "UTF-8";
    private static final int TIME_OUT = 1000 * 2 * 60; // 2分钟超时
    private final static Log log = LogFactory.getLog(ScpTools.class);

    /**
     * 私有化默认构造函数
     * 实例化对象只能通过getInstance
     */
    private ScpTools() {

    }

    public static Map<String, Object> getRemoteDir(FileInfo fileInfo, String basePath, Connection connection) {
        logger.info("begin to get dir to local:==================");
        SFTPv3Client sftpv3Client = null;
        List<String> excludeFiles = FileInfoUtil.getExcludeFile(fileInfo);
        logger.info("basepath: ： ================" + basePath);
        Map<String, Object> map = new HashMap<>();
        try {
            sftpv3Client = new SFTPv3Client(connection);
            //查看目标服务器此文件夹是否存在
            Boolean b = isDir(sftpv3Client, fileInfo.getTarget());
            if (!b) {
                System.out.println("文件夹不存在");
                map.put("code", -1);
                map.put("msg", "target directory is not exists;");
                return map;
            } else {
                logger.info("服务器目标文件夹：" + fileInfo.getTarget());
                //获取目标文件夹中所有文件
                List<SFTPv3DirectoryEntry> v = sftpv3Client.ls(fileInfo.getTarget());
                logger.info("获取到的文件个数为： " + v.size());
                for (SFTPv3DirectoryEntry entry : v) {
                    logger.info("file name: " + entry.filename);
                    SFTPv3FileAttributes attributes = entry.attributes;
                    String fileName = entry.filename;
                    String target = fileInfo.getTarget() + File.separator + fileName;
                    if (!attributes.isRegularFile() || (excludeFiles != null && excludeFiles.contains(fileName))) {
                        continue;
                    }
                    String localFile = basePath + fileInfo.getTarget() + File.separator + fileName;
                    logger.info("begin to scp dir to local,fileName: " + target + "; localFile: " + localFile);
                    ScpTools.getRemoteFile(target, localFile, connection);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        map.put("code", 0);
        return map;
    }

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

    public static void getRemoteFile(String remoteFile, String remoteTargetDirectory, String newPath, Connection connection) {
        SCPClient scpClient = null;
        SCPInputStream sis = null;
        FileOutputStream fos = null;
        try {
            scpClient = connection.createSCPClient();
            sis = scpClient.get(remoteTargetDirectory + "/" + remoteFile);
            File f = new File(newPath);
            if (!f.exists()) {
                f.mkdirs();
            }
            File newFile = new File(newPath + remoteFile);
            fos = new FileOutputStream(newFile);
            byte[] b = new byte[4096];
            int i;
            while ((i = sis.read(b)) != -1) {
                fos.write(b, 0, i);
            }
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (sis != null) {
                    sis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        connection.close();
        System.out.println("download ok");
    }

    public static Map<String, Object> getRemoteFile(String remoteFile, String newFile, Connection connection) {
        Map<String, Object> map = new HashMap<>();
        SCPClient scpClient;
        SCPInputStream sis = null;
        FileOutputStream fos = null;
        int index = newFile.lastIndexOf(File.separator);
        String newPath = newFile.substring(0, index);
        String fileName = newFile.substring(index + 1);
        map.put("fileName", fileName);
        try {
            scpClient = connection.createSCPClient();
            sis = scpClient.get(remoteFile);
            File f = new File(newPath);
            if (!f.exists()) {
                f.mkdirs();
            }
            File file = new File(newFile);
            fos = new FileOutputStream(newFile);
            byte[] b = new byte[4096];
            int i;
            while ((i = sis.read(b)) != -1) {
                fos.write(b, 0, i);
            }
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            map.put("code", -1);
            map.put("msg", e.getMessage());
            return map;
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
                if (sis != null) {
                    sis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("download ok");
        map.put("code", "0");
        return map;
    }

    public static String backupFile(String target, Connection connection) {
        String remotePath = target.substring(0, target.lastIndexOf("/"));
        String bakName = target.substring(target.lastIndexOf("/") + 1);
//        bak.`date +%y%m%d`.tar.gz
        String cmd = "cd " + remotePath + "; cp " + bakName + " " + bakName + "_`date +%Y%m%d%H%M%S`.bak " + ";";
        System.out.println("backupfile cmd: " + cmd);
        String[] temp = executeCmd(cmd, connection);
        return temp[2];
    }

    public static String backupDir(String target, Connection connection) {
        logger.info("ScpTools.backupDir:" + target);
        String remotePath = target.substring(0, target.lastIndexOf("/"));
        String bakName = target.substring(target.lastIndexOf("/") + 1);
//        bak.`date +%y%m%d`.tar.gz
        String cmd = "cd " + remotePath + "; tar -zcf " + bakName + "_`date +%Y%m%d%H%M%S`.tar.gz " + bakName + ";";
        logger.info("backupDir cmd: " + cmd);
        String[] temp = executeCmd(cmd, connection);
        return temp[2];
    }

    /**
     * 获取服务器上相应文件的流
     *
     * @param remoteFile            文件名
     * @param remoteTargetDirectory 文件路径
     * @return
     * @throws IOException
     */
//    public SCPInputStream getStream(String remoteFile, String remoteTargetDirectory) throws IOException {
//        Connection connection = new Connection(ip, port);
//        connection.connect();
//        boolean isAuthenticated = connection.authenticateWithPassword(name, password);
//        if (!isAuthenticated) {
//            System.out.println("连接建立失败");
//            return null;
//        }
//        SCPClient scpClient = connection.createSCPClient();
//        return scpClient.get(remoteTargetDirectory + "/" + remoteFile);
//    }

    /**
     * 上传文件到服务器
     *
     * @param f                     文件对象
     * @param remoteTargetDirectory 上传路径
     * @param mode                  默认为null
     */
    public static Map<String, Object> uploadFile(File f, String remoteTargetDirectory, Connection connection, String mode) {
        Map<String, Object> map = new HashMap<>();
        System.out.println("begin to upload file");
        logger.info("fileName: " + f.getAbsolutePath() + "=====" + f.getName());
        logger.info("remote Path: " + remoteTargetDirectory);
        SCPClient scpClient = null;
        SCPOutputStream os = null;
        FileInputStream fis = null;
        try {
            scpClient = new SCPClient(connection);
            os = scpClient.put(f.getName(), f.length(), remoteTargetDirectory, mode);
            byte[] b = new byte[4096];
            fis = new FileInputStream(f);
            int i;
            while ((i = fis.read(b)) != -1) {
                os.write(b, 0, i);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            map.put("code", -1);
            map.put("msg", e.getMessage());
            return map;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("upload ok");
        map.put("code", 0);
        f.getPath();
        return map;
    }

    /**
     * 上传文件到服务器
     *
     * @param fileInfo   文件对象
     * @param connection 上传路径
     * @param mode       默认为null
     */
    public static Map<String, Object> uploadDir(FileInfo fileInfo, Connection connection, String mode) {
        Map<String, Object> map = new HashMap<>();
        System.out.println("begin to upload file");
        List<String> excludeFiles = FileInfoUtil.getExcludeFile(fileInfo);
        SCPClient scpClient = null;
        SCPOutputStream os = null;
        FileInputStream fis = null;
        try {
            scpClient = new SCPClient(connection);
//            备份目录
            ScpTools.backupDir(fileInfo.getTarget(), connection);

            String sourceDir = fileInfo.getSource();
            File source = new File(sourceDir);
            File[] sourceFiles = source.listFiles();
            if (sourceFiles == null || sourceFiles.length == 0) {
                return null;
            }
            for (File f : sourceFiles) {
                if (excludeFiles != null && excludeFiles.contains(f.getName())) {
                    continue;
                }
                logger.info("begin to trans  dir  file to remote:" + f.getName());
                os = scpClient.put(f.getName(), f.length(), fileInfo.getTarget(), mode);
                byte[] b = new byte[4096];
                fis = new FileInputStream(f);
                int i;
                while ((i = fis.read(b)) != -1) {
                    os.write(b, 0, i);
                }
                os.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            map.put("code", -1);
            map.put("msg", e.getMessage());
            return map;
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("upload dir ok");
        map.put("code", 0);
        return map;
    }

    /**
     * 上传文件到服务器
     *
     * @param cmd 文件对象
     */
    public static String[] executeCmd(String cmd, Connection conn) {
        System.out.println("begin to execute command");
        System.out.println("command: " + cmd);
        InputStream stdOut = null;
        InputStream stdErr = null;
        String outStr = "";
        String outErr = "";
        int outStatus = -1;
        String[] rt = new String[3];
        try {
            // Open a new {@link Session} on this connection
            Session session = conn.openSession();
            // Execute a command on the remote machine.
            session.execCommand(cmd);
            stdOut = new StreamGobbler(session.getStdout());
            outStr = processStream(stdOut, charset);
            stdErr = new StreamGobbler(session.getStderr());
            outErr = processStream(stdErr, charset);
            outStatus = session.getExitStatus();
            session.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT);
            log.info("outStr=" + outStr.trim() + ", outErr=" + outErr.trim() + ", exitStatus="
                    + outStatus);
            rt[0] = outStr.trim();
            rt[1] = outErr.trim();
            rt[2] = String.valueOf(outStatus);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
//                if (conn != null) {
//                    conn.close();
//                }
                if (stdOut != null) {
                    stdOut.close();
                }
                if (stdErr != null) {
                    stdErr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rt;
    }

    /**
     * Description: <br>
     * 按指定字符集转换输入流
     *
     * @param in
     * @param charset
     * @return
     * @throws Exception <br>
     * @author XXX<br>
     * @taskId <br>
     */
    private static String processStream(InputStream in, String charset) throws Exception {
        byte[] buf = new byte[1024];
        StringBuilder sb = new StringBuilder();
        while (in.read(buf) != -1) {
            sb.append(new String(buf, charset));
        }

        return sb.toString();
    }

    public static void main(String[] args) throws IOException {

        File f1 = new File("E:\\Documents\\test\\127.0.0.1\\111.txt");
        System.out.println(f1.getAbsolutePath());
    }
}
