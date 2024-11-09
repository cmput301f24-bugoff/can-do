package com.bugoff.can_do.database;

import java.util.Map;

/**
 * {@code DatabaseEntity} is an interface for Firestore database entities.
 */
public interface DatabaseEntity {
    /**
     * Returns the ID of the entity.
     *
     * @return The ID of the entity.
     */
    String getId();
    /**
     * Returns a map representation of the entity.
     *
     * @return A map representation of the entity.
     */
    Map<String, Object> toMap();
    /**
     * Sets the entity to remote.
     */
    void setRemote();
    /**
     * Attaches a listener to the entity.
     */
    void attachListener();
    /**
     * Detaches the listener from the entity.
     */
    void detachListener();
    /**
     * Reacts to when the entity is updated.
     */
    void onUpdate();
    /**
     * Sets the update listener for the entity.
     *
     * @param listener The listener to set.
     */
    void setOnUpdateListener(Runnable listener);
}
