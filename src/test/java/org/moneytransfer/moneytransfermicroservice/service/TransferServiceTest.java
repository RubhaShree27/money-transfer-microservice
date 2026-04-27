package org.moneytransfer.moneytransfermicroservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.moneytransfer.moneytransfermicroservice.dto.TransferRequest;
import org.moneytransfer.moneytransfermicroservice.dto.TransferResponse;
import org.moneytransfer.moneytransfermicroservice.entity.Account;
import org.moneytransfer.moneytransfermicroservice.repo.AccountRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
public class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private TransferService transferService;

    @BeforeEach
    public void setUp() {
        transferService = new TransferService(accountRepository);
    }

    @Test
    public void testSuccessfulTransfer() {
        Account fromAccount = new Account("ACC001", "Alice", new BigDecimal("1000.00"));
        Account toAccount = new Account("ACC002", "Bob", new BigDecimal("500.00"));

        when(accountRepository.getAccount("ACC001")).thenReturn(fromAccount);
        when(accountRepository.getAccount("ACC002")).thenReturn(toAccount);

        TransferRequest request = new TransferRequest("ACC001", "ACC002", new BigDecimal("100.00"));
        TransferResponse response = transferService.transferMoney(request);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals("ACC001", response.getFromAccountId());
        assertEquals("ACC002", response.getToAccountId());
        assertTrue(response.getMessage().contains("successfully"));

        verify(accountRepository, times(2)).saveAccount(any(Account.class));
    }

    @Test
    public void testTransferInsufficientFunds() {
        Account fromAccount = new Account("ACC001", "Alice", new BigDecimal("50.00"));
        Account toAccount = new Account("ACC002", "Bob", new BigDecimal("500.00"));

        when(accountRepository.getAccount("ACC001")).thenReturn(fromAccount);
        when(accountRepository.getAccount("ACC002")).thenReturn(toAccount);

        TransferRequest request = new TransferRequest("ACC001", "ACC002", new BigDecimal("100.00"));
        TransferResponse response = transferService.transferMoney(request);

        assertEquals("FAILED", response.getStatus());
        assertTrue(response.getMessage().contains("Insufficient funds"));
    }

    @Test
    public void testTransferValidation_NullFromAccount() {
        TransferRequest request = new TransferRequest(null, "ACC002", new BigDecimal("100.00"));

        assertThrows(IllegalArgumentException.class, () -> transferService.transferMoney(request));
    }

    @Test
    public void testTransferValidation_NullToAccount() {
        TransferRequest request = new TransferRequest("ACC001", null, new BigDecimal("100.00"));

        assertThrows(IllegalArgumentException.class, () -> transferService.transferMoney(request));
    }

    @Test
    public void testTransferValidation_SameAccount() {
        TransferRequest request = new TransferRequest("ACC001", "ACC001", new BigDecimal("100.00"));

        assertThrows(IllegalArgumentException.class, () -> transferService.transferMoney(request));
    }

    @Test
    public void testTransferValidation_NegativeAmount() {
        TransferRequest request = new TransferRequest("ACC001", "ACC002", new BigDecimal("-100.00"));

        assertThrows(IllegalArgumentException.class, () -> transferService.transferMoney(request));
    }

    @Test
    public void testTransferValidation_ZeroAmount() {
        TransferRequest request = new TransferRequest("ACC001", "ACC002", new BigDecimal("0.00"));

        assertThrows(IllegalArgumentException.class, () -> transferService.transferMoney(request));
    }

    @Test
    public void testGetAccount() {
        Account account = new Account("ACC001", "Alice", new BigDecimal("1000.00"));
        when(accountRepository.getAccount("ACC001")).thenReturn(account);

        Account retrieved = transferService.getAccount("ACC001");

        assertEquals("ACC001", retrieved.getAccountId());
        assertEquals("Alice", retrieved.getAccountHolder());
        verify(accountRepository, times(1)).getAccount("ACC001");
    }

    @Test
    public void testCreateAccount() {
        Account newAccount = new Account("ACC999", "TestUser", new BigDecimal("5000.00"));
        when(accountRepository.createAccount("ACC999", "TestUser", new BigDecimal("5000.00")))
                .thenReturn(newAccount);

        Account created = transferService.createAccount("ACC999", "TestUser", new BigDecimal("5000.00"));

        assertEquals("ACC999", created.getAccountId());
        verify(accountRepository, times(1)).createAccount("ACC999", "TestUser", new BigDecimal("5000.00"));
    }
}