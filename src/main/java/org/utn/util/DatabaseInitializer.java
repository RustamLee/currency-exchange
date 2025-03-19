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

            String createTableSQL = """
                    CREATE TABLE IF NOT EXISTS Currencies (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        code VARCHAR(10) NOT NULL UNIQUE,
                        full_name VARCHAR(50) NOT NULL,
                        sign VARCHAR(10) NOT NULL
                    );
                    """;
            stmt.execute(createTableSQL);
            System.out.println("The table Currencies is ready.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
