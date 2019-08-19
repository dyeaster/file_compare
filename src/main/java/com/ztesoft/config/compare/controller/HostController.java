package com.ztesoft.config.compare.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ztesoft.config.compare.dto.HostInfoDto;
import com.ztesoft.config.compare.entity.*;
import com.ztesoft.config.compare.repository.HostDetailRepository;
import com.ztesoft.config.compare.repository.HostInfoRepository;
import com.ztesoft.config.compare.service.HostService;
import com.ztesoft.config.compare.utils.HostUtil;
import com.ztesoft.config.compare.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/host")
public class HostController {

    @Autowired
    private HostInfoRepository hostInfoRepository;

    @Autowired
    private HostDetailRepository hostDetailRepository;

    @Autowired
    private HostService hostService;

    @RequestMapping(method = RequestMethod.POST)
    public Map<String, Object> insert(@RequestBody HostInfoDto hostInfoDto) {
        HostInfo hostInfo = new HostInfo();
        hostInfo.setHostIp(hostInfoDto.getHostIp());
        hostInfo.setProjectId(hostInfoDto.getProjectId());
        hostInfo.setPort(hostInfoDto.getPort());
        hostInfo.setUser(hostInfoDto.getUser());
        String password = hostInfoDto.getPassword();
        hostInfo.setPassword(password);
        Map<String, Object> resultMap = new HashMap<>();
        if (!HostUtil.checkHost(hostInfo)) {
            resultMap.put("code", -1);
            resultMap.put("msg", "server information is not correct, please try again.");
            return resultMap;
        }
        hostInfo.setPassword(HostUtil.encryptDES(hostInfoDto.getPassword()));
        List<HostDetail> hostDetails = hostInfoDto.getHostDetailList();
        hostInfo.setAdditionValue(JSON.toJSONString(hostDetails));
//        保存host信息
        resultMap.put("hostInfo",hostInfoRepository.save(hostInfo));
        resultMap.put("code", 0);
        resultMap.put("msg", "add host success");
        return resultMap;
    }

//    @RequestMapping(method = RequestMethod.POST)
    public Map<String, Object> insert1(@RequestBody Map<String, String> param) {
        Map<String, Object> resultMap = new HashMap<>();
        if (!HostUtil.checkHost(param)) {
            resultMap.put("code", -1);
            resultMap.put("msg", "server information is not correct, please try again.");
            return resultMap;
        }
        Long projectId = Long.valueOf(param.get("projectId"));
        String hostIp = param.get("hostIp");
        HostInfo hostInfo = new HostInfo();
        hostInfo.setProjectId(projectId);
        hostInfo.setHostIp(hostIp);
        hostInfo = hostInfoRepository.save(hostInfo);
        if (hostInfo.getHostId() == null) {
            resultMap.put("code", -1);
            resultMap.put("msg", "add host failed, please refer to the log");
            return resultMap;
        }
        Long hostId = hostInfo.getHostId();
        param.remove("projectId");
        param.put("password", HostUtil.encryptDES(param.get("password")));
        for (Map.Entry<String, String> entry : param.entrySet()) {
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
            HostDetail hostDetail = new HostDetail();
            hostDetail.setHostId(hostId);
            hostDetail.setKey(mapKey);
            hostDetail.setValue(mapValue);
            hostDetailRepository.save(hostDetail);
        }
        resultMap.put("code", 0);
        resultMap.put("msg", "add host success");
        return resultMap;
    }

    @Transactional
    @RequestMapping(method = RequestMethod.PUT)
    public Map<String, Object> update(@RequestBody HostInfoDto hostInfoDto) {
        HostInfo hostInfo = new HostInfo();
        hostInfo.setHostIp(hostInfoDto.getHostIp());
        hostInfo.setProjectId(hostInfoDto.getProjectId());
        hostInfo.setPort(hostInfoDto.getPort());
        hostInfo.setUser(hostInfoDto.getUser());
        hostInfo.setHostId(hostInfoDto.getHostId());
        String password = hostInfoDto.getPassword();
        hostInfo.setPassword(password);
        Map<String, Object> resultMap = new HashMap<>();
        if (!HostUtil.checkHost(hostInfo)) {
            resultMap.put("code", -1);
            resultMap.put("msg", "server information is not correct, please try again.");
            return resultMap;
        }
        hostInfo.setPassword(HostUtil.encryptDES(hostInfoDto.getPassword()));
        List<HostDetail> hostDetails = hostInfoDto.getHostDetailList();
        hostInfo.setAdditionValue(JSON.toJSONString(hostDetails));
//        保存host信息
        resultMap.put("hostInfo",hostInfoRepository.save(hostInfo));
        resultMap.put("code", 0);
        resultMap.put("msg", "add host success");
        return resultMap;
    }
//    @Transactional
//    @RequestMapping(method = RequestMethod.PUT)
    public Map<String, Object> update1(@RequestBody Map<String, String> param) {
        Map<String, Object> resultMap = new HashMap<>();
        if (!HostUtil.checkHost(param)) {
            resultMap.put("code", -1);
            resultMap.put("msg", "server information is not correct, please try again.");
            return resultMap;
        }
        Long projectId = Long.valueOf(param.get("projectId"));
        Long hostId = Long.valueOf(param.get("hostId"));
        String hostIp = param.get("hostIp");
        HostInfo hostInfo = new HostInfo();
        hostInfo.setProjectId(projectId);
        hostInfo.setHostIp(hostIp);
        hostInfo.setHostId(hostId);
        hostInfoRepository.save(hostInfo);
        param.remove("projectId");
        param.remove("hostId");
        param.put("password", HostUtil.encryptDES(param.get("password")));
        List<HostDetail> hostDetails = HostUtil.map2HostDetailList(param, hostId);
//        删除原有的属性
//        hostDetailRepository.deleteByHostId(hostId);
//        保存属性
//        hostDetailRepository.saveAll(hostDetails);
        for (HostDetail hostDetail : hostDetails) {
            if (hostDetailRepository.existsByHostIdAndAndKey(hostDetail.getHostId(), hostDetail.getKey())) {
                hostDetailRepository.updateOne(hostDetail.getValue(), hostDetail.getHostId(), hostDetail.getKey());
            } else {
                hostDetailRepository.save(hostDetail);
            }
        }
        resultMap.put("code", 0);
        resultMap.put("msg", "add host success");
        return resultMap;
    }

