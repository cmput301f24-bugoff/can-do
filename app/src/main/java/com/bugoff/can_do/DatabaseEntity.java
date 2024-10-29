package com.bugoff.can_do;

import java.util.Map;

// Some of the things that are needed for all entities in the database, NOT INCLUSIVE
public interface DatabaseEntity {
    String getId();
    Map<String, Object> toMap();
    void setRemote();
    void attachListener();
    void detachListener();
    void onUpdate();
    void setOnUpdateListener(Runnable listener);
}
