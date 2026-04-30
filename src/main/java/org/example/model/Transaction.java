package org.example.model;

import java.util.UUID;

public record Transaction(UUID transactionId, double amount) {

    private static final int NEGATIVE = -1;

    public Transaction {
        if(transactionId == null) {
            throw new IllegalArgumentException("TransactionId cannot be null");
        }
    }

    public static Transaction credit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        return new Transaction(UUID.randomUUID(), amount);
    }

    public static Transaction debit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        return new Transaction(UUID.randomUUID(), amount*NEGATIVE);
    }
}
