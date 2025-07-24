package com.antinspector.hedera.relations.service;

import com.antinspector.hedera.relations.ants.HederaAntHill;
import com.antinspector.hedera.relations.graph.AccountsInMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class HillFactory {

    private static final int ANTS_COUNT = 400;
    private static final int WAVES_COUNT = 3;

    @Autowired
    private AccountsInMemoryStorage accountsInMemoryStorage;

    private final Map<String, HederaAntHill> hederaAntHills = new HashMap<>();

    public HederaAntHill getHederaAntHill(String jobId) {
        if (hederaAntHills.containsKey(jobId)) {
            return hederaAntHills.get(jobId);
        } else {
            throw new IllegalArgumentException("Hill with jobId " + jobId + " does not exist.");
        }
    }

    public HederaAntHill createDefaultHill(String sourceAccountId, String targetAccountId) {
        return createHill(sourceAccountId, targetAccountId, ANTS_COUNT, WAVES_COUNT);
    }

    public HederaAntHill createHill(String sourceAccountId, String targetAccountId, int antsCount, int wavesCount) {
        var id = UUID.randomUUID().toString();
        var hill = new HederaAntHill(id, sourceAccountId, targetAccountId, antsCount, wavesCount, accountsInMemoryStorage);
        hederaAntHills.put(id, hill);
        return hill;
    }

}
