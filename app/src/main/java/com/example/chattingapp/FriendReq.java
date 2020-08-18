package com.example.chattingapp;

public class FriendReq {

    String request_type;

    public FriendReq(String request_type) {
        this.request_type = request_type;
    }

    public FriendReq() {
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }
}
