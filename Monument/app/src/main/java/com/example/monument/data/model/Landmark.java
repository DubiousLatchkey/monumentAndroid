package com.example.monument.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Landmark implements Parcelable {
    private String name;
    private String landmark_id;
    private String avatar;

    public Landmark(String name, String landmark_id, String avatar) {
        this.name = name;
        this.landmark_id = landmark_id;
        this.avatar = avatar;
    }

    protected Landmark(Parcel in){
        name = in.readString();
        landmark_id = in.readString();
        avatar = in.readString();
    }

    public static final Creator<Landmark> CREATOR = new Creator<Landmark>() {
        @Override
        public Landmark createFromParcel(Parcel in) {
            return new Landmark(in);
        }

        @Override
        public Landmark[] newArray(int size) {
            return new Landmark[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLandmark_id() {
        return landmark_id;
    }

    public void setLandmark_id(String landmark_id) {
        this.landmark_id = landmark_id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public static Creator<Landmark> getCREATOR() {
        return CREATOR;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(landmark_id);
        dest.writeString(avatar);
    }
}
