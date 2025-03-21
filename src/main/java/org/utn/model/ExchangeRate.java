package org.utn.model;

import java.math.BigDecimal;

public class ExchangeRate {
    private int id;
    private int baseCurrencyId;
    private int targetCurrencyId;
    private BigDecimal rate;
    private Currency baseCurrency;
    private Currency targetCurrency;

    public ExchangeRate(int id, Currency baseCurrency,Currency targetCurrency, BigDecimal rate) {
        this.id = id;
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.baseCurrencyId = baseCurrency.getId();
        this.targetCurrencyId = targetCurrency.getId();
        this.rate = rate;
    }

    public int getId() { return id; }
    public int getBaseCurrencyId() { return baseCurrencyId; }
    public int getTargetCurrencyId() { return targetCurrencyId; }
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    public Currency getTargetCurrency() {
        return targetCurrency;
    }
}
