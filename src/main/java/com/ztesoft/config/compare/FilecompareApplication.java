package com.ztesoft.config.compare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication()
//@EntityScan("com.ztesoft.config.compare.entity")
//@EnableJpaRepositories("com.ztesoft.config.compare.repository")
public class FilecompareApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilecompareApplication.class, args);
    }

}
