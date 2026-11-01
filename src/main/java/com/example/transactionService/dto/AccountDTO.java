package com.example.transactionService.dto;


import com.example.transactionService.enums.AccountStatus;
import com.example.transactionService.enums.AccountType;
import com.example.transactionService.enums.Currency;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDTO {

    private long id;


    private String accountNumber;

    private BigDecimal balance;

    private Currency currency;

    private AccountType accountType;


    private AccountStatus accountStatus;


//    private UserDTO user;
    private String ownerEmail;
    private  LocalDateTime createdAt;
}
