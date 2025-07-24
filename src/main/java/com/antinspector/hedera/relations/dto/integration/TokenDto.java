package com.antinspector.hedera.relations.dto.integration;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TokenDto {
    TokenResponseDto.AdminKeyDto admin_key;
    String metadata;
    String name;
    String symbol;
    String token_id;
    String type;
    int decimals;
}
