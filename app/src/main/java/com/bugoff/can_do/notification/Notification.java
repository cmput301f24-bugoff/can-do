package com.bugoff.can_do.notification;

public class Notification {
    private String id;
    private String type;
    private String message;
    private String from;
    private String to;
    private String event;

    public Notification() {
    }

    public Notification(String id, String type, String message, String from, String to, String event) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.from = from;
        this.to = to;
        this.event = event;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return message;
    }

    public void setContent(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Object getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}