package com.theandroidclassroom.mlkitchatsuggestions;

public class MessagePojo {

    private String msg,mobile,name;
    private long timestamp;

    public String getMsg() {
        return msg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
