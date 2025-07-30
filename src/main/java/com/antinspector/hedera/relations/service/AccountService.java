package com.antinspector.hedera.relations.service;

import com.antinspector.hedera.relations.ants.HederaAntHill;
import com.antinspector.hedera.relations.dto.integration.TokenDto;
import com.antinspector.hedera.relations.dto.web.*;
import com.antinspector.hedera.relations.graph.Account;
import com.antinspector.hedera.relations.graph.AccountsInMemoryStorage;
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


    public GetRelationsAsyncResponse findRelationAsync(String sourceAccountId, String targetAccountId, int antCount, int waves) {
        if (graphBuilder.isStorageUpdateInProgress()) {
            throw new IllegalStateException("Graph storage is currently being updated, please try again later.");
        }
        var hill = hillFactory.createHill(sourceAccountId, targetAccountId, antCount, waves);
        hill.runAntsAsync();
        return GetRelationsAsyncResponse.builder()
                .hillId(hill.getHillId())
                .build();
    }

    public HillJobStatusResponse getJobStatus(String jobId) {
        var hill = hillFactory.getHederaAntHill(jobId);
        if (hill.isDone()) {
            return HillJobStatusResponse.of(JobStatusEnum.COMPLETED);
        }
        return HillJobStatusResponse.of(JobStatusEnum.PENDING);
    }

    public List getJobResult(String jobId, boolean extended) {
        var hill = hillFactory.getHederaAntHill(jobId);
        List<List<String>> relationIds = hill.getRelationsSync().stream()
                .sorted(Comparator.comparingInt(List::size))
                .toList();
        return extended ? buildExtendedResult(relationIds, hill.getHillId()) : relationIds;
    }

    public void runTest(int antCount, int waves, boolean extendedResponse) {
        if (graphBuilder.isStorageUpdateInProgress()) {
            throw new IllegalStateException("Graph storage is currently being updated, please try again later.");
        }
        int steps = 0;
        int successful = 0;
        while (steps < 50) {
            steps++;
            var sourceAccountId = accountsInMemoryStorage.getRandomAccountId();
            var targetAccountId = accountsInMemoryStorage.getRandomAccountId();
            var relations = findRelations(sourceAccountId, targetAccountId, antCount, waves, extendedResponse);
            if (!relations.isEmpty()) {
                successful++;
                System.out.println("Found " + relations.size() + " paths between " + sourceAccountId + " and " + targetAccountId);
                System.out.println("Relations: " + relations);
            }
            System.out.println("Steps: " + steps + ", Successful: " + successful);
        }
    }

    public SystemInfoResponse getSystemInfo() {
        var timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return SystemInfoResponse.builder()
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

    public List findRelations(String sourceAccountId, String targetAccountId, int antCount, int waves, boolean extended) {
        if (graphBuilder.isStorageUpdateInProgress()) {
            throw new IllegalStateException("Graph storage is currently being updated, please try again later.");
        }
        HederaAntHill antHill = hillFactory.createHill(sourceAccountId, targetAccountId, antCount, waves);
        List<List<String>> relationIds = antHill.findRelationsSync().stream()
                .sorted(Comparator.comparingInt(List::size))
                .toList();
        return extended ? buildExtendedResult(relationIds, antHill.getHillId()) : relationIds;
    }

    private List<RelationResultResponse> buildExtendedResult(List<List<String>> relations, String hillId) {
        var hill = hillFactory.getHederaAntHill(hillId);
        return relations.stream()
                .map(this::mapToRelationItems)
                .map(this::addTokenInfo)
                .map(relationItems -> buildRelationResult(hill.getSourceAccountId(), hill.getTargetAccountId(), relationItems))
                .toList();
    }

    private List<RelationResultResponse.RelationItem> addTokenInfo(List<RelationResultResponse.RelationItem> relationItems) {
        var allTokenIds = relationItems.stream()
                .map(RelationResultResponse.RelationItem::getTokenRelations)
                .flatMap(Collection::stream)
                .map(RelationResultResponse.TokenRelationItem::getTokenId)
                .collect(Collectors.toUnmodifiableSet());
        var tokenInfos = tokenService.getTokenInfos(allTokenIds).stream().collect(Collectors.toMap(TokenDto::getToken_id, Function.identity()));

        return relationItems.stream()
                .map(relationItem -> relationItem.toBuilder()
                        .tokenRelations(addTokenInfo(relationItem.getTokenRelations(), tokenInfos))
                        .build()
                )
                .toList();
    }

    private List<RelationResultResponse.TokenRelationItem> addTokenInfo(List<RelationResultResponse.TokenRelationItem> tokenRelationItems, Map<String, TokenDto> tokenInfo) {
        return tokenRelationItems.stream()
                .map(tokenRelationItem -> addTokenInfo(tokenRelationItem, tokenInfo))
                .toList();
    }

    private RelationResultResponse.TokenRelationItem addTokenInfo(RelationResultResponse.TokenRelationItem tokenRelationItem, Map<String, TokenDto> tokenInfo) {
        var symbol = Optional.ofNullable(tokenInfo.get(tokenRelationItem.getTokenId()))
                .map(TokenDto::getSymbol).orElse(null);
        var name = Optional.ofNullable(tokenInfo.get(tokenRelationItem.getTokenId()))
                .map(TokenDto::getName).orElse(null);
        return tokenRelationItem.toBuilder()
                .symbol(symbol)
                .name(name)
                .build();
    }

    private RelationResultResponse buildRelationResult(String sourceAccountId, String targetAccountId, List<RelationResultResponse.RelationItem> relationItems) {
        return RelationResultResponse.builder()
                .source(sourceAccountId)
                .target(targetAccountId)
                .relations(relationItems)
                .build();
    }

    private List<RelationResultResponse.RelationItem> mapToRelationItems(List<String> relationIds) {
        var relationItems = new ArrayList<RelationResultResponse.RelationItem>();
        for (int i = 0; i < relationIds.size() - 1; i++) {
            String accountId = relationIds.get(i);
            String nextAccountId = relationIds.get(i + 1);
            Account account = accountsInMemoryStorage.getAccount(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
            var tokenRelations = account.getDebitRelations().get(nextAccountId).entrySet().stream()
                    .map(e -> RelationResultResponse.TokenRelationItem.builder()
                            .tokenId(e.getKey())
                            .transactions(e.getValue().shortView())
                            .build()).toList();
            relationItems.add(RelationResultResponse.RelationItem.builder()
                    .id(accountId)
                    .tokenRelations(tokenRelations)
                    .build());
        }
        relationItems.add(RelationResultResponse.RelationItem.builder()
                .id(relationIds.getLast())
                .tokenRelations(Collections.emptyList())
                .build());
        return relationItems;

    }

}
