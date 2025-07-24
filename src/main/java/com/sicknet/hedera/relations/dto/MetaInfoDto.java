package com.sicknet.hedera.relations.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetaInfoDto {

    List<TransactionsPart> transactions;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TransactionsPart {
        Long fromTs;
        Long toTs;
        String basePath;
        List<TransactionsSubPart> subParts;

        @JsonIgnore
        public boolean isCompleted() {
            return Optional.ofNullable(toTs)
                    .filter(ts -> ts>= getDayEndTs())
                    .isPresent();
        }

        private long getDayEndTs() {
            return Instant.ofEpochSecond(fromTs)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .atTime(23, 59, 59)
                    .atZone(ZoneId.systemDefault())
                    .toEpochSecond();
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TransactionsSubPart {
        long fromTs;
        long toTs;
        String fileName;
    }

}
