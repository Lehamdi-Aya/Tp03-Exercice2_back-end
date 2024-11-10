package com.example.demo;



import com.example.demo.LPS.LogEntry;
import com.example.demo.LPS.LogEntryBuilder;
import com.example.demo.Model.CombinedData;
import com.example.demo.Model.UserProfile;
import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LogManager {
    private List<LogEntry> logEntries = new ArrayList<>();
    private Map<String, UserProfile> userProfiles = new HashMap<>();

    public void logAction(String event, String user, String action) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        LogEntry logEntry = new LogEntryBuilder()
                .setTimestamp(timestamp)
                .setEvent(event)
                .setUser(user)
                .setAction(action)
                .build();
        logEntries.add(logEntry);
    }

    public Map<String, UserProfile> parseLogs() {
        for (LogEntry log : logEntries) {
            String userId = log.getUser();
            userProfiles.putIfAbsent(userId, new UserProfile(userId));
            if (log.getAction().equals("write")) {
                userProfiles.get(userId).incrementWriteOperations();
            } else {
                userProfiles.get(userId).incrementReadOperations();
            }
        }
        return userProfiles;
    }

    public void saveUserProfilesToJson(String filename) {
        Map<String, UserProfile> parsedUserProfiles = parseLogs(); // Parser les logs avant de sauvegarder
        try (FileWriter writer = new FileWriter(filename)) {
            Gson gson = new Gson();
            gson.toJson(parsedUserProfiles.values(), writer);  // Sauvegarder les profils parsés
            System.out.println("User profiles saved to " + filename);

            System.out.println("User Profiles: " + userProfiles.values());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void saveLogsToJson(String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            Gson gson = new Gson();
            gson.toJson(logEntries, writer); // Sauvegarder les logs dans le fichier JSON
            System.out.println("Logs saved to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void saveCombinedDataToJson(String filename, List<UserProfile> userProfiles) {
        CombinedData combinedData = new CombinedData(userProfiles, logEntries);
        try (FileWriter writer = new FileWriter(filename)) {
            Gson gson = new Gson();
            gson.toJson(combinedData, writer); // Sauvegarder les données combinées dans le fichier JSON
            System.out.println("Combined data saved to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
