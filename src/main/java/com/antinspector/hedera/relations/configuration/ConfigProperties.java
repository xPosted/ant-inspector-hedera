package com.antinspector.hedera.relations.configuration;

import com.antinspector.hedera.relations.dto.integration.TokenDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "sicknet.hedera")
@Data
public class ConfigProperties {

    private List<TokenDto> tokens;
    private TransctionsFSStorageConfig configuration;
    private SystemConfig system;

    @Value
    @AllArgsConstructor
    public static class SystemConfig {
        List<String> accounts;
    }

    @Value
    @AllArgsConstructor
    public static class TransctionsFSStorageConfig {
        String homeDir;
        String metaFileName;
    }

}
