package com.sicknet.hedera.relations.service;

import com.sicknet.hedera.relations.configuration.ConfigProperties;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

@Configuration
public class UpdateScheduler {

    @Autowired
    private GraphBuilder graphBuilder;
    @Autowired
    private ConfigProperties configProperties;

    @SneakyThrows
    @Scheduled(fixedRate = 2, timeUnit = TimeUnit.HOURS)
    public void updateGraph() {
        var homeDir = configProperties.getConfiguration().getHomeDir();
        var fileName = configProperties.getConfiguration().getMetaFileName();
        graphBuilder.readNewTransactionsIfExist(homeDir, fileName);
        System.out.println("Graph updated successfully");

// 0.0.6986681 -> 0.0.7010279
        // 0.0.7016900 -> 0.0.6705700
        // 0.0.6092602 and 0.0.7253422 -> 0.0.2283230 ?


    }

}