    @RequestMapping(method = RequestMethod.GET)
    public Map<String, Object> find() {
        List<HostInfo> hostInfos = hostInfoRepository.findAll();
        return ResponseUtil.renderTableResponse(hostInfos);
    }

    @RequestMapping(value = "/4file/{id}", method = RequestMethod.GET)
    public List<HostDetail> findById(@PathVariable Long id) {
        HostInfo hostInfo = hostInfoRepository.getOne(id);
        return HostUtil.hostInfo2HostDetailList(hostInfo);
    }

    @RequestMapping(value = "/project/{id}", method = RequestMethod.GET)
    public Map<String, Object> findByProjectId(@PathVariable Long id) {
        List<HostInfo> hostInfos = hostInfoRepository.findByProjectId(id);
        List<HostInfoDto> hostInfoDtos = new ArrayList<>(hostInfos.size());
        for (HostInfo hostInfo : hostInfos) {
            HostInfoDto hostInfoDto = new HostInfoDto();
            hostInfoDto.setHostId(hostInfo.getHostId());
            hostInfoDto.setHostIp(hostInfo.getHostIp());
            hostInfoDto.setProjectId(hostInfo.getProjectId());
            hostInfoDto.setPort(hostInfo.getPort());
            hostInfoDto.setUser(hostInfo.getUser());
            hostInfoDto.setPassword(HostUtil.decryptDES(hostInfo.getPassword()));
            String additionValue = hostInfo.getAdditionValue();
            if(!StringUtils.isEmpty(additionValue)|| "null".equals(additionValue)) {
                List<HostDetail> hostDetails = JSONObject.parseArray(additionValue,HostDetail.class);
                hostInfoDto.setHostDetailList(hostDetails);
            }
            hostInfoDtos.add(hostInfoDto);
        }
        return ResponseUtil.renderTableResponse(hostInfoDtos);
    }
    @RequestMapping(value = "/project1/{id}", method = RequestMethod.GET)
    public Map<String, Object> findByProjectId1(@PathVariable Long id) {
        List<HostInfo> hostInfos = hostInfoRepository.findByProjectId(id);
        List<HostInfoDto> hostInfoDtos = new ArrayList<>(hostInfos.size());
        for (HostInfo hostInfo : hostInfos) {
            HostInfoDto hostInfoDto = new HostInfoDto();
            hostInfoDto.setHostId(hostInfo.getHostId());
            hostInfoDto.setHostIp(hostInfo.getHostIp());
            hostInfoDto.setProjectId(hostInfo.getProjectId());
            hostInfoDto.setPort(hostInfo.getPort());
            hostInfoDto.setUser(hostInfo.getUser());
            hostInfoDto.setPassword(HostUtil.decryptDES(hostInfo.getPassword()));
            List<HostDetail> hostDetails = hostDetailRepository.findByHostId(hostInfo.getHostId());
            hostInfoDto.setHostDetailList(hostDetails);
            hostInfoDtos.add(hostInfoDto);
        }
        return ResponseUtil.renderTableResponse(hostInfoDtos);
    }


    @Transactional
    @RequestMapping(method = RequestMethod.DELETE)
    public Map<String, Object> delete(@RequestParam("hostId") Long hostId) {
//        hostInfoRepository.
        Map<String, Object> map = new HashMap<>();
        hostDetailRepository.deleteByHostId(hostId);
        hostInfoRepository.deleteById(hostId);
        map.put("result", !hostInfoRepository.existsById(hostId));
        return map;
    }
}
