package org.pretend.bank;


import org.pretend.bank.service.BankAccountService;
import org.pretend.bank.service.BankAccountServiceImpl;

public class Main {
    public static void main(String[] args) {
        final BankAccountService bankAccountService = new BankAccountServiceImpl();
    }
}