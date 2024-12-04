package com.bugoff.can_do.testclasses;

import java.util.HashMap;
import java.util.Map;

public class TestEvent {
    private String id;
    private String name;
    private String description;
    private TestFacility facility;

    public TestEvent(String id, TestFacility facility) {
        this.id = id;
        this.facility = facility;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TestFacility getFacility() { return facility; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("description", description);
        map.put("facilityId", facility != null ? facility.getId() : null);
        return map;
    }
}
