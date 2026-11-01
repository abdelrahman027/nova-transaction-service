package com.example.transactionService.kafka.service;


import com.example.transactionService.kafka.dto.BalanceUpdateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEventPublisher {
    private final KafkaTemplate<String,Object> kafkaTemplate;

    public void sendBalanceUpdate(BalanceUpdateEvent event) {
        kafkaTemplate.send("balance-update-events",event.getAccountNumber(),event)
                .whenComplete((stringObjectSendResult, throwable) -> {
                    if (throwable == null) {
                        log.info("send message to topic");
                        String topicResult = stringObjectSendResult.getRecordMetadata().topic();
                        Long topicOffset =  stringObjectSendResult.getRecordMetadata().offset();
                        log.info("message sent to topic {} Offset {}",topicResult,topicOffset);
                    }
                });
    }
}
