package com.antinspector.hedera.relations.dto.web;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SystemInfoResponse {

    @Builder.Default
    public final String VERSION = "0.1.0";
    @Builder.Default
    public final String NAME = "Ant Inspector for Hedera account network";
    @Builder.Default
    public final String DESCRIPTION = "This is tool for finding relations inside Hedera accounts network. " +
            "You are welcome to set 'source' and 'target' Hedera accounts, that are typically looks like [0.0.123], " +
            " and this tool will try to find out 'many flow' between this two accounts. This tool is based on well known " +
            "'Ant algorithm' for finding relations on graph.";
    @Builder.Default
    public final String GITHUB_URL = "https://github.com/xPosted/ant-inspector-hedera";
    @Builder.Default
    public final String contactEmail = "aleksandrzhupanov@gmail.com";
    @Builder.Default
    public final String contactName = "Aleksandr Zhupanov";
    @Builder.Default
    public final String contactPhone = "+380995327657";
    public String timeFrom;
    public String timeTo;
    public Integer accountsProcessed;
    public Long transactionsProcessed;
    public boolean storageAvailable;

}
