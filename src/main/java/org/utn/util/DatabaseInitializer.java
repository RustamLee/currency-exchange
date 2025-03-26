package org.utn.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    private static final String URL = "jdbc:sqlite:database/currencies.db";

    public static void initDatabase() {
        File dbDirectory = new File("database");
        if (!dbDirectory.exists()) {
            if (!dbDirectory.mkdirs()) {
                System.err.println("Failed to create database directory.");
                throw new RuntimeException("Failed to create database directory.");
            }
            System.out.println("Database directory created.");
        }

        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found in classpath!");
            throw new RuntimeException("Missing SQLite JDBC driver", e);
        }


        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            // 2. Улучшение SQL-запросов
            String createCurrenciesTableSQL =
                    "CREATE TABLE IF NOT EXISTS Currencies (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "code VARCHAR(10) NOT NULL UNIQUE," +
                            "full_name VARCHAR(50) NOT NULL," +
                            "sign VARCHAR(10) NOT NULL" +
                            ");"; // Добавлен пробел перед закрывающей скобкой

            stmt.execute(createCurrenciesTableSQL);
            System.out.println("The table Currencies is ready.");

            String createExchangeRatesTableSQL =
                    "CREATE TABLE IF NOT EXISTS ExchangeRates (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "base_currency_id INTEGER NOT NULL," +
                            "target_currency_id INTEGER NOT NULL," +
                            "rate DECIMAL(10,6) NOT NULL," +
                            "FOREIGN KEY (base_currency_id) REFERENCES Currencies(id)," +
                            "FOREIGN KEY (target_currency_id) REFERENCES Currencies(id)," +
                            "UNIQUE (base_currency_id, target_currency_id)" +
                            ");"; // Добавлен пробел перед закрывающей скобкой

            stmt.execute(createExchangeRatesTableSQL);
            System.out.println("The table ExchangeRates is ready.");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }
}
