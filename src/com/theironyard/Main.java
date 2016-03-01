package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) throws SQLException {
        Connection conn  = DriverManager.getConnection("jdbc:h2:./main");//creates a connection to database
        createTables(conn);//creates tables of users and bills to store info and match data to users
        // write your code here
        Spark.init();
        Spark.get(
                "/",
                ((request, response) -> {
                    User user = getUserFromSession(request.session(), conn);

                    if (user == null) {//checks if a user is logged in
                        return new ModelAndView(null, "home.html");
                    }
                    else {
                        HashMap m = new HashMap();//use to pass in data to the bills html file
                        String offsetStr = request.queryParams("offset");//is null first run through
                        int offset = 0;
                        if (offsetStr != null) {
                            offset = Integer.valueOf(offsetStr);
                        }
                        ArrayList<Bill> bills = selectBills(conn, user.id);
                        ArrayList<Bill> shortBills = new ArrayList<>(bills.subList(offset, Math.min(offset + 5, bills.size())));
                        for (Bill bill : shortBills) {
                            bill.flipDueSoon();
                        }
                        m.put("bills", shortBills);
                        m.put("end", offset >= bills.size() - 5);//checks to see if offset has reached the end of the data list
                        m.put("beginning", offset == 0);//checks to see if at beginnning of data, nex/previous buttons only show when needed
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
                    int index = Integer.valueOf(request.queryParams("id"));//grabs the index of the bill corresponding to the edit link clicked
                    Bill bill = selectBill(conn, index);//through a hidden type input
                    return new ModelAndView(bill, "edit.html");
                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/login",//accepts input form home page and creates a user which is stored in the user table
                ((request, response) -> {
                    String name = request.queryParams("userName");
                    String password = request.queryParams("userPass");
                    User user = selectUser(conn, name);
                    if (user == null) {//checks if the user already exists in the database/adds user to database if not
                        insertUser(conn, name, password);
                    }
                    user = selectUser(conn, name);//grabs user data from database to check password entry
                    Session session = request.session();
                    session.attribute("userName", name);

                    if (user.password.equals(password)){
                        response.redirect("/");
                        return "";
                    }
                    else {
                        Spark.halt("403");
                        return "";
                    }
                })
        );
        Spark.post(//post route to end current user session and return to home.html login page
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
                    User user = getUserFromSession(request.session(), conn);//grabs user from session with user info from database
                    String biller = request.queryParams("biller");
                    LocalDate date = LocalDate.parse(request.queryParams("dueDate"));
                    BigDecimal amount = BigDecimal.valueOf(Double.valueOf(request.queryParams("amountDue")));
                    Bill bill = new Bill(2, biller, date, amount);//the 2 is not saved in database, a random id is assigned to the bill when inserted into the bills table
                    insertBill(conn, bill, user);
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/delete",
                ((request, response) -> {
                    int index = Integer.valueOf(request.queryParams("id"));//grabs from a hidden type input the id of the bill to be deleted
                    deleteBill(conn, index);
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/edit",
                ((request, response) -> {
                    int index = Integer.valueOf(request.queryParams("id"));
                    Bill bill = selectBill(conn, index);//pulls bill data for the bill with the id 'index'
                    String biller = request.queryParams("biller");
                    String amountStr = request.queryParams("amountDue");
                    String dueDate = request.queryParams("dueDate");
                    //3 checks to see if input was given, if no input the value is not changed within the local bill object
                    if (!dueDate.isEmpty()) {
                        bill.dueDate = LocalDate.parse(dueDate);
                    }
                    if (!biller.isEmpty()) {
                        bill.setBiller(biller);
                        String ths = bill.biller;
                    }
                    if (!amountStr.isEmpty()) {
                        bill.setAmount(BigDecimal.valueOf(Double.valueOf(amountStr)));
                    }
                    updateBill(conn, bill);//takes local bill object and rewrites the entire bill in database memory(only those fields that recieved input are changed)
                    response.redirect("/");
                    return "";
                })
        );
    }
    //gets username from session and uses it in method call for the return
    static User getUserFromSession(Session session, Connection conn) throws SQLException {
        String name = session.attribute("userName");
        return selectUser(conn, name);
    }
    //creates tables for bills and users, the bills table gets the user id as a value to connect that user to his/her posts on the database
    static public void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, name VARCHAR, password VARCHAR)");
        stmt.execute("CREATE TABLE IF NOT EXISTS bills (id IDENTITY, user_id INT, biller VARCHAR, due_date VARCHAR, amount DECIMAL)");

    }
    //inserts new users info into the database
    public static void insertUser(Connection conn, String name, String password) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES(NULL, ?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, password);
        stmt.execute();
    }
    //pulls user info from database and returns a user object
    public static User selectUser(Connection conn, String name) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
        stmt.setString(1, name);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            int id = results.getInt("id");
            String password = results.getString("password");
            return new User(id, name, password);
        }
        return null;
    }
    //inserts a new bill objects info into the database saves the user id into the bills table to associate the bill with a particular user
    public static void insertBill(Connection conn, Bill bill, User user) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO bills VALUES(NULL, ?, ?, ?, ?)");
        stmt.setInt(1, user.id);
        stmt.setString(2, bill.biller);
        stmt.setString(3, bill.dueDate.toString());
        stmt.setBigDecimal(4, bill.amount);
        stmt.execute();
    }
    //gets a particular bills data from the database using the bill id and returns a bill object
    public static Bill selectBill(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM bills INNER JOIN users ON bills.user_id = users.id WHERE bills.id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            String biller = results.getString("bills.biller");
            LocalDate dueDate = LocalDate.parse(results.getString("bills.due_date"));
            BigDecimal amount = results.getBigDecimal("bills.amount");
            return new Bill(id, biller, dueDate, amount);
        }
        return null;
    }
    //removes a bill entirely from the database, gets it by the id in the bill object
    static void deleteBill(Connection conn, int id) throws SQLException {
        PreparedStatement stmt2 = conn.prepareStatement("DELETE FROM bills WHERE id = ?");
        stmt2.setInt(1, id);
        stmt2.execute();
    }
    //updates a particular bill in the database
    static void updateBill(Connection conn, Bill bill) throws SQLException {
        PreparedStatement stmt2 = conn.prepareStatement("UPDATE bills SET biller = ?, due_date = ?, amount = ? WHERE id = ?");
        stmt2.setString(1, bill.biller);
        stmt2.setString(2, bill.dueDate.toString());
        stmt2.setBigDecimal(3, bill.amount);
        stmt2.setInt(4, bill.id);
        stmt2.execute();
    }
    //returns an arraylist of all the bills associated with a given user through the users id
    public static ArrayList<Bill> selectBills(Connection conn, int id) throws SQLException {
        ArrayList<Bill> bills = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM bills INNER JOIN users ON bills.user_id = users.id WHERE bills.user_id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            int BillId = results.getInt("id");
            String biller = results.getString("bills.biller");
            LocalDate dueDate = LocalDate.parse(results.getString("bills.due_date"));
            BigDecimal amount = results.getBigDecimal("bills.amount");
            Bill bill = new Bill(BillId, biller, dueDate, amount);
            bills.add(bill);
        }
        return bills;
    }
}
