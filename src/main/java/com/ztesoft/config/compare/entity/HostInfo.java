package com.ztesoft.config.compare.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class HostInfo {
    @Id
    @GeneratedValue
    private Long hostId;
    private String hostIp;
    private Long projectId;
}
