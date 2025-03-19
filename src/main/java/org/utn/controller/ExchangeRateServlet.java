package org.utn.controller;

import com.google.gson.Gson;
import org.utn.dao.CurrencyDAO;
import org.utn.dao.ExchangeRateDAO;
import org.utn.model.Currency;
import org.utn.model.ExchangeRate;
import org.w3c.dom.ls.LSOutput;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

public class ExchangeRateServlet extends HttpServlet {
    private final ExchangeRateDAO exchangeRateDAO = new ExchangeRateDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String path = req.getPathInfo();
            if (path == null || path.equals("/exchangeRates")) {
                List<ExchangeRate> exchangeRates = exchangeRateDAO.getAllExchangeRates();
                List<Map<String, Object>> responseList = new ArrayList<>();

                CurrencyDAO currencyDAO = new CurrencyDAO();

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
            System.out.println(e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"An unexpected error occurred\"}");
        }
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
