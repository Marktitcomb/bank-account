package org.pretend.bank.service;

import org.pretend.bank.model.Transaction;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class BankAccountServiceImpl implements BankAccountService {

    private static final int AUDIT_TRANSACTION_COUNT = 1000;
    private double balance = 0.0;
    private final List<Transaction> transactions = new ArrayList<>(AUDIT_TRANSACTION_COUNT);
    private final ReentrantLock lock = new ReentrantLock();
    private final ExecutorService auditExecutor = Executors.newSingleThreadExecutor();

    final AuditService auditService;

    public BankAccountServiceImpl(final AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public void processTransaction(final Transaction transaction) {
        List<Transaction> batch = null;

        lock.lock();
        try{
            balance += transaction.amount();
            transactions.add(transaction);
            if(transactions.size() == AUDIT_TRANSACTION_COUNT) {
                batch = new ArrayList<>(transactions);
                System.out.printf("Received %s transactions, sending batch to Audit", AUDIT_TRANSACTION_COUNT);
                transactions.clear();
            }
        } finally {
            lock.unlock();
        }

        if(batch != null) {
            final List<Transaction> batchToSubmit = batch;
            auditExecutor.submit(() -> auditService.submitForAudit(batchToSubmit));
        }
    }

    @Override
    public synchronized double retrieveBalance() {
        return balance;
    }

    @Override
    public void flushTransactions() {
        List<Transaction> remainingTransactions = null;

        lock.lock();
        try{
            if(! transactions.isEmpty()) {
                remainingTransactions = new ArrayList<>(transactions);
                transactions.clear();
                System.out.printf("Flushing remaining %s transactions \n", remainingTransactions.size());
            }
        } finally {
            lock.unlock();
        }

        if( remainingTransactions != null) {
            auditService.submitForAudit(remainingTransactions);
        }

        auditExecutor.shutdown();
        try {
            if(! auditExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                auditExecutor.shutdownNow();
            }
        } catch(final InterruptedException ex) {
            auditExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
