package com.ztesoft.config.compare.dto;

import lombok.Data;


@Data
public class FileValueInfo {
    private String fileName;
    //    1 same, 2 diff, 3. lack 4 additional
    private String status;
    private Long sourceLength;
    private Long targetLength;
    private String sourceContent;
    private String targetContent;
}
