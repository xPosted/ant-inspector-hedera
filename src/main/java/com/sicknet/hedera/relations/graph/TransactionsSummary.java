package com.sicknet.hedera.relations.graph;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
public class TransactionsSummary {

    Set<String> transactionIds;
    Long totalAmount;

    public static TransactionsSummary empty() {
        return new TransactionsSummary(new HashSet<>(), 0L);
    }

    public static TransactionsSummary of(String transactionId, Long amount) {
        return new TransactionsSummary(
                new HashSet<>(List.of(transactionId)),
                amount
        );
    }

    public TransactionsSummary merge(TransactionsSummary other) {
        transactionIds.addAll(other.transactionIds);
        long mergedTotalAmount = totalAmount + other.totalAmount;
        return new TransactionsSummary(transactionIds, mergedTotalAmount);
    }

    public TransactionsSummary shortView() {
        return new TransactionsSummary(
                Collections.unmodifiableSet(transactionIds.stream().limit(10).collect(Collectors.toSet())),
                totalAmount
        );
    }

}
