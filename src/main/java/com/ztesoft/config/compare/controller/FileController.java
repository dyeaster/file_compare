package com.ztesoft.config.compare.controller;

import com.alibaba.fastjson.JSON;
import com.ztesoft.config.compare.dto.FileInfoDto;
import com.ztesoft.config.compare.dto.ReplaceValue;
import com.ztesoft.config.compare.entity.FileInfo;
import com.ztesoft.config.compare.repository.FileInfoRepository;
import com.ztesoft.config.compare.utils.FileInfoUtil;
import com.ztesoft.config.compare.utils.ResponseUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/file")
public class FileController {

    @Autowired
    private FileInfoRepository fileInfoRepository;

    @RequestMapping(method = RequestMethod.GET)
    public Map<String, Object> findAll() {
        List<FileInfo> fileInfos = fileInfoRepository.findAll();
        return ResponseUtil.renderTableResponse(fileInfos);
    }

    @RequestMapping(value = "/host/{id}", method = RequestMethod.GET)
    public Map<String, Object> findByHostId(@PathVariable Long id) {
//        List<FileInfo> fileInfos = fileInfoRepository.findByHostId(Long.valueOf(id));
        List<FileInfo> fileInfos = fileInfoRepository.findByHostId(id);
        List<FileInfoDto> fileInfoDtos = new ArrayList<>(fileInfos.size());
        for(FileInfo fileInfo:fileInfos) {
            FileInfoDto fileInfoDto = new FileInfoDto();
            BeanUtils.copyProperties(fileInfo,fileInfoDto);
            String valueMapStr = fileInfo.getValueMap();
            if(valueMapStr.startsWith("[{") && valueMapStr.endsWith("}]")) {
                fileInfoDto.setReplaceList(JSON.parseArray(fileInfo.getValueMap(), ReplaceValue.class));
            }
            fileInfoDtos.add(fileInfoDto);
        }
        return ResponseUtil.renderTableResponse(fileInfoDtos);
    }

    @RequestMapping(method = RequestMethod.POST)
    public FileInfo insert(@RequestBody FileInfoDto fileInfoDto) {
        FileInfo fileInfo = new FileInfo();
        BeanUtils.copyProperties(fileInfoDto,fileInfo);
        List<ReplaceValue> valueList = fileInfoDto.getReplaceList();
//      删除字符串最后的文件分隔符
        fileInfo.setValueMap(JSON.toJSONString(valueList));
        FileInfoUtil.deleteEndSeparator(fileInfo);
        return fileInfoRepository.save(fileInfo);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public FileInfo update(@RequestBody  FileInfoDto fileInfoDto) {
        FileInfo fileInfo = new FileInfo();
        BeanUtils.copyProperties(fileInfoDto,fileInfo);
        List<ReplaceValue> valueList = fileInfoDto.getReplaceList();
//      删除字符串最后的文件分隔符
        fileInfo.setValueMap(JSON.toJSONString(valueList));
        FileInfoUtil.deleteEndSeparator(fileInfo);
        return fileInfoRepository.save(fileInfo);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public Map<String, Object> delete(@RequestBody FileInfo fileInfo) {
        Map<String, Object> map = new HashMap<>();
        if (fileInfoRepository.existsById(fileInfo.getFileId())) {
            fileInfoRepository.delete(fileInfo);
            map.put("result", !fileInfoRepository.existsById(fileInfo.getFileId()));
        } else {
            map.put("result", true);
        }
        return map;
    }
}
