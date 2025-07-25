package com.antinspector.hedera.relations.dto.web;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HillJobStatusResponse {
    JobStatusEnum status;

    public static HillJobStatusResponse of(JobStatusEnum status) {
        return HillJobStatusResponse.builder().status(status).build();
    }

}
