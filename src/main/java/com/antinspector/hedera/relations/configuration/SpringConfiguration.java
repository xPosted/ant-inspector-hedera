package com.antinspector.hedera.relations.configuration;

import com.antinspector.hedera.relations.graph.AccountsInMemoryStorage;
import com.antinspector.hedera.relations.integration.HederaTokenClient;
import com.antinspector.hedera.relations.service.tokens.TokeInfoLocalDataSource;
import com.antinspector.hedera.relations.service.tokens.TokenInfoDataSource;
import com.antinspector.hedera.relations.service.tokens.TokenInfoExternalDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfiguration {

    @Bean
    public AccountsInMemoryStorage accountsMetaInfo(ConfigProperties properties) {
        return new AccountsInMemoryStorage(Long.MAX_VALUE, 0L,
                properties.getSystem().getAccounts());
    }

    @Bean
    public TokenInfoDataSource tokenInfoLocalDataSource(ConfigProperties properties) {
        return new TokeInfoLocalDataSource(properties, 1);
    }

    @Bean
    public TokenInfoDataSource tokenInfoRemoteDataSource(HederaTokenClient hederaTokenClient) {
        return new TokenInfoExternalDataSource(hederaTokenClient, 0);
    }

}
