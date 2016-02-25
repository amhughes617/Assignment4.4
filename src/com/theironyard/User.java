package com.theironyard;

import java.util.ArrayList;

/**
 * Created by alexanderhughes on 2/25/16.
 */
public class User {
    String name, password;
    ArrayList<Bill> bills = new ArrayList<>();

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }
}
