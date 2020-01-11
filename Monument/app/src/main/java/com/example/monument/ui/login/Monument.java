package com.example.monument.ui.login;
import java.io.*;
import java.util.*;

public class Monument {
    //Instance Variables
    private String name;
    private int x;
    private int y;
    private int worth;

    //Constructor
    public Monument(String n, int x, int y, int worth) {
        this.name = n;
        this.x = x;
        this.y = y;
        this.worth = worth;
    }

    //Methods
    public String getName() {
        return name;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getWorth() {
        return worth;
    }
}
