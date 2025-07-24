package com.antinspector.hedera.relations.service.tokens;

import com.antinspector.hedera.relations.configuration.ConfigProperties;
import com.antinspector.hedera.relations.dto.TokenInfoOperationDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

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
