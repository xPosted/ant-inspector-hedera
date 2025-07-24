package com.sicknet.hedera.relations.configuration;

import com.sicknet.hedera.relations.graph.AccountsInMemoryStorage;
import com.sicknet.hedera.relations.integration.HederaTokenClient;
import com.sicknet.hedera.relations.service.tokens.TokeInfoLocalDataSource;
import com.sicknet.hedera.relations.service.tokens.TokenInfoDataSource;
import com.sicknet.hedera.relations.service.tokens.TokenInfoExternalDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfiguration {

    @Bean
    public AccountsInMemoryStorage accountsMetaInfo(ConfigProperties properties) {
        return new AccountsInMemoryStorage(0L, 0L,
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
