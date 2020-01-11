package com.example.monument.ui.login;
import java.io.*;
import java.util.*;

public class User {
    //Instance Variables
    private String username;
    private String password;
    private ArrayList<Monument> monuments = new ArrayList<Monument>();
    private int currency = 0;

    //Constructor
    public User(String user, String pass) {
        this.username = user;
        this.password = pass;
    }

    //Methods
    public void addMonument(Monument mon) {
        monuments.add(mon);
        currency += mon.getWorth();
    }
    public void printMonuments() {
        for (int i = 0; i < monuments.size(); i++) {
            System.out.println(monuments.get(i));
        }
    }
    public String getUser() {
        return username;
    }
    public String getPass() {
        return password;
    }
    public int getCurrency() {
        return currency;
    }
}
