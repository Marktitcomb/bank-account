package org.pretend.bank.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pretend.bank.model.Transaction;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

class BankAccountServiceImplTest {

    private BankAccountServiceImpl bankAccountServiceimpl;

    @BeforeEach
    void setUp() {
        bankAccountServiceimpl = new BankAccountServiceImpl();
    }

    @Test
    public void retrieveBalanceAfterInitializing() {
       assertEquals(0.0, bankAccountServiceimpl.retrieveBalance());
    }

    @Test
    public void processTransactionDebitsAndCredits() {
        bankAccountServiceimpl.processTransaction(Transaction.debit(500));
        bankAccountServiceimpl.processTransaction(Transaction.credit(200));
        bankAccountServiceimpl.processTransaction(Transaction.credit(200));
        assertEquals(-100, bankAccountServiceimpl.retrieveBalance());
    }

    @Test
    public void processTransactionHandlesConncurrentRequests() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(100);

        for(int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {

                try {
                    if(index % 2 == 0) {
                        bankAccountServiceimpl.processTransaction(Transaction.credit(2));
                    } else {
                        bankAccountServiceimpl.processTransaction(Transaction.debit(2));
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();
        assertEquals(0.0, bankAccountServiceimpl.retrieveBalance());
    }
}