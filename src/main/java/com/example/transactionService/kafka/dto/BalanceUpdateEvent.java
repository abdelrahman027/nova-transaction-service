package com.example.transactionService.kafka.dto;


import com.example.transactionService.enums.Currency;
import com.example.transactionService.enums.TransactionDirection;
import com.example.transactionService.enums.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BalanceUpdateEvent {

    public String accountNumber;
    public BigDecimal amount;
    public TransactionDirection transactionDirection;
    public TransactionStatus transactionStatus;
    public String reference;
    public Currency currency;
    public String email;
    public String firstName;
    public BigDecimal currentBalance;



}
