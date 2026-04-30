package org.pretend.bank.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void creditShouldHavePositiveAmount() {
        Transaction transaction = Transaction.credit(200);
        assertTrue(transaction.amount() > 0);
        assertNotNull(transaction.transactionId());
    }

    @Test
    void debitShouldHaveNegativeAmount() {
        Transaction transaction = Transaction.debit(200);
        assertTrue(transaction.amount() < 0);
        assertNotNull(transaction.transactionId());
    }

    @Test
    void transactionWithNullTransactionIdThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Transaction(null, 300));
    }

    @Test
    void transactionThrowsExceptionForNonPositiveAmounts() {
        assertThrows(IllegalArgumentException.class, () -> Transaction.credit(0));
        assertThrows(IllegalArgumentException.class, () -> Transaction.credit(-100));
        assertThrows(IllegalArgumentException.class, () -> Transaction.debit(-100));
        assertThrows(IllegalArgumentException.class, () -> Transaction.debit(-100));
    }

}