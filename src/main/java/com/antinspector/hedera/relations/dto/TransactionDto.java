package com.antinspector.hedera.relations.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TransactionDto {
    private Object batch_key;
    private Object bytes;
    @JsonProperty("charged_tx_fee")
    private long chargedTxFee;
    @JsonProperty("consensus_timestamp")
    private String consensusTimestamp;
    @JsonProperty("entity_id")
    private Object entityId;
    @JsonProperty("max_fee")
    private String maxFee;
    @JsonProperty("max_custom_fees")
    private List<Object> maxCustomFees;
    @JsonProperty("memo_base64")
    private String memoBase64;
    private String name;
    @JsonProperty("nft_transfers")
    private List<Object> nftTransfers;
    private String node;
    private int nonce;
    @JsonProperty("parent_consensus_timestamp")
    private Object parentConsensusTimestamp;
    private String result;
    private boolean scheduled;
    @JsonProperty("staking_reward_transfers")
    private List<Object> stakingRewardTransfers;
    @JsonProperty("token_transfers")
    private List<TransferDto> tokenTransfers;
    @JsonProperty("transaction_hash")
    private String transactionHash;
    @JsonProperty("transaction_id")
    private String transactionId;
    private List<TransferDto> transfers;
    @JsonProperty("valid_duration_seconds")
    private String validDurationSeconds;
    @JsonProperty("valid_start_timestamp")
    private String validStartTimestamp;

}
