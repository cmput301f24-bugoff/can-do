package com.bugoff.can_do;

public class User {
    private String androidId;
    private String name;

    public User(String androidId) {
        this.androidId = androidId;
    }

    public User(String androidId, String name) {
        this.androidId = androidId;
        this.name = name;
    }

    public String getAndroidId() {
        return androidId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
