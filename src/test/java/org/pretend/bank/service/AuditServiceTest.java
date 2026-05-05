package org.pretend.bank.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;
import org.pretend.bank.model.Transaction;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuditServiceTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final AuditService auditService = new AuditService(OBJECT_MAPPER);

    @Test
    void shouldRejectSubmissionsWhereNot1000Transactions() {
        List<Transaction> transactions  = new ArrayList<>();
        for(int i = 0; i < 500; i++) {
            transactions.add(Transaction.credit(250.0));
        }
        assertThrows(IllegalArgumentException.class, () -> auditService.submitForAudit(transactions));
    }

    @Test
    void shouldProcessSubmissionOf1000Transactions() {
        List<Transaction> transactions  = new ArrayList<>();
        for(int i = 0; i < 1000; i++) {
            transactions.add(Transaction.credit(250.0));
        }
        assertDoesNotThrow(() -> auditService.submitForAudit(transactions));
    }

    @Test
    void shouldCreateBatchesWithinLimit() {
        List<Transaction> transactions  = new ArrayList<>();
        for(int i = 0; i < 1000; i++) {
            transactions.add(Transaction.credit(250.0));
        }
        List<AuditService.Batch> batches = auditService.generateAuditBatches(transactions);

        for(AuditService.Batch batch: batches) {
            assertTrue(batch.totalValue <= 1_000_000);
        }
    }

    @Test
    void shouldMinimizeBatches_singleBucket() {
        List<Transaction> transactions  = new ArrayList<>();
        for(int i = 0; i < 1000; i++) {
            transactions.add(Transaction.credit(250.0));
        }

        List<AuditService.Batch> batches = auditService.generateAuditBatches(transactions);
        assertEquals(1, batches.size());
    }

    @Test
    void shouldMinimizeBatches_multipleBuckets() {
        List<Transaction> transactions  = new ArrayList<>();
        for(int i = 0; i < 500; i++) {
            transactions.add(Transaction.credit(500_000));
        }
        for(int i = 0; i < 500; i++) {
            transactions.add(Transaction.debit(500_000));
        }

        List<AuditService.Batch> batches = auditService.generateAuditBatches(transactions);
        assertEquals(500, batches.size());
    }

    @Test
    void shouldContainAllTransactions() {
        List<Transaction> transactions  = new ArrayList<>();
        for(int i = 0; i < 1000; i++) {
            transactions.add(Transaction.credit(250_000));
        }

        List<AuditService.Batch> batches = auditService.generateAuditBatches(transactions);
        int totalTransactionCount = batches.stream().mapToInt(b -> b.transactions.size()).sum();
        assertEquals(1000, totalTransactionCount);
    }

    @Test
    void shouldPrintAllTransactions() throws JsonProcessingException {
        List<Transaction> transactions  = new ArrayList<>();
        for(int i = 0; i < 1000; i++) {
            transactions.add(Transaction.credit(1_000_000));
        }

        List<AuditService.Batch> batches = auditService.generateAuditBatches(transactions);
        String json = auditService.generateJSONAuditLogs(batches);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        assertTrue(root.has("submission"));
        JsonNode submission = root.get("submission");
        JsonNode batchesNode = submission.get("batches");
        assertTrue(batchesNode.isArray());
        assertEquals(1000, batchesNode.size());

        JsonNode firstBatch = batchesNode.get(0);
        assertEquals(1_000_000, firstBatch.get("totalValueOfAllTransactions").asDouble());
        assertEquals(1, firstBatch.get("countOfTransactions").asInt());
    }

    @Test
    void shouldThrowJsonProcessingException () throws JsonProcessingException {
        ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        when(mockObjectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("error") {});
        AuditService throwingAuditService = new AuditService(mockObjectMapper);
        List<Transaction> transactions  = new ArrayList<>();
        for(int i = 0; i < 1000; i++) {
            transactions.add(Transaction.credit(1_000_000));
        }

        assertThrows(RuntimeException.class, () -> throwingAuditService.submitForAudit(transactions));
    }
}