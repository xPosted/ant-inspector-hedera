package com.sicknet.hedera.relations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
@EnableFeignClients
public class HbarRelationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(HbarRelationsApplication.class, args);
    }

}
