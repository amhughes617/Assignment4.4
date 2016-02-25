package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;
import java.time.LocalDate;
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
                        return new ModelAndView(user, "bills.html");
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
                    String dueDate = request.queryParams("dueDate");
                    int dueDay = Integer.valueOf(dueDate.split("/")[1]);
                    int month = Integer.valueOf(dueDate.split("/")[0]);
                    int amount = Integer.valueOf(request.queryParams("amountDue"));
                    Bill bill = new Bill(biller,dueDay, month, amount, user.bills.size());
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
                    String dueDate = request.queryParams("dueDate");
                    String amountStr = request.queryParams("amountDue");
                    if (!dueDate.isEmpty()) {
                        user.bills.get(index).setDateDay(Integer.valueOf(dueDate.split("/")[1]));
                        user.bills.get(index).setDateMonth(Integer.valueOf(dueDate.split("/")[0]));
                    }
                    if (!biller.isEmpty()) {
                        user.bills.get(index).setBiller(biller);
                    }
                    if (!amountStr.isEmpty()) {
                        user.bills.get(index).setAmount(Integer.valueOf(amountStr));
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
