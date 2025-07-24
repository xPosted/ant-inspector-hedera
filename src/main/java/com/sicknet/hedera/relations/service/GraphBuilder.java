package com.sicknet.hedera.relations.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sicknet.hedera.relations.dto.MetaInfoDto;
import com.sicknet.hedera.relations.dto.TransactionDto;
import com.sicknet.hedera.relations.graph.AccountsInMemoryStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class GraphBuilder {

    private final ReentrantLock storageUpdateLock = new ReentrantLock();

    @Autowired
    private AccountsInMemoryStorage accountsMetaInfo;

    private final ObjectMapper objectMapper;

    public GraphBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public boolean isStorageUpdateInProgress() {
        return storageUpdateLock.isLocked();
    }

    public void readNewTransactionsIfExist(String homeDir, String metaFileName) throws IOException {
        if (storageUpdateLock.isLocked()) {
            log.warn("GraphBuilder is already updating storage, skipping new transactions read.");
            return;
        }
        try  {
            storageUpdateLock.lock();
            var metaFilePath = String.format("%s/%s", homeDir, metaFileName);
            getNewTransactionsParts(metaFilePath).stream()
                    .filter(MetaInfoDto.TransactionsPart::isCompleted)
                    .forEach(transactionsPart -> readTransactionsPart(transactionsPart, homeDir));
            accountsMetaInfo.calculateTokenSummaries();
        } finally {
            storageUpdateLock.unlock();
        }
    }

    private void readSubPart(MetaInfoDto.TransactionsSubPart subPart, String baseDir, String homeDir) {
        String pathToFile = String.format("%s/%s/%s", homeDir, baseDir, subPart.getFileName());
        var transactions = readTransactionsFromFile(pathToFile);
        addTransactions(transactions, subPart.getToTs());
    }

    private void readTransactionsPart(MetaInfoDto.TransactionsPart transactionsPart, String homeDir) {
        getNewTransactionSubParts(transactionsPart).parallelStream().forEach(subPart -> readSubPart(subPart, transactionsPart.getBasePath(), homeDir));
    }


    private void addTransactions(List<TransactionDto> transactions, Long latestTs) {
        transactions.forEach(accountsMetaInfo::addTransaction);
        accountsMetaInfo.updateToTs(latestTs);
    }

    private List<MetaInfoDto.TransactionsPart> getNewTransactionsParts(String homeDir) throws IOException {
        var storageMetaInfoDto = readMetaInfoDtoFromFile(homeDir);
        return storageMetaInfoDto.getTransactions().stream()
                .filter(tp -> tp.getToTs() > accountsMetaInfo.getToTs())
                .toList();
    }

    private List<MetaInfoDto.TransactionsSubPart> getNewTransactionSubParts(MetaInfoDto.TransactionsPart transactionsPart) {
        return transactionsPart.getSubParts().stream()
                .filter(sp -> sp.getToTs() > accountsMetaInfo.getToTs())
                .toList();
    }

    private MetaInfoDto readMetaInfoDtoFromFile(String pathToFile) throws IOException {
        return objectMapper.readValue(
                new File(pathToFile), new TypeReference<MetaInfoDto>() {
                }
        );
    }

    private List<TransactionDto> readTransactionsFromFile(String pathToFile) {
        try {
            return objectMapper.readValue(
                    new File(pathToFile), new TypeReference<List<TransactionDto>>() {
                    }
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to read transactions from file: " + pathToFile, e);
        }
    }

}
