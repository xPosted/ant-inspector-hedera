package com.antinspector.hedera.relations.graph;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.antinspector.hedera.relations.dto.TransferInfo;
import lombok.Builder;
import lombok.Value;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Value
@Builder(toBuilder = true)
public class Account {

    String accountId;
    Map<String, Map<String, TransactionsSummary>> debitRelations;
    Map<String, Map<String, TransactionsSummary>> creditRelations;
    Map<String, TransactionsSummary> debitTokenSummaries;
    Map<String, TransactionsSummary> creditTokenSummaries;

    public static Account buildNew(String accountId) {
        return Account.builder().accountId(accountId)
                .debitRelations(new HashMap<>())
                .creditRelations(new HashMap<>())
                .debitTokenSummaries(new HashMap<>())
                .creditTokenSummaries(new HashMap<>())
                .build();
    }

    @JsonIgnore
    public Long getTotalTransmitAmount(String tokenId) {
        return debitTokenSummaries.get(tokenId).getTotalAmount();
    }

    @JsonIgnore
    public Long getTotalReceiveAmount(String tokenId) {
        return creditTokenSummaries.get(tokenId).getTotalAmount();
    }

    @JsonIgnore
    public Long getTotalTransmitCount(String tokenId) {
        return (long) debitTokenSummaries.get(tokenId).getTransactionIds().size();
    }

    @JsonIgnore
    public Long getTotalReceiveCount(String tokenId) {
        return (long) creditTokenSummaries.get(tokenId).getTransactionIds().size();
    }

    @JsonIgnore
    public Long getTransmitAmount(String relatedAccountId, String tokenId) {
        return debitRelations.get(relatedAccountId).get(tokenId).getTotalAmount();
    }

    @JsonIgnore
    public Integer getTransmitCount(String relatedAccountId, String tokenId) {
        return debitRelations.get(relatedAccountId).get(tokenId).getTransactionIds().size();
    }

    Account addTransfer(TransferInfo transferInfo) {
        var relation = new HashMap<String, TransactionsSummary>();
        relation.put(transferInfo.getTokenId(), transferInfo.getSummary());
        if (transferInfo.getSender().equals(accountId)) {
            return addDebitRelation(transferInfo.getReceiver(), relation);
        }
        if (transferInfo.getReceiver().equals(accountId)) {
            return addCreditRelation(transferInfo.getSender(), relation);
        }
        throw new IllegalArgumentException("Invalid transfer info: " + transferInfo + " for account: " + accountId);
    }

    Account addDebitRelation(String receiver, Map<String, TransactionsSummary> summary) {
        debitRelations.merge(receiver, summary, this::mergeTokenRelationMaps);
        return this.toBuilder()
                .debitRelations(debitRelations)
                .build();
    }

    Account addCreditRelation(String sender, Map<String, TransactionsSummary> summary) {
        creditRelations.merge(sender, summary, this::mergeTokenRelationMaps);
        return this.toBuilder()
                .creditRelations(creditRelations)
                .build();
    }

    void runPostUpdateCalculations() {
        debitTokenSummaries.putAll(getTokenSummaries(debitRelations));
        creditTokenSummaries.putAll(getTokenSummaries(creditRelations));
    }

    private Map<String, TransactionsSummary> getTokenSummaries(Map<String, Map<String, TransactionsSummary>> relations) {
        return relations.values().stream()
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.reducing(TransactionsSummary.empty(), TransactionsSummary::merge))));
    }

    private Map<String, TransactionsSummary> mergeTokenRelationMaps(Map<String, TransactionsSummary> current, Map<String, TransactionsSummary> newRelations) {
        newRelations.forEach((key, value) -> current.merge(key, value, TransactionsSummary::merge));
        return current;
    }

}
