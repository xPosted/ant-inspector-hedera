package com.antinspector.hedera.relations.dto.integration;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class TokenResponseDto {
    List<TokenDto> tokens;
    LinksDto links;

    @Value
    @Builder
    public static class AdminKeyDto {
        String _type;
        String key;
    }

    @Value
    @Builder
    public static class LinksDto {
        String next;
    }
}
