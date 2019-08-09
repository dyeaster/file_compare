package com.ztesoft.config.compare.controller;

import com.ztesoft.config.compare.entity.*;
import com.ztesoft.config.compare.repository.FileInfoRepository;
import com.ztesoft.config.compare.repository.HostDetailRepository;
import com.ztesoft.config.compare.repository.HostInfoRepository;
import com.ztesoft.config.compare.repository.ProjectRepository;
import com.ztesoft.config.compare.service.FileService;
import com.ztesoft.config.compare.utils.HostUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(value = "/compare")
public class CompareController {

    @Autowired
    private HostInfoRepository hostInfoRepository;

    @Autowired
    private FileInfoRepository fileInfoRepository;
    @Autowired
    private HostDetailRepository hostDetailRepository;
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private FileService fileService;

    private Map<String, Map<String, Object>> compareResult = new HashMap<>();


    /**
     * 获取所有的服务器及服务器下的配置文件列表
     *
     * @return
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<Map<String, Object>> getHost() {
        List<HostInfo> hostInfos = hostInfoRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>(hostInfos.size());
        for (HostInfo hostInfo : hostInfos) {
            List<HostDetail> hostDetails = hostDetailRepository.findByHostId(hostInfo.getHostId());
            Map<String, Object> map = HostUtil.hostDetailList2Map(hostDetails);
            map.put("hostIp", hostInfo.getHostIp());
//            map.put("projectId", id);
            map.put("hostId", hostInfo.getHostId());
            result.add(map);
        }
        return result;
    }

    @RequestMapping(value = "/collect", method = RequestMethod.POST)
    public Map<String, Object> collectFile(@RequestParam("hostId") Long id) {
        Map<String, Object> map = new HashMap<>();
        compareResult = new HashMap<>();
        List<FileInfo> fileInfos = fileInfoRepository.findByHostId(id);
        List<Map<String, Object>> list = fileService.collectFileById(id);
        map.put("result", list);
        map.put("files", fileInfos);
        return map;
    }

    @RequestMapping(value = "/execute", method = RequestMethod.POST)
    public Map<String, Map<String, Object>> compareFile(@RequestParam("hostId") Long hostId,
                                                        @RequestParam("fileIds[]") Long[] fileIds) {
        HostInfo hostInfo = hostInfoRepository.getOne(hostId);
        List<FileInfo> fileInfos = fileInfoRepository.findAllById(Arrays.asList(fileIds));
        Map<Long, Object> map = new HashMap<>();
        for (FileInfo fileInfo : fileInfos) {
            compareResult.put("file" + fileInfo.getFileId(), fileService.compareFile(hostInfo, fileInfo));
        }
        return compareResult;
    }

    @RequestMapping(value = "/result", method = RequestMethod.POST)
    public Map<String, Object> getResultByIpAndFile(@RequestParam("fileId") Long fileId) {
        Map<String, Object> map = compareResult.get("file" + fileId);
        return map;
    }

    @RequestMapping(value = "/sync", method = RequestMethod.POST)
    public Map<String, Object> syncFile2Server(@RequestParam("hostIp") String hostIp,
                                               @RequestParam("user") String user,
                                               @RequestParam("fileId[]") Long[] fileId) {
        Map<String, Object> map = new HashMap<>();
        List<FileInfo> fileInfos = fileInfoRepository.findAllById(Arrays.asList(fileId));
        for (FileInfo fileInfo : fileInfos) {
            fileService.transfer2Server(fileInfo.getSource(), fileInfo.getTarget(), hostIp, user);
        }
        return null;
    }

    @RequestMapping(value = "/sync1", method = RequestMethod.POST)
    public List<Map<String, Object>> syncFile2Server1(@RequestParam("fileId[]") Long[] fileId) {
        List<Map<String, Object>> list = new ArrayList<>(fileId.length);
        List<FileInfo> fileInfos = fileInfoRepository.findAllById(Arrays.asList(fileId));
        for (FileInfo fileInfo : fileInfos) {
            list.add(fileService.syncFile2Server(fileInfo));
        }
        return list;
    }
}
