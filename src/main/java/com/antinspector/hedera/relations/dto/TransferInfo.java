package com.antinspector.hedera.relations.dto;

import com.antinspector.hedera.relations.graph.TransactionsSummary;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TransferInfo {

    String sender;
    String receiver;
    String tokenId;
    TransactionsSummary summary;

}
