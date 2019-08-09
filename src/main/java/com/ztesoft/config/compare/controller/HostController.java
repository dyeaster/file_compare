package com.ztesoft.config.compare.controller;

import com.ztesoft.config.compare.entity.*;
import com.ztesoft.config.compare.repository.HostDetailRepository;
import com.ztesoft.config.compare.repository.HostInfoRepository;
import com.ztesoft.config.compare.utils.HostUtil;
import com.ztesoft.config.compare.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
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

    @RequestMapping(method = RequestMethod.POST)
    public Map<String, Object> insert(@RequestBody Map<String, String> param) {
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
    public Map<String, Object> update(@RequestBody Map<String, String> param) {
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
        List<HostDetail> hostDetails = hostDetailRepository.findByHostId(id);
//            Map<String, Object> map = HostUtil.hostDetailList2Map(hostDetails);
        return hostDetails;
    }

    @RequestMapping(value = "/project/{id}", method = RequestMethod.GET)
    public Map<String, Object> findByProjectId(@PathVariable Long id) {
        List<HostInfo> hostInfos = hostInfoRepository.findByProjectId(id);
        List<Map<String, Object>> result = new ArrayList<>(hostInfos.size());
        for (HostInfo hostInfo : hostInfos) {
            List<HostDetail> hostDetails = hostDetailRepository.findByHostId(hostInfo.getHostId());
            Map<String, Object> map = HostUtil.hostDetailList2Map(hostDetails);
            map.put("hostIp", hostInfo.getHostIp());
            map.put("projectId", id);
            map.put("hostId", hostInfo.getHostId());
            result.add(map);
        }
        return ResponseUtil.renderTableResponse(result);
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
