package com.sicknet.hedera.relations.service.tokens;

import com.sicknet.hedera.relations.configuration.ConfigProperties;
import com.sicknet.hedera.relations.dto.TokenInfoOperationDto;
import com.sicknet.hedera.relations.dto.integration.TokenDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashSet;
import java.util.stream.Collectors;

@AllArgsConstructor
public class TokeInfoLocalDataSource implements TokenInfoDataSource {

    private final ConfigProperties configProperties;
    @Getter
    private final int priority;

    @Override
    public TokenInfoOperationDto getTokenInfo(TokenInfoOperationDto tokenInfoOperationDto) {
        if (tokenInfoOperationDto.getTokenIds() == null || tokenInfoOperationDto.getTokenIds().isEmpty()) {
            return tokenInfoOperationDto;
        }
        var requestedTokenIds = tokenInfoOperationDto.getTokenIds();
        var staticAvailableTokenInfos = configProperties.getTokens().stream()
                .filter(t -> requestedTokenIds.contains(t.getToken_id()))
                .toList();
        return tokenInfoOperationDto.updateWithTokenInfoResponse(staticAvailableTokenInfos);
    }
}
