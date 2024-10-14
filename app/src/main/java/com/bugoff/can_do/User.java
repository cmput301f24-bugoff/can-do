package com.bugoff.can_do;

public class User {
    private String androidId;
    private String name;
    private Boolean isAdmin;
    private Facility facility;

    public User(String androidId) {
        this.androidId = androidId;
        this.name = null;
        this.isAdmin = false;
        this.facility = null;
    }

    public User(String androidId, String name, Boolean isAdmin, Facility facility) {
        this.androidId = androidId;
        this.name = name;
        this.isAdmin = isAdmin;
        this.facility = facility;
    }

    public String getAndroidId() {
        return androidId;
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

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }
}
