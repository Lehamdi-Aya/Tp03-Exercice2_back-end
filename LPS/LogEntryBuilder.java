package com.example.demo.LPS;


import java.text.SimpleDateFormat;
import java.util.Date;

public class LogEntryBuilder {
    private String timestamp;
    private String event;
    private String user;
    private String action;

    public LogEntryBuilder setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public LogEntryBuilder setEvent(String event) {
        this.event = event;
        return this;
    }

    public LogEntryBuilder setUser(String user) {
        this.user = user;
        return this;
    }

    public LogEntryBuilder setAction(String action) {
        this.action = action;
        return this;
    }

    public LogEntry build() {
        return new LogEntry(timestamp, event, user, action);
    }

}
