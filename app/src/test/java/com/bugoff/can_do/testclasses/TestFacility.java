package com.bugoff.can_do.testclasses;

import java.util.HashMap;
import java.util.Map;

public class TestFacility {
    private String id;
    private String name;
    private String address;
    private TestUser owner;

    public TestFacility(String id, TestUser owner) {
        this.id = id;
        this.owner = owner;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public TestUser getOwner() { return owner; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("address", address);
        map.put("ownerId", owner != null ? owner.getId() : null);
        return map;
    }
}
