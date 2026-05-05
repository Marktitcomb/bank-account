package org.pretend.bank.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pretend.bank.service.BankAccountService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BalanceControllerTest {

    private BalanceController controller;
    private static final int TEST_PORT = 9876;

    @BeforeEach
    void setUp() {
        BankAccountService bankAccountService = mock(BankAccountService.class);
        when(bankAccountService.retrieveBalance()).thenReturn(200000.00);
        controller = new BalanceController(bankAccountService);
        controller.startHttpServer(TEST_PORT);
    }

    @AfterEach
    void tearDown() {
        controller.stopHttpServer();
    }

    @Test
    void shouldReturnBalanceAsJson() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create("http://localhost:" + TEST_PORT + "/pretend-bank/balance").toURL().openConnection();
        connection.setRequestMethod("GET");

        assertEquals(200, connection.getResponseCode());
        String body = new String(connection.getInputStream().readAllBytes());
        assertTrue(body.contains("200000.00"));
    }

    @Test
    void shouldReturnBalanceAsJsonForBasePath() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) URI.create("http://localhost:" + TEST_PORT).toURL().openConnection();
        connection.setRequestMethod("GET");

        assertEquals(200, connection.getResponseCode());
        String body = new String(connection.getInputStream().readAllBytes());
        assertTrue(body.contains("200000.00"));
    }

}