package com.ztesoft.config.compare.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class FileInfo {
    @Id
    @GeneratedValue
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
    private String valueMap;
}
