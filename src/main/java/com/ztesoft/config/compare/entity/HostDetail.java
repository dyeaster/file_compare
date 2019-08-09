package com.ztesoft.config.compare.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class HostDetail {
    @Id
    @GeneratedValue
    private Long id;
    private Long hostId;
    private String key;
    private String value;
}
