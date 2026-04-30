package org.pretend.bank.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.pretend.bank.model.Transaction;

class TransactionProducerServiceTest {

    private BankAccountService bankAccountService;

    private TransactionProducerService transactionProducerService;

    @BeforeEach
    public void setup() {
        bankAccountService = mock(BankAccountService.class);
        transactionProducerService = new TransactionProducerService(bankAccountService, 25);
    }

    @AfterEach
    public void tearDown() {
        transactionProducerService.shutdownExecutors();
    }

    @Test
    public void produceTransactionsTest() throws InterruptedException {
        doNothing().when(bankAccountService).processTransaction(any(Transaction.class));
        transactionProducerService.produceTransactions();
        Thread.sleep(200);
        transactionProducerService.shutdownExecutors();
        verify(bankAccountService, atLeast(4)).processTransaction(any(Transaction.class));
    }

    @Test
    public void processesTransactionsWithinRange() throws InterruptedException {
        doNothing().when(bankAccountService).processTransaction(any(Transaction.class));
        transactionProducerService.produceTransactions();
        Thread.sleep(200);
        transactionProducerService.shutdownExecutors();

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(bankAccountService, atLeast(1)).processTransaction(captor.capture());

        for(Transaction transaction: captor.getAllValues()) {
            assertTrue(Math.abs(transaction.amount()) > 200.0);
            assertTrue(Math.abs(transaction.amount()) < 500000.0);
        }
    }
}