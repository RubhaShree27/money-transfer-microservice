package org.moneytransfer.moneytransfermicroservice.repo;

import org.moneytransfer.moneytransfermicroservice.entity.Account;
import org.moneytransfer.moneytransfermicroservice.exception.AccountNotFoundException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository for managing accounts.
 */
@Repository
public class AccountRepository {
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    public AccountRepository() {
        // Initialize with sample accounts
        initializeSampleAccounts();
    }

    private void initializeSampleAccounts() {
        accounts.put("ACC001", new Account("ACC001", "Rubha shree", new BigDecimal("1000.00")));
        accounts.put("ACC002", new Account("ACC002", "Palanisamy", new BigDecimal("500.00")));
        accounts.put("ACC003", new Account("ACC003", "Acc holder", new BigDecimal("750.50")));
    }

    public Account getAccount(String accountId) throws AccountNotFoundException {
        Account account = accounts.get(accountId);
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + accountId);
        }
        return account;
    }

    public void saveAccount(Account account) {
        accounts.put(account.getAccountId(), account);
    }

    public List<Account> getAllAccounts() {
        return new ArrayList<>(accounts.values());
    }

    public Account createAccount(String accountId, String accountHolder, BigDecimal initialBalance) {
        if (accounts.containsKey(accountId)) {
            throw new IllegalArgumentException("Account already exists: " + accountId);
        }
        Account account = new Account(accountId, accountHolder, initialBalance);
        accounts.put(accountId, account);
        return account;
    }


    public void deleteAccount(String accountId) {
        if (!accounts.containsKey(accountId)) {
            throw new AccountNotFoundException("Account not found: " + accountId);
        }
        accounts.remove(accountId);
    }

    public boolean accountExists(String accountId) {
        return accounts.containsKey(accountId);
    }
}