package org.utn.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
public class DatabaseInitializer {
    private static final String URL = "jdbc:sqlite:database/currencies.db";

    public static void initDatabase() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            // Create table Currencies
            String createCurrenciesTableSQL = """
                    CREATE TABLE IF NOT EXISTS Currencies (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        code VARCHAR(10) NOT NULL UNIQUE,
                        full_name VARCHAR(50) NOT NULL,
                        sign VARCHAR(10) NOT NULL
                    );
                    """;
            stmt.execute(createCurrenciesTableSQL);
            System.out.println("The table Currencies is ready.");

            // Create table ExchangeRates
            String createExchangeRatesTableSQL = """
                    CREATE TABLE IF NOT EXISTS ExchangeRates (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        base_currency_id INTEGER NOT NULL,
                        target_currency_id INTEGER NOT NULL,
                        rate DECIMAL(10,6) NOT NULL,
                        FOREIGN KEY (base_currency_id) REFERENCES Currencies(id),
                        FOREIGN KEY (target_currency_id) REFERENCES Currencies(id),
                        UNIQUE (base_currency_id, target_currency_id)
                    );
                    """;
            stmt.execute(createExchangeRatesTableSQL);
            System.out.println("The table ExchangeRates is ready.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}

