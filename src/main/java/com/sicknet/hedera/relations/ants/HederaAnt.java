package com.sicknet.hedera.relations.ants;

import com.sicknet.hedera.relations.ants.exception.NoAnyAvailableRelationsException;
import com.sicknet.hedera.relations.graph.Account;
import lombok.Builder;
import lombok.Value;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HederaAnt {

    private final String sourceAccountId;
    private String currentAccountId;
    private final String targetAccountId;
    private final List<String> visitedAccounts;
    private final int pheromoneFactor;
    private final Function<String, Optional<Double>> pheromoneValueProvider;
    private final Function<String, Account> accountProvider;
    private final Consumer<AntPath> resultConsumer;

    public HederaAnt(String sourceAccountId, String targetAccountId, int pheromoneFactor,
                     Function<String, Optional<Double>> pheromoneValueProvider,
                     Function<String, Account> accountProvider,
                     Consumer<AntPath> resultConsumer) {
        this.sourceAccountId = sourceAccountId;
        this.currentAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.visitedAccounts = new ArrayList<>(List.of(sourceAccountId));
        this.pheromoneFactor = pheromoneFactor;
        this.pheromoneValueProvider = pheromoneValueProvider;
        this.accountProvider = accountProvider;
        this.resultConsumer = resultConsumer;
    }

    public void findPath() {
        try {
            while (!visitedAccounts.contains(targetAccountId)) {
                var nextStep = findNextStep();
                visitedAccounts.add(nextStep);
                currentAccountId = nextStep;
            }
            if (visitedAccounts.contains(targetAccountId)) {
                resultConsumer.accept(AntPath.builder()
                        .visitedAccounts(visitedAccounts)
                        .sourceAccountId(sourceAccountId)
                        .targetAccountId(targetAccountId)
                        .build());
            }
        } catch (NoAnyAvailableRelationsException e) {
            // Handle the case where no relations are available
    //        System.err.println("Ant has stucked on account: " + currentAccountId + ", "+visitedAccounts);
        }

    }

    private String findNextStep() throws NoAnyAvailableRelationsException {
        var acc = accountProvider.apply(currentAccountId);
        if (acc.getDebitRelations().containsKey(targetAccountId)) {
            return targetAccountId;
        }
        var relationValues = acc.getDebitRelations().keySet().stream()
                .filter(relatedAccountId -> !visitedAccounts.contains(relatedAccountId))
                .collect(Collectors.toMap(Function.identity(), v -> getRelationValue(v, acc)));
        return getRandomRelation(relationValues).getKey();
    }

    private Map.Entry<String, Double> getRandomRelation(Map<String, Double> relationValues) throws NoAnyAvailableRelationsException {
        var random = new Random();
        var randomValue = random.nextDouble() * relationValues.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        double cumulativeValue = 0.0;
        for (var entry : relationValues.entrySet()) {
            cumulativeValue += entry.getValue();
            if (cumulativeValue >= randomValue) {
                return entry;
            }
        }
        throw new NoAnyAvailableRelationsException();
    }

    private Double getRelationValue(String relatedAccountId, Account currentAccount) {
        var pheromoneValue = getPheromoneValue(relatedAccountId);
        var tokenValuesSum = (double) 100 / currentAccount.getDebitRelations().size();
        return pheromoneValue * tokenValuesSum;
    }

    private Double getPheromoneValue(String relatedAccountId) {
        return pheromoneValueProvider.apply(relatedAccountId)
                .map(pheromoneValue -> pheromoneValue * pheromoneFactor)
                .orElse(1D);
    }

    @Value
    @Builder
    public static class AntPath {
        String sourceAccountId;
        String targetAccountId;
        List<String> visitedAccounts;
        Integer complexity;
    }

}
