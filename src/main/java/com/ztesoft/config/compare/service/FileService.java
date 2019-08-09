package com.ztesoft.config.compare.service;

import com.ztesoft.config.compare.entity.FileInfo;
import com.ztesoft.config.compare.entity.HostInfo;

import java.util.List;
import java.util.Map;

/**
 * 文件服务接口
 */
//@Service
public interface FileService {

    Map<String, Object> syncFile2Server(FileInfo fileInfo);

    Map<String, Object> compareFile(HostInfo hostInfo, FileInfo fileInfo);

    List<Map<String, Object>> collectFileById(Long id);

    Map<String, Object> compareConfig(FileInfo fileInfo);
}
