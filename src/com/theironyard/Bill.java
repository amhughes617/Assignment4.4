package com.theironyard;


/**
 * Created by alexanderhughes on 2/25/16.
 */
public class Bill {
    String biller;
    int dateDay, dateMonth;
    int amount;
    int id;
    //boolean isDone;

    public Bill(String biller, int dateDay, int dateMonth, int amount, int id) {
        this.biller = biller;
        this.dateDay = dateDay;
        this.dateMonth = dateMonth;
        this.amount = amount;
        this.id = id;
    }

    public String getBiller() {
        return biller;
    }

    public void setBiller(String biller) {
        this.biller = biller;
    }

    public int getDateDay() {
        return dateDay;
    }

    public void setDateDay(int dateDay) {
        this.dateDay = dateDay;
    }

    public int getDateMonth() {
        return dateMonth;
    }

    public void setDateMonth(int dateMonth) {
        this.dateMonth = dateMonth;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
