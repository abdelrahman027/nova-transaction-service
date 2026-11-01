package com.example.transactionService.controller;


import com.example.transactionService.dto.ApiResponse;
import com.example.transactionService.dto.TransactionRequest;
import com.example.transactionService.enums.TransactionDirection;
import com.example.transactionService.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;


    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<?>> transfer(@Valid @RequestBody TransactionRequest request){
        return ResponseEntity.ok(transactionService.transfer(request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<?>> withdraw(@Valid @RequestBody TransactionRequest request){
        return ResponseEntity.ok(transactionService.withdraw(request));
    }


    @GetMapping("/history")
    public ResponseEntity<ApiResponse<?>> getHistory(
            @RequestParam String accountNumber,
            @RequestParam(required = false)  @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false)  @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate end

            ){

        LocalDateTime startDate = (start != null)?start.atStartOfDay() : LocalDateTime.of(LocalDateTime.now().getYear(), 1,1,0,0);
        LocalDateTime endDate = (end != null)?end.atTime(LocalTime.MAX): LocalDateTime.now();

        return ResponseEntity.ok(transactionService.getAllTransactionHistory(accountNumber,startDate,endDate));
    }

    @GetMapping("/history/direction")
    public ResponseEntity<ApiResponse<?>> getHistoryByDirection(@RequestParam String accountNumber, @RequestParam TransactionDirection transactionDirection){
        return ResponseEntity.ok(transactionService.getMyTransactionHistoryByDirection(accountNumber,transactionDirection));
    }

    @GetMapping("/history/{reference}")
    public ResponseEntity<ApiResponse<?>> getTransactionByReference(@PathVariable String reference){
        return ResponseEntity.ok(transactionService.getTransactionByReference(reference));
    }


}
