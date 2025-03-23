package org.utn.dao;

import org.utn.model.Currency;
import org.utn.model.ExchangeRate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExchangeRateDAO {

    private static final String URL = "jdbc:sqlite:database/currencies.db";
    private final CurrencyDAO currencyDAO = new CurrencyDAO();


    public List<ExchangeRate> getAllExchangeRates() {
        List<ExchangeRate> exchangeRates = new ArrayList<>();
        String sql = "SELECT * FROM ExchangeRates";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int baseCurrencyId = rs.getInt("base_currency_id");
                int targetCurrencyId = rs.getInt("target_currency_id");

                Currency baseCurrency = currencyDAO.getCurrencyById(baseCurrencyId);
                Currency targetCurrency = currencyDAO.getCurrencyById(targetCurrencyId);

                exchangeRates.add(new ExchangeRate(
                        rs.getInt("id"),
                        baseCurrency,
                        targetCurrency,
                        rs.getBigDecimal("rate")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return exchangeRates;
    }

    public BigDecimal getExchangeRate(int baseCurrencyId, int targetCurrencyId) {
        String sql = "SELECT rate FROM ExchangeRates WHERE base_currency_id = ? AND target_currency_id = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:database/currencies.db");
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, baseCurrencyId);
            pstmt.setInt(2, targetCurrencyId);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal("rate");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public ExchangeRate getExchangeRateByCodes(String baseCode, String targetCode) {
        Currency baseCurrency = currencyDAO.getCurrencyByCode(baseCode);
        Currency targetCurrency = currencyDAO.getCurrencyByCode(targetCode);

        if (baseCurrency == null || targetCurrency == null) {
            return null;
        }

        String sql = "SELECT id, rate FROM ExchangeRates WHERE base_currency_id = ? AND target_currency_id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, baseCurrency.getId());
            pstmt.setInt(2, targetCurrency.getId());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new ExchangeRate(
                            rs.getInt("id"),
                            baseCurrency,
                            targetCurrency,
                            rs.getBigDecimal("rate")
                    );
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return null;
    }


    public ExchangeRate addExchangeRate(Currency baseCurrency, Currency targetCurrency, BigDecimal rate) {
        if (baseCurrency == null || targetCurrency == null) {
            return null;
        }
        if (getExchangeRateByCodes(baseCurrency.getCode(), targetCurrency.getCode()) != null) {
            return null;
        }

        String sql = "INSERT INTO ExchangeRates (base_currency_id, target_currency_id, rate) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, baseCurrency.getId());
            pstmt.setInt(2, targetCurrency.getId());
            pstmt.setBigDecimal(3, rate);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                return null;
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    return new ExchangeRate(id, baseCurrency, targetCurrency, rate);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return null;
    }

    public boolean updateExchangeRate(String baseCurrencyCode, String targetCurrencyCode, BigDecimal rate) {
        Currency baseCurrency = currencyDAO.getCurrencyByCode(baseCurrencyCode);
        Currency targetCurrency = currencyDAO.getCurrencyByCode(targetCurrencyCode);
        try {
            if (baseCurrency == null || targetCurrency == null) {
                return false;
            }

        } catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
        String sql = "UPDATE ExchangeRates SET rate = ? WHERE base_currency_id = ? AND target_currency_id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, rate);
            pstmt.setInt(2, baseCurrency.getId());
            pstmt.setInt(3, targetCurrency.getId());

            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return false;
    }

    public BigDecimal getExchangeRateWithFallback(String fromCode, String toCode) {
        ExchangeRate directRate = getExchangeRateByCodes(fromCode, toCode);
        if (directRate != null) {
            return directRate.getRate();
        }

        ExchangeRate reverseRate = getExchangeRateByCodes(toCode, fromCode);
        if (reverseRate != null) {
            return BigDecimal.ONE.divide(reverseRate.getRate(), 6, RoundingMode.HALF_UP);
        }

        ExchangeRate fromUsd = getExchangeRateByCodes("USD", fromCode);
        ExchangeRate toUsd = getExchangeRateByCodes("USD", toCode);

        if (fromUsd != null && toUsd != null) {
            return toUsd.getRate().divide(fromUsd.getRate(), 6, RoundingMode.HALF_UP);
        }

        return null;
    }


}

