package com.antinspector.hedera.relations.web;

import com.antinspector.hedera.relations.dto.web.GetRelationsAsyncResponse;
import com.antinspector.hedera.relations.dto.web.HillJobStatusResponse;
import com.antinspector.hedera.relations.dto.web.SystemInfoResponse;
import com.antinspector.hedera.relations.graph.Account;
import com.antinspector.hedera.relations.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController()
@RequestMapping("/hedera")
public class AdminController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/account")
    public Account getAccount(@RequestParam("accountId") String accountId) {
        return accountService.getAccountById(accountId);
    }

    @PostMapping("/sync/ant/run")
    public Set getRelations(@RequestParam("sourceAccountId") String sourceAccountId,
                                          @RequestParam("targetAccountId") String targetAccountId,
                                          @RequestParam(value = "antCount", defaultValue = "500") int antCount,
                                          @RequestParam(value = "waves", defaultValue = "4") int waves,
                                          @RequestParam(value = "extended", defaultValue = "false") boolean extended) {
        return accountService.findRelations(sourceAccountId, targetAccountId, antCount, waves, extended);
    }

    @PostMapping("/async/ant/run")
    public GetRelationsAsyncResponse getRelationsAsync(@RequestParam("sourceAccountId") String sourceAccountId,
                                                       @RequestParam("targetAccountId") String targetAccountId,
                                                       @RequestParam(value = "antCount", defaultValue = "500") int antCount,
                                                       @RequestParam(value = "waves", defaultValue = "4") int waves) {
        return accountService.findRelationAsync(sourceAccountId, targetAccountId, antCount, waves);
    }

    @PostMapping("/async/ant/status")
    public HillJobStatusResponse getHillJobStatus(@RequestParam("hillId") String hillId) {
        return accountService.getJobStatus(hillId);
    }

    @GetMapping("/async/ant/result")
    public Set getJobResult(@RequestParam("hillId") String hillId,
                                          @RequestParam(value = "extended", defaultValue = "false") boolean extended) {
       return accountService.getJobResult(hillId, extended);
    }

    @GetMapping("/about")
    public SystemInfoResponse getSystemInfo() {
        return accountService.getSystemInfo();
    }

    @PostMapping("/application/test")
    public void testApplication(@RequestParam(value = "antCount", defaultValue = "700") int antCount,
                                @RequestParam(value = "waves", defaultValue = "1") int waves,
                                @RequestParam(value = "extended", defaultValue = "false") boolean extendedResponse) {
        accountService.runTest(antCount, waves, extendedResponse);
    }
}
