package org.pretend.bank.service;

import org.pretend.bank.model.Transaction;


import java.util.ArrayList;
import java.util.List;

public class BankAccountServiceImpl implements BankAccountService {

    private static final int AUDIT_TRANSACTION_COUNT = 1000;
    private double balance = 0.0;
    private final List<Transaction> transactions = new ArrayList<>();

    final AuditService auditService;

    public BankAccountServiceImpl(final AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public synchronized void processTransaction(final Transaction transaction) {
        balance += transaction.amount();
        transactions.add(transaction);
        if(transactions.size() == AUDIT_TRANSACTION_COUNT) {
            System.out.println(String.format("Received %s transactions, sending batch to Audit", AUDIT_TRANSACTION_COUNT));
            auditService.submitForAudit(transactions);
            transactions.clear();
        }
    }

    @Override
    public synchronized double retrieveBalance() {
        return balance;
    }
}
