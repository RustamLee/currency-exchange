package org.utn.controller;

import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.utn.dao.CurrencyDAO;
import org.utn.model.Currency;

import java.io.IOException;
import java.util.List;

public class CurrencyServlet extends HttpServlet {
    private final CurrencyDAO currencyDAO = new CurrencyDAO();
    private final Gson gson = new Gson();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();

        if (path == null || path.equals("/")) {
            List<Currency> currencies = currencyDAO.getAllCurrencies();
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(gson.toJson(currencies));
            return;
        }

        String currencyCode = path.substring(1).toUpperCase();

        Currency currency = currencyDAO.getCurrencyByCode(currencyCode);
        if (currency == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\": \"Currency not found\"}");
            return;
        }

        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(currency));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Currency newCurrency = gson.fromJson(req.getReader(), Currency.class);

        boolean success = currencyDAO.addCurrency(newCurrency);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (success) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(gson.toJson(newCurrency));
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Failed to add currency\"}");
        }
    }


}
