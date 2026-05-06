package org.pretend.bank;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.pretend.bank.controller.BalanceController;
import org.pretend.bank.service.AuditService;
import org.pretend.bank.service.BankAccountService;
import org.pretend.bank.service.BankAccountServiceImpl;
import org.pretend.bank.service.TransactionProducerService;

public class BankAccountOrchestrator {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static void main(String[] args) {
        final AuditService auditService = new AuditService(OBJECT_MAPPER);
        final BankAccountService bankAccountService = new BankAccountServiceImpl(auditService);
        final BalanceController balanceController = new BalanceController(bankAccountService);
        final TransactionProducerService transactionProducerService =
                new TransactionProducerService(bankAccountService, 25);


        transactionProducerService.produceTransactions();
        balanceController.startHttpServer(8080);

        System.out.println("Started Banking Application");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            transactionProducerService.shutdownExecutors();
            bankAccountService.flushTransactions();
            balanceController.stopHttpServer();
            System.out.println("Application shutting down");
        }));
    }
}