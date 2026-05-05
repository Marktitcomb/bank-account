package org.pretend.bank;


import org.pretend.bank.controller.BalanceController;
import org.pretend.bank.service.BankAccountService;
import org.pretend.bank.service.BankAccountServiceImpl;
import org.pretend.bank.service.TransactionProducerService;

public class Main {
    public static void main(String[] args) {
        final BankAccountService bankAccountService = new BankAccountServiceImpl();
        final BalanceController balanceController = new BalanceController(bankAccountService);
        final TransactionProducerService transactionProducerService =
                new TransactionProducerService(bankAccountService, 25);

        transactionProducerService.produceTransactions();
        balanceController.startHttpServer(8080);

        System.out.println("Started Banking Application");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            transactionProducerService.shutdownExecutors();
            balanceController.stopHttpServer();
        }));
    }
}