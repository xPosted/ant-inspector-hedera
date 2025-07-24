package com.sicknet.hedera.relations.service.tokens;

import com.sicknet.hedera.relations.dto.TokenInfoOperationDto;

public interface TokenInfoDataSource {

    TokenInfoOperationDto getTokenInfo(TokenInfoOperationDto tokenInfoOperationDto);
    int getPriority();

}
