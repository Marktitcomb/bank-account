package org.pretend.bank.service;

import org.pretend.bank.model.Transaction;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TransactionProducerService {

    private static final double MIN_AMOUNT = 200.0;
    private static final double MAX_AMOUNT = 500000.0;

    private static final ScheduledExecutorService creditExecutor = Executors.newSingleThreadScheduledExecutor();
    private static final ScheduledExecutorService debitExecutor = Executors.newSingleThreadScheduledExecutor();

    private final int transactionRate;
    private final BankAccountService bankAccountService;

    public TransactionProducerService(final BankAccountService bankAccountService, final int transactionRate) {
        this.transactionRate = transactionRate;
        this.bankAccountService = bankAccountService;
    }

    public void produceTransactions() {
        creditExecutor.scheduleAtFixedRate(() -> {
            bankAccountService.processTransaction(Transaction.credit(MIN_AMOUNT + Math.random() * (MAX_AMOUNT - MIN_AMOUNT)));
        }, 0, 1000 / transactionRate, TimeUnit.MILLISECONDS);
        debitExecutor.scheduleAtFixedRate(() -> {
            bankAccountService.processTransaction(Transaction.debit(MIN_AMOUNT + Math.random() * (MAX_AMOUNT - MIN_AMOUNT)));
        }, 0, 1000 / transactionRate, TimeUnit.MILLISECONDS);
    }

    public void shutdownExecutors() {
        creditExecutor.shutdown();
        debitExecutor.shutdown();
    }

}
