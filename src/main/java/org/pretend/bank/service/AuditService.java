package org.pretend.bank.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pretend.bank.model.Transaction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Audit Service responsible to batching transactions and submitting them
 * to a downstream audit system
 *
 * Each submission contains a defined number of transactions which are separated into
 * batches with a maximum value of 1_000_000. The number of batches is minimized using
 * the first-fit-decreasing algorithm.
 * */
public class AuditService {

    private static final double MAX_BATCH_VALUE = 1_000_000;
    //Chunk size can be used in future to optimize memory
    private static final int CHUNK_SIZE  = 1000;

    private final ObjectMapper objectMapper;

    public AuditService(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void submitForAudit(final List<Transaction> transactions) {
        List<Batch> batches = generateAuditBatches(transactions);
        System.out.println(generateJSONAuditLogs(batches));
    }

    /**
     * Function to assigned transactions to batches using first-fit-decreasing algorithm.
     * Chunking is also used sacrificing the optimization of batches to optimize memory
     * by reducing the number of transactions in memory when sorting.
     * To optimize for batches set CHUNK_SIZE = BankAccountService.AUDIT_TRANSACTION_COUNT
     * */
    List<Batch> generateAuditBatches(final List<Transaction> transactions) {

        final List<Batch> batches = new ArrayList<>();
        final int auditTransactionsCount = transactions.size();

        for(int i = 0; i < auditTransactionsCount; i += CHUNK_SIZE) {
            final int chunkEndIndex = Math.min((i + CHUNK_SIZE), auditTransactionsCount);
            final List<Transaction> transactionsChunk = new ArrayList<>(transactions.subList(i, chunkEndIndex));
            transactionsChunk.sort(Comparator.comparingDouble(t -> -Math.abs(t.amount())));

            for(final Transaction transaction: transactionsChunk) {
                placeTransactionInBatch(batches, transaction);
            }
        }

        return batches;
    }

    private void placeTransactionInBatch(final List<Batch> batches, final Transaction transaction) {
        double transactionValue = Math.abs(transaction.amount());
        boolean placed = false;

        for(final Batch batch : batches) {
            if(batch.totalValue + transactionValue <= MAX_BATCH_VALUE) {
                batch.totalValue += transactionValue;
                batch.transactions.add(transaction);
                placed = true;
                break;
            }
        }

        if(! placed) {
            final Batch batch = new Batch();
            batch.totalValue = transactionValue;
            batch.transactions.add(transaction);
            batches.add(batch);
        }
    }

    String generateJSONAuditLogs(final List<Batch> batches) {
        Submission submission = new Submission(batches.stream()
                .map(b -> new BatchSummary(b.totalValue, b.transactions.size()))
                .toList());
        try {
            return objectMapper.writeValueAsString(new SubmissionWrapper(submission));
        } catch(final JsonProcessingException ex) {
            throw new RuntimeException("Failed to serialize audit submission");
        }
    }

    record SubmissionWrapper(Submission submission) {}

    record Submission(List<BatchSummary> batches) {}

    record BatchSummary(double totalValueOfAllTransactions, int countOfTransactions) {}

    static class Batch {
        final List<Transaction> transactions = new ArrayList<>();
        double totalValue = 0.0;
    }
}
