package com.sicknet.hedera.relations.service.tokens;

import com.sicknet.hedera.relations.dto.TokenInfoOperationDto;
import com.sicknet.hedera.relations.dto.integration.TokenDto;
import com.sicknet.hedera.relations.dto.integration.TokenResponseDto;
import com.sicknet.hedera.relations.integration.HederaTokenClient;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class TokenInfoExternalDataSource implements TokenInfoDataSource {

    private final HederaTokenClient hederaTokenClient;
    @Getter
    private final int priority;

    @Override
    public TokenInfoOperationDto getTokenInfo(TokenInfoOperationDto tokenInfoOperationDto) {
        if (tokenInfoOperationDto.getTokenIds() == null || tokenInfoOperationDto.getTokenIds().isEmpty()) {
            return tokenInfoOperationDto;
        }
        var tokenInfoResponse = getTokenById(tokenInfoOperationDto.getTokenIds());
        return tokenInfoOperationDto.updateWithTokenInfoResponse(tokenInfoResponse);
    }

    public List<TokenDto> getTokenById(Set<String> tokenId) {
        TokenResponseDto response = hederaTokenClient.getTokenById(tokenId);
        if (response.getTokens() != null && !response.getTokens().isEmpty()) {
            return response.getTokens();
        }
        throw new IllegalArgumentException("Tokens response is empty, request: " + tokenId);
    }
}
