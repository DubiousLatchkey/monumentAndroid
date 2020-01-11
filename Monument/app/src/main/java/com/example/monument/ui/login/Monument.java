package com.example.monument.ui.login;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;

import java.io.*;
import java.util.*;

public class Monument {
    //Instance Variables
    private String name;
    private FirebaseVisionCloudLandmark landmark;
    private int worth;

    //Constructor
    public Monument(String n, FirebaseVisionCloudLandmark l, int worth) {
        this.name = n;
        landmark = l;
        this.worth = worth;
    }

    //Methods
    public String getName() {
        return name;
    }
    public FirebaseVisionCloudLandmark getLandmark() {
        return landmark;
    }

    public int getWorth() {
        return worth;
    }
}
