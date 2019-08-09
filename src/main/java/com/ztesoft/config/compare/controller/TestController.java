package com.ztesoft.config.compare.controller;

import com.ztesoft.config.compare.entity.*;
import com.ztesoft.config.compare.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class TestController {

//    @Autowired
//    private ServerConfig serverConfig;
//
//    @Autowired
//    private FileService fileService;

    @RequestMapping(value = "/compare/{ip}")
    public String compareByIp(@PathVariable("ip") String ip) {
        System.out.println(ip);
        return null;
    }


//    @RequestMapping(value = "/server")
//    public ServerConfig serverConfig() {
//        return serverConfig;
//    }

//    @RequestMapping(value = "/collectFile/{ip}", method = RequestMethod.POST)
//    public Map<String, Object> collectFile(@RequestBody HostInfo host) {
//        Map<String, Object> map;
//        map = fileService.collectFileByIp(host);
//        return map;
//    }


    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("111", "222");
        System.out.println(map.get("111"));
        System.out.println(".................");
        System.out.println(map.get("222"));
    }

}
