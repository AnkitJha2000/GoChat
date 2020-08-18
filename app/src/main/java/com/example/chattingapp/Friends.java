package com.example.chattingapp;

public class Friends {

    String date,FriendID;

    public Friends() {
        // empty constructor //////////////////
    }

    public Friends(String date, String friendID) {
        this.date = date;
        FriendID = friendID;
    }

    public String getFriendID() {
        return FriendID;
    }

    public void setFriendID(String friendID) {
        FriendID = friendID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
