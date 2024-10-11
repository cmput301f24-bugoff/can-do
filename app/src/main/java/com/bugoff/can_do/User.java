package com.bugoff.can_do;

public class User {
    private String androidId;
    private String name;
    private Boolean isAdmin;

    public User(String androidId) {
        this.androidId = androidId;
    }

    public User(String androidId, String name, Boolean isAdmin) {
        this.androidId = androidId;
        this.name = name;
        this.isAdmin = isAdmin;
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

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
