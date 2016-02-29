package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static HashMap<String, User> users = new HashMap<>();
    //static LocalDate currentDate = LocalDate.now();

    public static void main(String[] args) {
	// write your code here
        Spark.init();
        Spark.get(
                "/",
                ((request, response) -> {
                    User user = getUserFromSession(request.session());

                    if (user == null) {
                        return new ModelAndView(null, "home.html");
                    }
                    else {
                        HashMap m = new HashMap();
                        String offsetStr = request.queryParams("offset");
                        int offset = 0;
                        if (offsetStr != null) {
                            offset = Integer.valueOf(offsetStr);
                        }
                        ArrayList<Bill> shortBills = new ArrayList<>(user.bills.subList(offset, Math.min(offset + 5, user.bills.size())));
                        m.put("bills", shortBills);
                        m.put("end", offset >= user.bills.size() - 5);
                        m.put("beginning", offset == 0);
                        m.put("offsetUp", offset + 5);
                        m.put("offsetDown", offset - 5);
                        return new ModelAndView(m, "bills.html");
                    }
                }),
                new MustacheTemplateEngine()
        );
        Spark.get(
                "/edit",
                ((request, response) -> {
                    User user = getUserFromSession(request.session());
                    int index = Integer.valueOf(request.queryParams("id"));
                    return new ModelAndView(user.bills.get(index), "edit.html");
                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/login",
                ((request, response) -> {
                    String name = request.queryParams("userName");
                    String password = request.queryParams("userPass");
                    User user = users.get(name);
                    if (user == null) {
                        user = new User(name, password);
                        users.put(name, user);
                    }
                    Session session = request.session();
                    session.attribute("userName", name);

                    if (users.get(name).password.equals(password)){
                        response.redirect("/");
                        return "";
                    }
                    else {
                        Spark.halt("403");
                        return "";
                    }
                })
        );
        Spark.post(
                "/logout",
                ((request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/addBill",
                ((request, response) -> {
                    User user = getUserFromSession(request.session());
                    String biller = request.queryParams("biller");
                    LocalDate date = LocalDate.parse(request.queryParams("dueDate"));
                    BigDecimal amount = BigDecimal.valueOf(Double.valueOf(request.queryParams("amountDue")));
                    Bill bill = new Bill(biller, date, amount, user.bills.size());
                    user.bills.add(bill);
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/delete",
                ((request, response) -> {
                    User user = getUserFromSession(request.session());
                    int index = Integer.valueOf(request.queryParams("id"));
                    user.bills.remove(index);
                    for (Bill bill : user.bills) {
                        bill.setId(user.bills.indexOf(bill));
                    }
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/editBill",
                ((request, response) -> {
                    User user = getUserFromSession(request.session());
                    int index = Integer.valueOf(request.queryParams("id"));
                    String biller = request.queryParams("biller");
                    String amountStr = request.queryParams("amountDue");
//                    if (!dueDate.isEmpty()) {
//                        user.bills.get(index).setDateDay(Integer.valueOf(dueDate.split("/")[1]));
//                        user.bills.get(index).setDateMonth(Integer.valueOf(dueDate.split("/")[0]));
//                    }
                    if (!biller.isEmpty()) {
                        user.bills.get(index).setBiller(biller);
                    }
                    if (!amountStr.isEmpty()) {
                        user.bills.get(index).setAmount(BigDecimal.valueOf(Double.valueOf(amountStr)));
                    }
                    response.redirect("/");
                    return "";
                })
        );
    }
    static User getUserFromSession(Session session) {
        String name = session.attribute("userName");
        return users.get(name);
    }
}
