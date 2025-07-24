package com.antinspector.hedera.relations.dto;

import com.antinspector.hedera.relations.dto.integration.TokenDto;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
@Builder
public class TokenInfoOperationDto {

    Set<String> tokenIds;
    @Singular
    List<TokenDto> tokenInfoResponses;

    public TokenInfoOperationDto updateWithTokenInfoResponse(List<TokenDto> tokenDtos) {
        var tokenLeft = tokenIds.stream().filter(t -> tokenDtos.stream()
                        .noneMatch(tokenDto -> tokenDto.getToken_id().equals(t)))
                .collect(Collectors.toUnmodifiableSet());
        var updatedResponseList = Stream.concat(tokenDtos.stream(), tokenInfoResponses.stream()).toList();
        return builder()
                .tokenIds(tokenLeft)
                .tokenInfoResponses(updatedResponseList)
                .build();
    }

}
