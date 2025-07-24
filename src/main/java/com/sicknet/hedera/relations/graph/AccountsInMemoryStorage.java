package com.sicknet.hedera.relations.graph;

import com.sicknet.hedera.relations.dto.TransactionDto;
import com.sicknet.hedera.relations.dto.TransferInfo;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
@AllArgsConstructor
public class AccountsInMemoryStorage {

    public static final String NATIVE_TOKEN_ID = "0.0.0"; // Represents the native HBAR token ID

    AtomicLong fromTs;
    AtomicLong toTs;
    List<String> systemAccounts;
    @Getter
    Map<String, Account> accountEntityMap;
    @Getter
    List<String> accountIds;
    AtomicLong transactionCounter = new AtomicLong(0);

    public AccountsInMemoryStorage(long fromTs, long toTs, List<String> systemAccounts) {
        this.fromTs = new AtomicLong(fromTs);
        this.toTs = new AtomicLong(toTs);
        this.systemAccounts = systemAccounts != null ? systemAccounts : Collections.emptyList();
        this.accountEntityMap = new ConcurrentHashMap<>(8000000);
        this.accountIds = new ArrayList<>();

    }

    public void addTransaction(TransactionDto transaction) {
        var nativeTransfer = map(transaction.getTransactionId(), transaction.getTransfers());
        var tokenTransfer = map(transaction.getTransactionId(), transaction.getTokenTransfers());
        nativeTransfer.ifPresent(this::processNewTransfer);
        tokenTransfer.ifPresent(this::processNewTransfer);
    }

    public void calculateTokenSummaries() {
        accountEntityMap.values().forEach(Account::runPostUpdateCalculations);
        accountIds = new ArrayList<>(accountEntityMap.keySet());
    }

    public Optional<Account> getAccount(String accountId) {
        return Optional.ofNullable(accountEntityMap.get(accountId));
    }

    public long getFromTs() {
        return fromTs.get();
    }

    public long getToTs() {
        return toTs.get();
    }

    public void updateToTs(long toTs) {
        if (this.toTs.get() < toTs) {
            this.toTs.getAndSet(toTs);
        }
    }

    public String getRandomAccountId() {
        return accountIds.get(new Random().nextInt(accountIds.size()));
    }

    public long getTransactionCount() {
        return transactionCounter.get();
    }

    private void processNewTransfer(TransferInfo transferInfo) {
        var senderAccId = transferInfo.getSender();
        var receiverAccId = transferInfo.getReceiver();
        if (!validateTransfer(transferInfo)) {
            return;
        }
        transactionCounter.incrementAndGet();
        accountEntityMap.compute(senderAccId,
                (id, acc) -> {
                    var account = Optional.ofNullable(acc).orElseGet(() -> Account.buildNew(senderAccId));
                    return account.addTransfer(transferInfo);
                });
        accountEntityMap.compute(receiverAccId,
                (id, acc) -> {
                    var account = Optional.ofNullable(acc).orElseGet(() -> Account.buildNew(receiverAccId));
                    return account.addTransfer(transferInfo);
                });
    }

    private boolean validateTransfer(TransferInfo transfer) {
        var senderAccId = transfer.getSender();
        var receiverAccId = transfer.getReceiver();
        var tokenId = transfer.getTokenId();
        if (senderAccId == null || receiverAccId == null || tokenId == null ) {
            log.warn("Invalid transfer info: sender={}, receiver={}, tokenId={}", senderAccId, receiverAccId, tokenId);
            return false;
        }
        // Skip transfers involving system accounts
        return !systemAccounts.contains(senderAccId) && !systemAccounts.contains(receiverAccId);
    }

    private Optional<TransferInfo> map(String transactionId, List<TransactionDto.TransferDto> transfers) {
        var senderTransfer = transfers.stream().min(Comparator.comparingLong(TransactionDto.TransferDto::getAmount));
        var receiverTransfer = transfers.stream().max(Comparator.comparingLong(TransactionDto.TransferDto::getAmount));
        if (senderTransfer.isEmpty()) {
            return Optional.empty(); // No valid transfers to process
        }
        var senderAccId = senderTransfer.get().getAccount();
        var receiverAccId = receiverTransfer.get().getAccount();
        var amount = receiverTransfer.get().getAmount();
        var tokenId = senderTransfer.map(TransactionDto.TransferDto::getTokenId).orElse(NATIVE_TOKEN_ID);
        var transferSummary = TransactionsSummary.of(transactionId, amount);
        return Optional.of(TransferInfo.builder()
                .sender(senderAccId)
                .receiver(receiverAccId)
                .summary(transferSummary)
                .tokenId(tokenId)
                .build());
    }

}
