package com.example.transactionService.repository;

import com.example.transactionService.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {


    Optional<Transaction> findByReference(String reference);

    @Query("select t from Transaction t where t.fromAccountNumber = :accountNumber or t.toAccountNumber = :accountNumber order by t.createdAt desc")
    List<Transaction> findAllByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query("select t from Transaction t where (t.toAccountNumber = :accountNumber or t.fromAccountNumber = :accountNumber) and t.createdAt between :start and :end order by t.createdAt desc ")
    List<Transaction> findAllByAccountNumberAndDateRange(
            @Param("accountNumber") String accountNumber,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<Transaction> findByFromAccountNumber(String fromAccountNumber);

    List<Transaction> findByToAccountNumber(String fromAccountNumber);


}
