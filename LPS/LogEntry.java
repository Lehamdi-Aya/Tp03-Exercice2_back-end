package com.example.demo.LPS;


public class LogEntry {
    private String timestamp;
    private String event;
    private String user;
    private String action;
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public String getEvent() {
        return event;
    }
    public void setEvent(String event) {
        this.event = event;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public LogEntry(String timestamp, String event, String user, String action) {
        super();
        this.timestamp = timestamp;
        this.event = event;
        this.user = user;
        this.action = action;
    }


}
