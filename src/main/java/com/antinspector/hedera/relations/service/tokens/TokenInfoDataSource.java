package com.antinspector.hedera.relations.service.tokens;

import com.antinspector.hedera.relations.dto.TokenInfoOperationDto;

public interface TokenInfoDataSource {

    TokenInfoOperationDto getTokenInfo(TokenInfoOperationDto tokenInfoOperationDto);
    int getPriority();

}
