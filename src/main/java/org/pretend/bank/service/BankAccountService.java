package org.pretend.bank.service;


import org.pretend.bank.model.Transaction;

/**
 * Service to track transactions and overall balance
 * */
public interface BankAccountService {

    /**
     * Process a transaction - called from credit and debit producers
     *
     * @param transaction transaction to process
     * */
    void processTransaction(final Transaction transaction);

    /**
     * Get the balance from the account
     * */
    double retrieveBalance();
}
