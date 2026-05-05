package org.pretend.bank.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.pretend.bank.service.BankAccountService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class BalanceController {

    private static final String BALANCE_RESOURCE = "/pretend-bank/balance";
    private static final String GET = "GET";

    private final BankAccountService bankAccountService;
    private HttpServer server;

    public BalanceController(final BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    public void startHttpServer(int port) {
        try{
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext(BALANCE_RESOURCE, exchange -> {
               if(! GET.equals(exchange.getRequestMethod())){
                   exchange.sendResponseHeaders(405, -1);
                   return;
               }
               final byte[] response = getBalanceInBytes();
               populateHeaders(exchange, response);
               try (OutputStream os = exchange.getResponseBody()) {
                   os.write(response);
               }
            });
            server.setExecutor(null);
            server.start();
            System.out.println("starting http server");
        } catch(final IOException ex) {
            throw new RuntimeException("Failed to start HTTP server", ex);
        }
    }

    public void stopHttpServer() {
        if(server != null) {
            server.stop(0);
        }
    }

    private byte[] getBalanceInBytes() {
        final double balance = bankAccountService.retrieveBalance();
        final String response = String.format("{\"balance\":%.2f}", balance);
        return response.getBytes();
    }

    private void populateHeaders(final HttpExchange exchange, final byte[] response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
    }
}
