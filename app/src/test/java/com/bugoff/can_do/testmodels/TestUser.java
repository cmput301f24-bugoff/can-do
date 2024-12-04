package com.bugoff.can_do.testmodels;

import java.util.HashMap;
import java.util.Map;

public class TestUser {
    private String id;
    private String name;
    private String email;

    public TestUser(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("email", email);
        return map;
    }
}