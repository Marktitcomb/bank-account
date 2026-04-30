package org.pretend.bank;


import org.pretend.bank.service.BankAccountService;
import org.pretend.bank.service.BankAccountServiceImpl;
import org.pretend.bank.service.TransactionProducerService;

public class Main {
    public static void main(String[] args) {
        final BankAccountService bankAccountService = new BankAccountServiceImpl();
        final TransactionProducerService transactionProducerService =
                new TransactionProducerService(bankAccountService, 25);


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            transactionProducerService.shutdownExecutors();
        }));
    }
}