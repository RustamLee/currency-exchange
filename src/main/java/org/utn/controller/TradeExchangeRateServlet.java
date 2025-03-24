package org.utn.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.utn.dao.CurrencyDAO;
import org.utn.dao.ExchangeRateDAO;
import org.utn.model.Currency;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

public class TradeExchangeRateServlet extends HttpServlet  {
    private ExchangeRateDAO exchangeRateDAO;
    private  CurrencyDAO currencyDAO;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void init() throws ServletException {
        super.init();
        exchangeRateDAO = new ExchangeRateDAO();
        currencyDAO = new CurrencyDAO();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String from = req.getParameter("from");
        String to = req.getParameter("to");
        String amountStr = req.getParameter("amount");

        if (from == null || to == null || amountStr == null) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Missing required parameters");
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid amount format");
            return;
        }

        BigDecimal rate = exchangeRateDAO.getExchangeRateWithFallback(from, to);
        if (rate == null) {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Exchange rate not found");
            return;
        }

        BigDecimal convertedAmount = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        Map<String, Object> responseJson = buildExchangeResponse(from, to, rate, amount, convertedAmount);

        sendJsonResponse(resp, HttpServletResponse.SC_OK, responseJson);
    }

    private Map<String, Object> buildCurrencyInfo(Currency currency) {
        Map<String, Object> currencyInfo = new LinkedHashMap<>();
        currencyInfo.put("id", currency.getId());
        currencyInfo.put("name", currency.getName());
        currencyInfo.put("code", currency.getCode());
        currencyInfo.put("sign", currency.getSign());
        return currencyInfo;
    }

    private Map<String, Object> buildExchangeResponse(String from, String to, BigDecimal rate, BigDecimal amount, BigDecimal convertedAmount) {
        Map<String, Object> responseJson = new LinkedHashMap<>();

        Currency baseCurrency = currencyDAO.getCurrencyByCode(from);
        Currency targetCurrency = currencyDAO.getCurrencyByCode(to);

        responseJson.put("baseCurrency", buildCurrencyInfo(baseCurrency));
        responseJson.put("targetCurrency", buildCurrencyInfo(targetCurrency));

        responseJson.put("rate", rate);
        responseJson.put("amount", amount);
        responseJson.put("convertedAmount", convertedAmount);

        return responseJson;
    }

    private void sendJsonResponse(HttpServletResponse resp, int status, Map<String, Object> data) throws IOException {
        resp.setContentType("application/json");
        resp.setStatus(status);
        resp.getWriter().write(gson.toJson(data));
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        sendJsonResponse(resp, status, Map.of("error", message));
    }
}

