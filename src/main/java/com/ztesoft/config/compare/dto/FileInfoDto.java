package com.ztesoft.config.compare.dto;

import lombok.Data;
import org.springframework.stereotype.Component;


@Data
@Component
public class FileInfoDto {
    private Long id;
    private String source;
    private String target;
    // 1,property 2.ini 3.others
    private Integer type;
    // 1.identical,2.exists,3.ignore
    private Integer method;
    private String dirFlag;
    private String exclude;
    private String valueMap;
    private String updateTime;
    private String comments;
//    private String fileName;
}
