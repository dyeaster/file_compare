package com.ztesoft.config.compare.service;

import com.ztesoft.config.compare.entity.FileInfo;
import com.ztesoft.config.compare.entity.HostDetail;
import com.ztesoft.config.compare.entity.HostInfo;

import java.util.List;
import java.util.Map;

/**
 * host服务接口
 */
//@Service
public interface HostService {

    HostDetail insert(HostDetail hostDetail);

    List<HostDetail> saveAll(List<HostDetail> hostDetails);

    List<HostDetail> updateHostDetail(List<HostDetail> hostDetails, Long hostId);
}
