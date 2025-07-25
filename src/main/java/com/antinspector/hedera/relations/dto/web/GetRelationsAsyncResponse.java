package com.antinspector.hedera.relations.dto.web;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GetRelationsAsyncResponse {
    String hillId;
}
