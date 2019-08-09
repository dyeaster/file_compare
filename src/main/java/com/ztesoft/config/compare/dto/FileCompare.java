package com.ztesoft.config.compare.dto;

import lombok.Data;

@Data
public class FileCompare {
    public String targetFile;
    public String sourceFile;
    public String type;
    public String method;
}
