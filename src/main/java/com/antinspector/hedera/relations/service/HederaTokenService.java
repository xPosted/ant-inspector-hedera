package com.antinspector.hedera.relations.service;

import com.antinspector.hedera.relations.dto.TokenInfoOperationDto;
import com.antinspector.hedera.relations.dto.integration.TokenDto;
import com.antinspector.hedera.relations.service.tokens.TokenInfoDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HederaTokenService {

    private final List<TokenInfoDataSource> tokenInfoDataSources;

    public List<TokenDto> getTokenInfos(Set<String> tokenIds) {
        var tokenInfoOperationDto = TokenInfoOperationDto.builder()
                .tokenIds(tokenIds)
                .build();
        return tokenInfoDataSources.stream()
                .sorted(Comparator.comparingInt(TokenInfoDataSource::getPriority).reversed())
                .reduce(tokenInfoOperationDto, (operationDto, dataSource) -> dataSource.getTokenInfo(operationDto), (x, y) -> x)
                .getTokenInfoResponses();
    }

}
