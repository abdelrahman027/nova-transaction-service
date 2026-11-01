package com.example.transactionService.feign;


import com.example.transactionService.dto.AccountDTO;
import com.example.transactionService.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-account-service")
public interface AccountFeignClient {

    @GetMapping("/api/accounts/{accountNumber}")
    ApiResponse<AccountDTO> getAccountByNumber(@PathVariable String accountNumber);
}
