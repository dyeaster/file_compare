package com.ztesoft.config.compare.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FileInfoDto {
    private Long fileId;
    private Long hostId;
    private Long projectId;
    private String source;
    private String target;
    // 1,property 2.ini 0.others
    private Integer type;
    // 1.identical,2.exists,0.ignore
    private Integer method;
    private Integer dirFlag;
    private String exclude;
    private List<ReplaceValue> replaceList;
}
