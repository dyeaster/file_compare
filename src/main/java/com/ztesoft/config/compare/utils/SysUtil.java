package com.ztesoft.config.compare.utils;

import com.ztesoft.config.compare.entity.HostInfo;

import java.io.File;

public class SysUtil {
    public static String getRootPath() {
        String usrHome = System.getProperty("user.home") + File.separator + "filecompare" + File.separator + "data" + File.separator;
        return usrHome;
    }

    public static String getPwd() {
        return System.getProperty("user.dir");
    }


    public static void main(String[] args) {
        System.out.println(getPwd());
    }

    public static String getBasePath(HostInfo hostInfo, String projectName) {
        return getRootPath() + projectName + File.separator + hostInfo.getHostIp() + File.separator;
    }
    public static String getBasePathWithoutSeparator(HostInfo hostInfo, String projectName) {
        return getRootPath() + projectName + File.separator + hostInfo.getHostIp();
    }
}
