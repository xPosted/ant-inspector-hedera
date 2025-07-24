package com.sicknet.hedera.relations.dto.web;

import com.sicknet.hedera.relations.graph.TransactionsSummary;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class RelationResult {
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
