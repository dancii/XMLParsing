package com.dancii.xmlparsing;

/**
 * Created by dancii on 14-11-11.
 */
public class Currency {

    private String currency;
    private double rate;

    public Currency(String currency, double rate){
        this.currency=currency;
        this.rate=rate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }
}
