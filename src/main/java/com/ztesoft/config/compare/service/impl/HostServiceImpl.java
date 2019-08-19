package com.ztesoft.config.compare.service.impl;

import ch.ethz.ssh2.Connection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ztesoft.config.compare.dto.ContentValueInfo;
import com.ztesoft.config.compare.dto.FileValueInfo;
import com.ztesoft.config.compare.entity.FileInfo;
import com.ztesoft.config.compare.entity.HostDetail;
import com.ztesoft.config.compare.entity.HostInfo;
import com.ztesoft.config.compare.entity.Project;
import com.ztesoft.config.compare.repository.FileInfoRepository;
import com.ztesoft.config.compare.repository.HostDetailRepository;
import com.ztesoft.config.compare.repository.HostInfoRepository;
import com.ztesoft.config.compare.repository.ProjectRepository;
import com.ztesoft.config.compare.service.FileService;
import com.ztesoft.config.compare.service.HostService;
import com.ztesoft.config.compare.utils.*;
import org.apache.catalina.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class HostServiceImpl implements HostService {
    private static Logger logger = LoggerFactory.getLogger(HostServiceImpl.class);
    @Autowired
    private FileInfoRepository fileInfoRepository;

    @Autowired
    private HostInfoRepository hostInfoRepository;

    @Autowired
    private HostDetailRepository hostDetailRepository;

    @Autowired
    private ProjectRepository projectRepository;


    @Transactional
    @Override
    public HostDetail insert(HostDetail hostDetail) {
        return hostDetailRepository.save(hostDetail);
    }

    @Override
    public List<HostDetail> saveAll(List<HostDetail> hostDetails) {
        return hostDetailRepository.saveAll(hostDetails);
    }

    @Transactional
    @Override
    public List<HostDetail> updateHostDetail(List<HostDetail> hostDetails, Long hostId) {
        hostDetailRepository.deleteByHostId(hostId);
        return hostDetailRepository.saveAll(hostDetails);
    }

    public static void main(String[] args) {
        List<HostDetail> hostDetails = new ArrayList<>();
        for(int i = 0;i<3;i++) {
            HostDetail hostDetail = new HostDetail();
            hostDetail.setKey("key" + i);
            hostDetail.setValue("value" + i);
            hostDetails.add(hostDetail);
        }
        String a = JSON.toJSONString(hostDetails);
        System.out.println(a);
        List<HostDetail> list = JSONObject.parseArray(a,HostDetail.class);
        System.out.println(list.size());
        for(HostDetail hostDetail: hostDetails) {
            System.out.println(hostDetail.getKey() + "==== " + hostDetail.getValue());
        }
    }
}
