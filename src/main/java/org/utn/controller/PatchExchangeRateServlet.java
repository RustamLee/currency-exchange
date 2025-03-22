package org.utn.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.utn.dao.ExchangeRateDAO;
import org.utn.model.Currency;
import org.utn.model.ExchangeRate;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class PatchExchangeRateServlet extends GenericServlet {
    private ExchangeRateDAO exchangeRateDAO;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void init() throws ServletException {
        super.init();
        exchangeRateDAO = new ExchangeRateDAO();
    }

    @Override
    public void service(ServletRequest req, ServletResponse resp) throws IOException {

        HttpServletRequest httpRequest = (HttpServletRequest) req;
        HttpServletResponse httpResponse = (HttpServletResponse) resp;

        if ("PATCH".equalsIgnoreCase(httpRequest.getMethod())) {
            doPatch(httpRequest, httpResponse);
        } else {
            httpResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method Not Allowed");
        }
    }

    private void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        System.out.println("Enter to doPatch servlet");
        String pathInfo = req.getPathInfo();
        System.out.println("Path Info: " + pathInfo);
        BufferedReader reader = req.getReader();
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        System.out.println("Raw body: " + body.toString());
        String[] pathParts = pathInfo.split("/");
        if (pathParts.length < 2 || pathParts[1].length() != 6) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid currency pair format");
            return;
        }

        String currencyPair = pathParts[1];
        String baseCurrencyCode = currencyPair.substring(0, 3);
        String targetCurrencyCode = currencyPair.substring(3, 6);

        System.out.println("Base Currency: " + baseCurrencyCode);
        System.out.println("Target Currency: " + targetCurrencyCode);

        String rateStr = null;
        String[] pairs = body.toString().split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue[0].equals("rate")) {
                rateStr = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                break;
            }
        }
        System.out.println("Rate parameter: " + rateStr);

        if (rateStr == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required rate parameter");
            return;
        }

        BigDecimal rate;
        try {
            rate = new BigDecimal(rateStr);
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid rate format");
            return;
        }

        boolean updated = exchangeRateDAO.updateExchangeRate(baseCurrencyCode, targetCurrencyCode, rate);
        if (updated) {
            ExchangeRate updatedExchangeRate = exchangeRateDAO.getExchangeRateByCodes(baseCurrencyCode, targetCurrencyCode);
            if (updatedExchangeRate != null) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");
                resp.getWriter().write(gson.toJson(buildExchangeRateMap(updatedExchangeRate)));
            } else {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to fetch updated exchange rate");
            }
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Currency pair not found");
        }
    }


    private Map<String, Object> buildExchangeRateMap(ExchangeRate exchangeRate) {
        Map<String, Object> exchangeRateInfo = new LinkedHashMap<>();
        exchangeRateInfo.put("id", exchangeRate.getId());
        exchangeRateInfo.put("baseCurrency", buildCurrencyMap(exchangeRate.getBaseCurrency()));
        exchangeRateInfo.put("targetCurrency", buildCurrencyMap(exchangeRate.getTargetCurrency()));
        exchangeRateInfo.put("rate", exchangeRate.getRate());

        return exchangeRateInfo;
    }

    private Map<String, Object> buildCurrencyMap(Currency currency) {
        Map<String, Object> currencyInfo = new LinkedHashMap<>();
        currencyInfo.put("id", currency.getId());
        currencyInfo.put("name", currency.getFullName());
        currencyInfo.put("code", currency.getCode());
        currencyInfo.put("sign", currency.getSign());

        return currencyInfo;
    }
}
