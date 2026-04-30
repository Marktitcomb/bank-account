package org.pretend.bank.service;

import org.pretend.bank.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.ArrayList;
import java.util.List;

public class BankAccountServiceImpl implements BankAccountService {

    private static final Logger logger = LoggerFactory.getLogger(BankAccountServiceImpl.class);

    private double balance = 0.0;
    private final List<Transaction> transactions = new ArrayList<>();

    @Override
    public synchronized void processTransaction(final Transaction transaction) {
        balance += transaction.amount();
        transactions.add(transaction);
        if(transactions.size() == 1000) {
            logger.info("Received 1000 transactions, sending batch to Audit");
            //AuditService.processBatch()
            transactions.clear();
        }
    }

    @Override
    public synchronized double retrieveBalance() {
        return balance;
    }
}
