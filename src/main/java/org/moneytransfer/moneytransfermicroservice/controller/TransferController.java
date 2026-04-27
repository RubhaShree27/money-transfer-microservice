package org.moneytransfer.moneytransfermicroservice.controller;

import org.moneytransfer.moneytransfermicroservice.dto.TransferRequest;
import org.moneytransfer.moneytransfermicroservice.dto.TransferResponse;
import org.moneytransfer.moneytransfermicroservice.entity.Account;
import org.moneytransfer.moneytransfermicroservice.exception.AccountNotFoundException;
import org.moneytransfer.moneytransfermicroservice.service.TransferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for money transfer API endpoints.
 */
@RestController
@RequestMapping("/api/accounts")
public class TransferController {
    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    /**
     * GET /api/accounts - Retrieve all accounts
     */
    @GetMapping
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(transferService.getAllAccounts());
    }

    /**
     * GET /api/accounts/{accountId} - Retrieve a specific account
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountId) {
        try {
            Account account = transferService.getAccount(accountId);
            return ResponseEntity.ok(account);
        } catch (AccountNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/accounts - Create a new account
     */
    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Account account) {
        try {
            Account createdAccount = transferService.createAccount(
                    account.getAccountId(),
                    account.getAccountHolder(),
                    account.getBalance()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * POST /api/accounts/transfer - Transfer money between accounts
     */
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transferMoney(@RequestBody TransferRequest transferRequest) {
        try {
            TransferResponse response = transferService.transferMoney(transferRequest);
            if ("SUCCESS".equals(response.getStatus())) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IllegalArgumentException e) {
            TransferResponse errorResponse = new TransferResponse();
            errorResponse.setStatus("FAILED");
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
