package com.ztesoft.config.compare;

import com.ztesoft.config.compare.dto.ContentValueInfo;
import com.ztesoft.config.compare.entity.FileInfo;
import com.ztesoft.config.compare.service.FileService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FilecompareApplicationTests {
    @Autowired
    private FileService fileService;

    @Ignore
    @Test
    public void contextLoads() {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileId(444L);
        fileInfo.setTarget("E:\\Documents\\test\\127.0.0.1\\system.cfg");
        fileInfo.setSource("E:\\Documents\\test\\127.0.0.1\\system1.cfg");
        fileInfo.setMethod(1);
        fileInfo.setType(1);
        fileInfo.setValueMap("");
        Map<String,Object> map = fileService.compareConfig(fileInfo);
        List<ContentValueInfo> list = (List<ContentValueInfo>) map.get("result");
        for (ContentValueInfo m: list){
            System.out.println(m.getName() + "\t" + m.getSourceValue() + "\t" + m.getTargetValue() + "\t" + m.getStatus() + "\t" + m.getMethod());
        }
    }

}
