package com.sicknet.hedera.relations.web;

import com.sicknet.hedera.relations.dto.web.HillJobStatus;
import com.sicknet.hedera.relations.dto.web.SystemInfo;
import com.sicknet.hedera.relations.graph.Account;
import com.sicknet.hedera.relations.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController("/hedera")
public class AdminController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/account")
    public Account getAccount(@RequestParam("accountId") String accountId) {
        return accountService.getAccountById(accountId);
    }

    @PostMapping("/relation")
    public Set getRelations(@RequestParam("sourceAccountId") String sourceAccountId,
                                          @RequestParam("targetAccountId") String targetAccountId,
                                          @RequestParam(value = "antCount", defaultValue = "500") int antCount,
                                          @RequestParam(value = "waves", defaultValue = "4") int waves,
                                          @RequestParam(value = "extended", defaultValue = "false") boolean extended) {
        return accountService.findRelations(sourceAccountId, targetAccountId, antCount, waves, extended);
    }

    @PostMapping("/relation/async")
    public String getRelationsAsync(@RequestParam("sourceAccountId") String sourceAccountId,
                                    @RequestParam("targetAccountId") String targetAccountId,
                                    @RequestParam(value = "antCount", defaultValue = "500") int antCount,
                                    @RequestParam(value = "waves", defaultValue = "4") int waves) {
        return accountService.findRelationAsync(sourceAccountId, targetAccountId, antCount, waves);
    }

    @PostMapping("/relation/async/status")
    public HillJobStatus getHillJobStatus(@RequestParam("hillId") String hillId) {
        return accountService.getJobStatus(hillId);
    }

    @GetMapping("/relation/async/result")
    public Set getJobResult(@RequestParam("hillId") String hillId,
                                          @RequestParam(value = "extended", defaultValue = "false") boolean extended) {
       return accountService.getJobResult(hillId, extended);
    }

    @GetMapping("/system/info")
    public SystemInfo getSystemInfo() {
        return accountService.getSystemInfo();
    }

    @PostMapping("/application/test")
    public void testApplication(@RequestParam(value = "extended", defaultValue = "false") boolean extendedResponse) {
        accountService.runTest(extendedResponse);
    }
}
