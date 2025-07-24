package com.antinspector.hedera.relations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
@EnableFeignClients
public class AntInspectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AntInspectorApplication.class, args);
    }

}
