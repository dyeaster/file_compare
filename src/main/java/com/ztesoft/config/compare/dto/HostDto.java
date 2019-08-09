package com.ztesoft.config.compare.dto;

import com.ztesoft.config.compare.entity.FileInfo;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class HostDto {
    private String hostIp;
    private String user;
    private List<FileInfo> fileInfoList;
}
