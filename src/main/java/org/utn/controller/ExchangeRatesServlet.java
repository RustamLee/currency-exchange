package org.utn.controller;

import com.google.gson.Gson;
import org.utn.dao.CurrencyDAO;
import org.utn.dao.ExchangeRateDAO;
import org.utn.model.Currency;
import org.utn.model.ExchangeRate;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// to get all change rates

public class ExchangeRatesServlet extends HttpServlet {
    private final ExchangeRateDAO exchangeRateDAO = new ExchangeRateDAO();
    private final Gson gson = new Gson();
    private final CurrencyDAO currencyDAO = new CurrencyDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo();
            if (path == null || path.isEmpty()) { // Исправленная проверка
                List<ExchangeRate> exchangeRates = exchangeRateDAO.getAllExchangeRates();
                List<Map<String, Object>> responseList = new ArrayList<>();

                for (ExchangeRate exchangeRate : exchangeRates) {
                    Map<String, Object> exchangeRateData = new LinkedHashMap<>();
                    exchangeRateData.put("id", exchangeRate.getId());

                    Currency baseCurrency = currencyDAO.getCurrencyById(exchangeRate.getBaseCurrencyId());
                    if (baseCurrency != null) {
                        exchangeRateData.put("baseCurrency", buildCurrencyMap(baseCurrency));
                    }

                    Currency targetCurrency = currencyDAO.getCurrencyById(exchangeRate.getTargetCurrencyId());
                    if (targetCurrency != null) {
                        exchangeRateData.put("targetCurrency", buildCurrencyMap(targetCurrency));
                    }

                    exchangeRateData.put("rate", exchangeRate.getRate());
                    responseList.add(exchangeRateData);
                }

                resp.setContentType("application/json");
                resp.setCharacterEncoding("UTF-8");
                resp.getWriter().write(gson.toJson(responseList));
                return;
            }
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid request path\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"An unexpected error occurred\"}");
        }
    }


    // create a new changeRate
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            System.out.println("Parameters received from the request");
            Map<String, String[]> parameters = req.getParameterMap();
            parameters.forEach((key, value) -> System.out.println(key + ": " + Arrays.toString(value)));

            if (!parameters.containsKey("baseCurrencyCode") ||
                    !parameters.containsKey("targetCurrencyCode") ||
                    !parameters.containsKey("rate")) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
                return;
            }

            String baseCurrencyCode = parameters.get("baseCurrencyCode")[0];
            String targetCurrencyCode = parameters.get("targetCurrencyCode")[0];
            String rateStr = parameters.get("rate")[0];


            if (baseCurrencyCode == null || targetCurrencyCode == null || rateStr == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required form fields");
                return;
            }
            BigDecimal rate;
            try {
                rate = new BigDecimal(rateStr);
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid rate format");
                return;
            }
            Currency baseCurrency = currencyDAO.getCurrencyByCode(baseCurrencyCode);
            Currency targetCurrency = currencyDAO.getCurrencyByCode(targetCurrencyCode);
            if (baseCurrency == null || targetCurrency == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "One or both currency not found");
                return;
            }
            if (exchangeRateDAO.getExchangeRateByCodes(baseCurrencyCode, targetCurrencyCode) != null) {
                resp.sendError(HttpServletResponse.SC_CONFLICT, "Exchange rate already exists");
                return;
            }
            ExchangeRate newExchangeRate = exchangeRateDAO.addExchangeRate(baseCurrency, targetCurrency, rate);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(gson.toJson(buildExchangeRateMap(newExchangeRate)));
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private Map<String, Object> buildCurrencyMap(Currency currency) {
        Map<String, Object> currencyInfo = new LinkedHashMap<>();
        currencyInfo.put("id", currency.getId());
        currencyInfo.put("name", currency.getName());
        currencyInfo.put("code", currency.getCode());
        currencyInfo.put("sign", currency.getSign());
        return currencyInfo;
    }

    private Map<String, Object> buildExchangeRateMap(ExchangeRate exchangeRate) {
        Map<String, Object> exchangeRateInfo = new LinkedHashMap<>();
        exchangeRateInfo.put("id", exchangeRate.getId());
        exchangeRateInfo.put("baseCurrency", buildCurrencyMap(exchangeRate.getBaseCurrency()));

        exchangeRateInfo.put("targetCurrency", buildCurrencyMap(exchangeRate.getTargetCurrency()));
        exchangeRateInfo.put("rate", exchangeRate.getRate());

        return exchangeRateInfo;
    }

}
