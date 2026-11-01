package com.example.transactionService.service;

import com.example.transactionService.dto.ApiResponse;
import com.example.transactionService.dto.TransactionDTO;
import com.example.transactionService.dto.TransactionRequest;
import com.example.transactionService.enums.TransactionDirection;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {
    ApiResponse<TransactionDTO> deposit(TransactionRequest transactionRequest);
    ApiResponse<TransactionDTO> transfer(TransactionRequest transactionRequest);
    ApiResponse<TransactionDTO> withdraw(TransactionRequest transactionRequest);
    ApiResponse<TransactionDTO> getTransactionByReference(String reference);

    ApiResponse<List<TransactionDTO>> getAllTransactionByAccountNumber(String accountNumber);

    ApiResponse<List<TransactionDTO>> getAllTransactionHistory(String accountNumber, LocalDateTime start,LocalDateTime end);

    ApiResponse<List<TransactionDTO>> getMyTransactionHistoryByDirection(String accountNumber, TransactionDirection transactionDirection);








}
