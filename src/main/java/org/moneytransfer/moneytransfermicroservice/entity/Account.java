package org.moneytransfer.moneytransfermicroservice.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private String accountId;
    private String accountHolder;
    private BigDecimal balance;

}