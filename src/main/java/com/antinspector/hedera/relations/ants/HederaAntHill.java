package com.antinspector.hedera.relations.ants;

import com.antinspector.hedera.relations.graph.Account;
import com.antinspector.hedera.relations.graph.AccountsInMemoryStorage;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class HederaAntHill {

    @Getter
    String hillId;
    @Getter
    String sourceAccountId;
    @Getter
    String targetAccountId;
    Map<String, Double> pheromoneAcountMap;
    Set<List<String>> paths;
    int antCount;
    int waves;
    AccountsInMemoryStorage accountsMetaInfo;
    CompletableFuture<Set<List<String>>> relationsResult;


    public HederaAntHill(String hillId, String sourceAccountId, String targetAccountId, int antCount, int waves, AccountsInMemoryStorage accountsMetaInfo) {
        this.hillId = hillId;
        this.sourceAccountId = sourceAccountId;
        this.targetAccountId = targetAccountId;
        this.pheromoneAcountMap = new HashMap<>();
        this.paths = new HashSet<>();
        this.antCount = antCount;
        this.waves = waves;
        this.accountsMetaInfo = accountsMetaInfo;
    }

    public void runAntsAsync() {
        relationsResult = CompletableFuture.supplyAsync(this::findRelationsSync);
    }

    public boolean isDone() {
        return relationsResult != null && relationsResult.isDone();
    }

    public Set<List<String>> getRelationsSync() {
        return relationsResult.join();
    }

    public Set<List<String>> findRelationsSync() {
        for (int i = 0; i < waves; i++) {
            Stream.generate(this::generateAnt)
                    .limit(antCount)
                    .parallel()
                    .forEach(HederaAnt::findPath);
        }
        return paths;
    }

    private HederaAnt generateAnt() {
        return new HederaAnt(sourceAccountId, targetAccountId, 2,
                this::getPheromoneValue, this::getAccount, this::onSuccess);
    }

    public void onSuccess(HederaAnt.AntPath antPath) {
        var successfulPath = antPath.getVisitedAccounts();
        paths.add(successfulPath);
        antPath.getVisitedAccounts()
                .forEach(account -> pheromoneAcountMap.compute(account,
                        (k, v) -> {
                            var newValue = 1 + (1 - successfulPath.size()/100.0);
                            return v != null && v < newValue ? Double.valueOf(newValue) : v;
                        }));
    }

    public Optional<Double> getPheromoneValue(String account) {
        return Optional.ofNullable(pheromoneAcountMap.get(account));
    }

    private Account getAccount(String accountId) {
        return accountsMetaInfo
                .getAccount(accountId)
                .orElseGet(() -> {
                    throw new IllegalArgumentException("Account not found: " + accountId);
                });
    }


}
