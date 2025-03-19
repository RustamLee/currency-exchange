package org.utn;

import org.utn.util.DatabaseInitializer;

public class CurrencyExchangeApplication {
    public static void main(String[] args) {
        DatabaseInitializer.initDatabase();
        System.out.println("Currency Exchange Application is running!");
    }
}
