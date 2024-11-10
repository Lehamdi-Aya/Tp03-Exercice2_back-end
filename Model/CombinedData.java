package com.example.demo.Model;



import com.example.demo.LPS.LogEntry;

import java.util.List;



public class CombinedData {
    private List<UserProfile> userProfiles;
    private List<LogEntry> logEntries;

    public CombinedData(List<UserProfile> userProfiles, List<LogEntry> logEntries) {
        this.userProfiles = userProfiles;
        this.logEntries = logEntries;
    }

    public List<UserProfile> getUserProfiles() {
        return userProfiles;
    }

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }
}
