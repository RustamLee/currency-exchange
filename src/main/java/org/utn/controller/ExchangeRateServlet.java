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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExchangeRateServlet extends HttpServlet {

    private final ExchangeRateDAO exchangeRateDAO = new ExchangeRateDAO();
    private final Gson gson = new Gson();
    private final CurrencyDAO currencyDAO = new CurrencyDAO();


    // to get some specific change rate /USDARS

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("Enter doGet ExchangeRateServlet");

        try {
            String path = req.getPathInfo();
            if (path == null || path.length() <= 1) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request path");
                return;
            }

            String[] currencyCodes = path.substring(1).split("(?<=\\G.{3})");
            if (currencyCodes.length != 2) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid currency pair format");
                return;
            }

            String baseCode = currencyCodes[0];
            String targetCode = currencyCodes[1];

            ExchangeRate exchangeRate = exchangeRateDAO.getExchangeRateByCodes(baseCode, targetCode);

            if (exchangeRate == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Exchange rate not found for " + baseCode + targetCode);
                return;
            }

            Map<String, Object> responseMap = buildExchangeRateMap(exchangeRate);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            resp.getWriter().write(gson.toJson(responseMap));

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
