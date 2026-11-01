package com.example.transactionService.dto;

import com.example.transactionService.enums.*;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {
    private Long id;
    private String reference;
    private String fromAccountNumber;
    private String fromBankCode;
    private String toAccountNumber;
    private String toBankCode;
    private BigDecimal amount;
    private String description;
    private Currency currency;
    private TransactionType type;
    private TransactionStatus status;
    private TransactionDirection direction;
    private Channel channel;
    private  LocalDateTime createdAt;

}
