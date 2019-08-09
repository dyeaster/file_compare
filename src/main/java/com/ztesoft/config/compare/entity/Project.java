package com.ztesoft.config.compare.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class Project {
    @Id
    @GeneratedValue
    private Long projectId;
    private String name;
    private String comments;
}
