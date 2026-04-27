package org.moneytransfer.moneytransfermicroservice.service;


import org.moneytransfer.moneytransfermicroservice.dto.TransferRequest;
import org.moneytransfer.moneytransfermicroservice.dto.TransferResponse;
import org.moneytransfer.moneytransfermicroservice.entity.Account;
import org.moneytransfer.moneytransfermicroservice.exception.AccountNotFoundException;
import org.moneytransfer.moneytransfermicroservice.repo.AccountRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


@Service
public class TransferService {
    private final AccountRepository accountRepository;
    private final AtomicLong transactionIdCounter = new AtomicLong(1000);

    public TransferService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }


    public TransferResponse transferMoney(TransferRequest request) {
        // Validate request
        validateTransferRequest(request);

        String transactionId = generateTransactionId();

        try {
            // Get both accounts - sorted by accountId to prevent deadlock
            String[] accountIds = {request.getFromAccountId(), request.getToAccountId()};
            Arrays.sort(accountIds);

            Account fromAccount = accountRepository.getAccount(request.getFromAccountId());
            Account toAccount = accountRepository.getAccount(request.getToAccountId());

            // Synchronize on accounts to prevent race conditions
            // Lock in sorted order to prevent deadlock
            synchronized (accountIds[0].intern()) {
                synchronized (accountIds[1].intern()) {
                    // Re-fetch accounts within synchronized block
                    fromAccount = accountRepository.getAccount(request.getFromAccountId());
                    toAccount = accountRepository.getAccount(request.getToAccountId());

                    // Check sufficient balance
                    if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
                        return new TransferResponse(
                                transactionId,
                                "FAILED",
                                request.getFromAccountId(),
                                request.getToAccountId(),
                                request.getAmount(),
                                "Insufficient funds in account " + request.getFromAccountId()
                        );
                    }

                    // Perform the transfer
                    fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
                    toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

                    // Save updated accounts
                    accountRepository.saveAccount(fromAccount);
                    accountRepository.saveAccount(toAccount);
                }
            }

            return new TransferResponse(
                    transactionId,
                    "SUCCESS",
                    request.getFromAccountId(),
                    request.getToAccountId(),
                    request.getAmount(),
                    "Transfer completed successfully"
            );

        } catch (AccountNotFoundException e) {
            return new TransferResponse(
                    transactionId,
                    "FAILED",
                    request.getFromAccountId(),
                    request.getToAccountId(),
                    request.getAmount(),
                    e.getMessage()
            );
        }
    }

    /**
     * Get account details.
     */
    public Account getAccount(String accountId) {
        return accountRepository.getAccount(accountId);
    }

    /**
     * Get all accounts.
     */
    public List<Account> getAllAccounts() {
        return accountRepository.getAllAccounts();
    }

    /**
     * Create a new account.
     */
    public Account createAccount(String accountId, String accountHolder, BigDecimal initialBalance) {
        return accountRepository.createAccount(accountId, accountHolder, initialBalance);
    }

    /**
     * Validate transfer request.
     */
    private void validateTransferRequest(TransferRequest request) {
        if (request.getFromAccountId() == null || request.getFromAccountId().trim().isEmpty()) {
            throw new IllegalArgumentException("From account ID cannot be null or empty");
        }
        if (request.getToAccountId() == null || request.getToAccountId().trim().isEmpty()) {
            throw new IllegalArgumentException("To account ID cannot be null or empty");
        }
        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    /**
     * Generate a unique transaction ID.
     */
    private String generateTransactionId() {
        return "TXN" + transactionIdCounter.getAndIncrement();
    }
}