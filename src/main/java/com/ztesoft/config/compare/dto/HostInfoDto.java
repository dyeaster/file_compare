package com.ztesoft.config.compare.dto;

import com.ztesoft.config.compare.entity.HostDetail;
import lombok.Data;

import java.util.List;

@Data
public class HostInfoDto {
    private Long hostId;
    private String hostIp;
    private Long projectId;
    private Integer port;
    private String user;
    private String password;
    private List<HostDetail> hostDetailList;
}
