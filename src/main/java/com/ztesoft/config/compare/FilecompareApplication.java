package com.ztesoft.config.compare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class FilecompareApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilecompareApplication.class, args);
    }

}
