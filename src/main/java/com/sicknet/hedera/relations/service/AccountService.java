package com.sicknet.hedera.relations.service;

import com.sicknet.hedera.relations.ants.HederaAntHill;
import com.sicknet.hedera.relations.dto.web.HillJobStatus;
import com.sicknet.hedera.relations.dto.web.JobStatusEnum;
import com.sicknet.hedera.relations.dto.integration.TokenDto;
import com.sicknet.hedera.relations.dto.web.RelationResult;
import com.sicknet.hedera.relations.dto.web.SystemInfo;
import com.sicknet.hedera.relations.graph.Account;
import com.sicknet.hedera.relations.graph.AccountsInMemoryStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountsInMemoryStorage accountsInMemoryStorage;
    @Autowired
    private HillFactory hillFactory;
    @Autowired
    private HederaTokenService tokenService;
    @Autowired
    private GraphBuilder graphBuilder;


    public String findRelationAsync(String sourceAccountId, String targetAccountId, int antCount, int waves) {
        var hill = hillFactory.createHill(sourceAccountId, targetAccountId, antCount, waves);
        hill.runAntsAsync();
        return hill.getHillId();
    }

    public HillJobStatus getJobStatus(String jobId) {
        var hill = hillFactory.getHederaAntHill(jobId);
        if (hill.isDone()) {
            return HillJobStatus.of(JobStatusEnum.COMPLETED);
        }
        return HillJobStatus.of(JobStatusEnum.PENDING);
    }

    public Set getJobResult(String jobId, boolean extended) {
        var hill = hillFactory.getHederaAntHill(jobId);
        Set<List<String>> relationIds = hill.getRelationsSync();
        return extended ? buildExtendedResult(relationIds, hill.getHillId()) : relationIds;
    }

    public void runTest(boolean extendedResponse) {
        int steps = 0;
        int successful = 0;
        while (steps < 1000) {
            steps++;
            var sourceAccountId = accountsInMemoryStorage.getRandomAccountId();
            var targetAccountId = accountsInMemoryStorage.getRandomAccountId();
            var relations = findRelations(sourceAccountId, targetAccountId, 600, 2, extendedResponse);
            if (!relations.isEmpty()) {
                successful++;
                System.out.println("Found " + relations.size() + " paths between " + sourceAccountId + " and " + targetAccountId);
                System.out.println("Relations: " + relations);
            }
            System.out.println("Steps: " + steps + ", Successful: " + successful);
        }
    }

    public SystemInfo getSystemInfo() {
        var timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return SystemInfo.builder()
                .timeFrom(timeFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(accountsInMemoryStorage.getFromTs()), ZoneId.systemDefault())))
                .timeTo(timeFormatter.format(LocalDateTime.ofInstant(Instant.ofEpochSecond(accountsInMemoryStorage.getToTs()), ZoneId.systemDefault())))
                .accountsProcessed(accountsInMemoryStorage.getAccountIds().size())
                .transactionsProcessed(accountsInMemoryStorage.getTransactionCount())
                .storageAvailable(!graphBuilder.isStorageUpdateInProgress())
                .build();
    }

    public Account getAccountById(String accountId) {
        return accountsInMemoryStorage.getAccount(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
    }

    public Set findRelations(String sourceAccountId, String targetAccountId, int antCount, int waves, boolean extended) {
        HederaAntHill antHill = hillFactory.createHill(sourceAccountId, targetAccountId, antCount, waves);
        Set<List<String>> relationIds = antHill.findRelationsSync().stream()
                .sorted(Comparator.comparingInt(List::size))
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
        return extended ? buildExtendedResult(relationIds, antHill.getHillId()) : relationIds;
    }

    public Set<RelationResult> buildExtendedResult(Set<List<String>> relations, String hillId) {
        var hill = hillFactory.getHederaAntHill(hillId);
        return relations.stream()
                .map(this::mapToRelationItems)
                .map(this::addTokenInfo)
                .map(relationItems -> buildRelationResult(hill.getSourceAccountId(), hill.getTargetAccountId(), relationItems))
                .collect(Collectors.toSet());
    }

    private List<RelationResult.RelationItem> addTokenInfo(List<RelationResult.RelationItem> relationItems) {
        var allTokenIds = relationItems.stream()
                .map(RelationResult.RelationItem::getTokenRelations)
                .flatMap(Collection::stream)
                .map(RelationResult.TokenRelationItem::getTokenId)
                .collect(Collectors.toUnmodifiableSet());
        var tokenInfos = tokenService.getTokenInfos(allTokenIds).stream().collect(Collectors.toMap(TokenDto::getToken_id, Function.identity()));

        return relationItems.stream()
                .map(relationItem -> relationItem.toBuilder()
                        .tokenRelations(addTokenInfo(relationItem.getTokenRelations(), tokenInfos))
                        .build()
                )
                .toList();
    }

    private List<RelationResult.TokenRelationItem> addTokenInfo(List<RelationResult.TokenRelationItem> tokenRelationItems, Map<String, TokenDto> tokenInfo) {
        return tokenRelationItems.stream()
                .map(tokenRelationItem -> addTokenInfo(tokenRelationItem, tokenInfo))
                .toList();
    }

    private RelationResult.TokenRelationItem addTokenInfo(RelationResult.TokenRelationItem tokenRelationItem, Map<String, TokenDto> tokenInfo) {
        var symbol = Optional.ofNullable(tokenInfo.get(tokenRelationItem.getTokenId()))
                .map(TokenDto::getSymbol).orElse(null);
        var name = Optional.ofNullable(tokenInfo.get(tokenRelationItem.getTokenId()))
                .map(TokenDto::getName).orElse(null);
        return tokenRelationItem.toBuilder()
                .symbol(symbol)
                .name(name)
                .build();
    }

    private RelationResult buildRelationResult(String sourceAccountId, String targetAccountId, List<RelationResult.RelationItem> relationItems) {
        return RelationResult.builder()
                .source(sourceAccountId)
                .target(targetAccountId)
                .relations(relationItems)
                .build();
    }

    private List<RelationResult.RelationItem> mapToRelationItems(List<String> relationIds) {
        var relationItems = new ArrayList<RelationResult.RelationItem>();
        for (int i = 0; i < relationIds.size() - 1; i++) {
            String accountId = relationIds.get(i);
            String nextAccountId = relationIds.get(i + 1);
            Account account = accountsInMemoryStorage.getAccount(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
            var tokenRelations = account.getDebitRelations().get(nextAccountId).entrySet().stream()
                    .map(e -> RelationResult.TokenRelationItem.builder()
                            .tokenId(e.getKey())
                            .transactions(e.getValue().shortView())
                            .build()).toList();
            relationItems.add(RelationResult.RelationItem.builder()
                    .id(accountId)
                    .tokenRelations(tokenRelations)
                    .build());
        }
        relationItems.add(RelationResult.RelationItem.builder()
                .id(relationIds.getLast())
                .tokenRelations(Collections.emptyList())
                .build());
        return relationItems;

    }

}
