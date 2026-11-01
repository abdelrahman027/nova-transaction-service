package com.example.transactionService.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    private String fromAccountNumber;

    @NotBlank(message = "destination account number is required!")
    private String toAccountNumber;

    @NotNull(message = "Amount is required!")
    @DecimalMin(value = "10",message = "only greater than 10 values allowed!")
    private BigDecimal amount;

    private String description;


}
