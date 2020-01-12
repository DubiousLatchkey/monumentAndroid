package com.example.monument.ui.login;

import com.example.monument.ui.login.Monument;

import java.util.*;

public class User {
    //Instance Variables
    private String username;
    private String password;
    private ArrayList<Monument> monuments = new ArrayList<Monument>();
    private ArrayList<String> visited;
    private long currency = 0;

    //Constructor
    public User(String user, long currency) {
        this.username = user;
        //this.password = pass;
        this.currency = currency;
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

    public void setVisited(ArrayList<String> visited) {
        this.visited = visited;
    }
    public ArrayList<String> getVisited(){
        return visited;
    }

    public String getUser() {
        return username;
    }
    public String getPass() {
        return password;
    }
    public long getCurrency() {
        return currency;
    }
}
