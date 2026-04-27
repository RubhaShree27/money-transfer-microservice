package org.moneytransfer.moneytransfermicroservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private String transactionId;
    private String status;
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private String message;
}