package com.antinspector.hedera.relations.service;

import com.antinspector.hedera.relations.configuration.ConfigProperties;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class UpdateScheduler {

    @Autowired
    private GraphBuilder graphBuilder;
    @Autowired
    private ConfigProperties configProperties;

    @SneakyThrows
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void updateGraph() {
        var homeDir = configProperties.getConfiguration().getHomeDir();
        var fileName = configProperties.getConfiguration().getMetaFileName();
        graphBuilder.readNewTransactionsIfExist(homeDir, fileName);
        // 0.0.6986681 -> 0.0.7010279
        // 0.0.7016900 -> 0.0.6705700
    }

}
