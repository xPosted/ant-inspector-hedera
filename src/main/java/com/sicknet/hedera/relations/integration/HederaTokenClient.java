package com.sicknet.hedera.relations.integration;

import com.sicknet.hedera.relations.dto.integration.TokenResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@FeignClient(name = "hederaTokenClient", url = "https://mainnet-public.mirrornode.hedera.com/api/v1")
public interface HederaTokenClient {

    @GetMapping("/tokens")
    TokenResponseDto getTokenById(@RequestParam("token.id") Set<String> tokenId);
}
