package com.antinspector.hedera.relations.dto.web;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HillJobStatus {
    JobStatusEnum status;

    public static HillJobStatus of(JobStatusEnum status) {
        return HillJobStatus.builder().status(status).build();
    }

}
