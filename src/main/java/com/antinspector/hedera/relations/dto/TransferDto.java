package com.antinspector.hedera.relations.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TransferDto {
    @JsonProperty("token_id")
    private String tokenId;
    private String account;
    private long amount;
    @JsonProperty("is_approval")
    private boolean isApproval;

}
