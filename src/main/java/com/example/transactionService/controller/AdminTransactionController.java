package com.example.transactionService.controller;


import com.example.transactionService.dto.ApiResponse;
import com.example.transactionService.dto.TransactionRequest;
import com.example.transactionService.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminTransactionController {

    private final TransactionService transactionService;


    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<?>> deposit(@Valid @RequestBody TransactionRequest request){
        return ResponseEntity.ok(transactionService.deposit(request));
    }
    @GetMapping("/history/{accountNumber}")
    public ResponseEntity<ApiResponse<?>> getTransactionByReference(@PathVariable String accountNumber){
        return ResponseEntity.ok(transactionService.getAllTransactionByAccountNumber(accountNumber));
    }
}
