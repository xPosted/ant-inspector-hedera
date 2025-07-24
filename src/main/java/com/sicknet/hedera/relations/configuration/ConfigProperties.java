package com.sicknet.hedera.relations.configuration;

import com.sicknet.hedera.relations.dto.integration.TokenDto;
import com.sicknet.hedera.relations.dto.integration.TokenResponseDto;
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
    private HederaStorageConfig configuration;
    private SystemConfig system;

    @Value
    @AllArgsConstructor
    public static class SystemConfig {
        List<String> accounts;
    }

    @Value
    @AllArgsConstructor
    public static class HederaStorageConfig {
        String homeDir;
        String metaFileName;
        String accountsGraphFileName;
    }

}
