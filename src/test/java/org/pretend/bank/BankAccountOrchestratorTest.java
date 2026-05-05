package org.pretend.bank;

import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BankAccountOrchestratorTest {

    @Test
    void shouldStartUpAndShutDownWithoutError() {
        assertDoesNotThrow(() -> {
            BankAccountOrchestrator.main(new String[]{});

            Thread.sleep(500);

            HttpURLConnection conn = (HttpURLConnection) URI.create("http://localhost:8080").toURL().openConnection();
            conn.setRequestMethod("GET");
            assertEquals(200, conn.getResponseCode());
        });
    }
}