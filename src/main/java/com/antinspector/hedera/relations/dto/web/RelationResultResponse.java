package com.antinspector.hedera.relations.dto.web;

import com.antinspector.hedera.relations.graph.TransactionsSummary;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class RelationResultResponse {
    String source;
    String target;
    List<RelationItem> relations;

    @Value
    @Builder(toBuilder = true)
    public static class RelationItem {
        String id;
        List<TokenRelationItem> tokenRelations;
    }

    @Value
    @Builder(toBuilder = true)
    public static class TokenRelationItem {
        String tokenId;
        String symbol;
        String name;
        TransactionsSummary transactions;
    }

}
