package com.example.transactionService.service.impl;


import com.example.transactionService.dto.AccountDTO;
import com.example.transactionService.dto.ApiResponse;
import com.example.transactionService.dto.TransactionDTO;
import com.example.transactionService.dto.TransactionRequest;
import com.example.transactionService.entity.Transaction;
import com.example.transactionService.enums.*;
import com.example.transactionService.exceptions.BadRequestException;
import com.example.transactionService.exceptions.NotFoundException;
import com.example.transactionService.feign.AccountFeignClient;
import com.example.transactionService.kafka.dto.BalanceUpdateEvent;
import com.example.transactionService.kafka.service.TransactionEventPublisher;
import com.example.transactionService.repository.TransactionRepository;
import com.example.transactionService.service.TransactionService;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountFeignClient accountFeignClient;
    private final ModelMapper modelMapper;

    private final TransactionEventPublisher transactionEventPublisher;
    private final RequestInterceptor requestInterceptor;


    @Override
    public ApiResponse<TransactionDTO> deposit(TransactionRequest transactionRequest) {
        AccountDTO account =fetchAndValidateAccount(transactionRequest.getToAccountNumber());
        Transaction deposit = Transaction.builder()
                .reference("DEP"+ UUID.randomUUID().toString().substring(0,8))
                .fromBankCode("NOVA")
                .currency(Currency.USD)
                .toAccountNumber(transactionRequest.getToAccountNumber())
                .fromAccountNumber(transactionRequest.getFromAccountNumber())
                .amount(transactionRequest.getAmount())
                .direction(TransactionDirection.CREDIT)
                .channel(Channel.API)
                .description(transactionRequest.getDescription())
                .type(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .build();
        Transaction savedTransaction = transactionRepository.save(deposit);


        BalanceUpdateEvent balanceUpdateEvent =BalanceUpdateEvent.builder()
                .accountNumber(transactionRequest.getToAccountNumber())
                .amount(transactionRequest.getAmount())
                .currency(Currency.USD)
                .email(account.getOwnerEmail())
                .transactionDirection(TransactionDirection.CREDIT)
                .transactionStatus(TransactionStatus.SUCCESS)
                .reference(savedTransaction.getReference())
                .currentBalance(account.getBalance())
                .build();
        transactionEventPublisher.sendBalanceUpdate(balanceUpdateEvent);

        return new ApiResponse<>(HttpStatus.CREATED.value(), "deposit success",modelMapper.map(savedTransaction,TransactionDTO.class));
    }

    @Override
    public ApiResponse<TransactionDTO> transfer(TransactionRequest transactionRequest) {
        if (transactionRequest.getFromAccountNumber() == null || transactionRequest.getFromAccountNumber().isEmpty())
            throw new BadRequestException("from account is needed");
        AccountDTO sourceAccount = fetchAndValidateAccount(transactionRequest.getFromAccountNumber());
        String loggedInEmailString = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            loggedInEmailString = authentication.getName();
        }
        log.info("Auth email is {}",loggedInEmailString);
        log.info("Account email is {}",sourceAccount.getOwnerEmail());
        if (!sourceAccount.getOwnerEmail().equals(loggedInEmailString)) {
            throw new BadRequestException("access denied !");
        }

        if (sourceAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("transaction failed , account inactive !");
        }

        if (sourceAccount.getBalance().compareTo(transactionRequest.getAmount())  < 0) {
            throw new BadRequestException("insufficient balance");
        }

        if (transactionRequest.getFromAccountNumber().equals(transactionRequest.getToAccountNumber())) {
            throw new BadRequestException("you can't transfer to the same account");
        }

        AccountDTO destinationAccount = fetchAndValidateAccount(transactionRequest.getToAccountNumber());
        Transaction transferTnx = Transaction.builder()
                .reference("TRF"+ UUID.randomUUID().toString().substring(0,8))
                .fromBankCode("NOVA")
                .currency(Currency.USD)
                .toAccountNumber(transactionRequest.getToAccountNumber())
                .fromAccountNumber(transactionRequest.getFromAccountNumber())
                .amount(transactionRequest.getAmount())
                .direction(TransactionDirection.CREDIT)
                .channel(Channel.API)
                .description(transactionRequest.getDescription())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .build();
        Transaction savedTransaction = transactionRepository.save(transferTnx);

        //THE SENDEr
        transactionEventPublisher.sendBalanceUpdate(BalanceUpdateEvent.builder()
                        .accountNumber(transactionRequest.getFromAccountNumber())
                        .amount(transactionRequest.getAmount())
                        .currency(Currency.USD)
                        .transactionDirection(TransactionDirection.DEBIT)
                        .transactionStatus(TransactionStatus.SUCCESS)
                        .reference(savedTransaction.getReference())
                        .currentBalance(sourceAccount.getBalance().subtract(transactionRequest.getAmount()))
                        .email(sourceAccount.getOwnerEmail())
                .build());

        //THE RECEIVER
        transactionEventPublisher.sendBalanceUpdate(BalanceUpdateEvent.builder()
                .accountNumber(transactionRequest.getToAccountNumber())
                .amount(transactionRequest.getAmount())
                .currency(Currency.USD)
                .transactionDirection(TransactionDirection.CREDIT)
                .transactionStatus(TransactionStatus.SUCCESS)
                .reference(savedTransaction.getReference())
                .currentBalance(destinationAccount.getBalance().add(transactionRequest.getAmount()))
                .email(destinationAccount.getOwnerEmail())

                .build());

        return new ApiResponse<>(HttpStatus.CREATED.value(),"Transfer Successful",modelMapper.map(savedTransaction,TransactionDTO.class));
    }

    @Override
    public ApiResponse<TransactionDTO> withdraw(TransactionRequest transactionRequest) {
        AccountDTO account = fetchAndValidateAccount(transactionRequest.getFromAccountNumber());
        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BadRequestException("transaction failed , account inactive !");
        }

        if (account.getBalance().compareTo(transactionRequest.getAmount())  < 0) {
            throw new BadRequestException("insufficient balance");
        }
        Transaction withdrawalTxn = Transaction.builder()
                .reference("WID"+ UUID.randomUUID().toString().substring(0,8))
                .fromBankCode("NOVA")
                .toBankCode("VAULT")
                .currency(Currency.USD)
                .toAccountNumber("VAULT")
                .fromAccountNumber(transactionRequest.getFromAccountNumber())
                .amount(transactionRequest.getAmount())
                .direction(TransactionDirection.CREDIT)
                .channel(Channel.API)
                .description(transactionRequest.getDescription())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .build();
        Transaction savedTransaction = transactionRepository.save(withdrawalTxn);
        transactionEventPublisher.sendBalanceUpdate(BalanceUpdateEvent.builder()
                .accountNumber(transactionRequest.getFromAccountNumber())
                .amount(transactionRequest.getAmount())
                .currency(Currency.USD)
                .transactionDirection(TransactionDirection.CREDIT)
                .transactionStatus(TransactionStatus.SUCCESS)
                .reference(savedTransaction.getReference())
                .currentBalance(account.getBalance().subtract(transactionRequest.getAmount()))
                .email(account.getOwnerEmail())

                .build());
        return new ApiResponse<>(HttpStatus.CREATED.value(),"Withdrawl Successful",modelMapper.map(savedTransaction,TransactionDTO.class));

    }

    @Override
    public ApiResponse<TransactionDTO> getTransactionByReference(String reference) {
        Transaction transaction = transactionRepository.findByReference(reference)
                .orElseThrow(()->new NotFoundException("transaction not found !"));
        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "transaction retrieved successfully",
                modelMapper.map(transaction,TransactionDTO.class));
    }

    @Override
    public ApiResponse<List<TransactionDTO>> getAllTransactionByAccountNumber(String accountNumber) {
        List<Transaction> transactionList = transactionRepository.findAllByAccountNumber(accountNumber);

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "transaction retrieved successfully",transactionList.stream()
                        .map( (tnx)->modelMapper.map(tnx,TransactionDTO.class)).toList()
                );
    }

    @Override
    public ApiResponse<List<TransactionDTO>> getAllTransactionHistory(String accountNumber, LocalDateTime start, LocalDateTime end) {
        List<Transaction> transactionList = transactionRepository.findAllByAccountNumberAndDateRange(accountNumber,start,end);

        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "transaction retrieved successfully",transactionList.stream()
                .map( (tnx)->modelMapper.map(tnx,TransactionDTO.class)).toList()
        );
    }

    @Override
    public ApiResponse<List<TransactionDTO>> getMyTransactionHistoryByDirection(String accountNumber,TransactionDirection transactionDirection) {
        List<Transaction> transactionList = transactionDirection.equals(TransactionDirection.DEBIT)
                ? transactionRepository.findByFromAccountNumber(accountNumber)
                : transactionRepository.findByToAccountNumber(accountNumber);
        return new ApiResponse<>(
                HttpStatus.OK.value(),
                "transaction retrieved successfully",transactionList.stream()
                .map( (tnx)->modelMapper.map(tnx,TransactionDTO.class)).toList()
        );
    }

    private AccountDTO fetchAndValidateAccount(String accountNumber) {
        ApiResponse<AccountDTO> response = accountFeignClient.getAccountByNumber(accountNumber);
        if (response == null || response.data() == null) {
            throw new NotFoundException("account not found");
        }

        if(response.data().getAccountStatus().equals(AccountStatus.CLOSED))
        {
            throw  new BadRequestException("transaction denied. account is closed");
        }
        return response.data();
    }
}
