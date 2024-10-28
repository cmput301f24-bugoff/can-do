package com.bugoff.can_do;

import java.util.Map;

public interface DatabaseEntity {
    String getId();
    Map<String, Object> toMap();
    void setRemote();
    void attachListener();
    void detachListener();
    void onUpdate();
    void setOnUpdateListener(Runnable listener);
}
