package com.theironyard;


import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Created by alexanderhughes on 2/25/16.
 */
public class Bill {
    LocalDate currentDate = LocalDate.now();
    String biller;
    BigDecimal amount;
    int id;
    LocalDate dueDate;
    boolean dueSoon;

    public Bill(String biller, LocalDate dueDate, BigDecimal amount, int id) {
        this.biller = biller;
        this.dueDate = dueDate;
        this.amount = amount;
        this.id = id;
    }

    public String getBiller() {
        return biller;
    }

    public void setBiller(String biller) {
        this.biller = biller;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isDueSoon() {
        return dueSoon;
    }

    public void setDueSoon(boolean dueSoon) {
        this.dueSoon = dueSoon;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void flipDueSoon() {
        if (dueDate.isAfter(currentDate) && currentDate.getYear() == dueDate.getYear() && dueDate.getDayOfYear() - currentDate.getDayOfYear() < 10) {
            setDueSoon(true);
        } else {
            setDueSoon(false);
        }
    }


    @Override
    public String toString() {
        return String.format("%s/%s%s$%.2f", dueDate.getMonthValue(), dueDate.getDayOfMonth(), biller, amount);
    }
}
