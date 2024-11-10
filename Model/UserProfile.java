package com.example.demo.Model;

import java.util.concurrent.atomic.AtomicInteger;

public class UserProfile {
    private String userId;
    private String name;
    private String email;
    private int age;
    private AtomicInteger readOperations;
    private AtomicInteger writeOperations;
    private AtomicInteger expensiveProductSearches;

    public UserProfile(String userId, String name, String email, int age) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.age = age;
        this.readOperations = new AtomicInteger(0);
        this.writeOperations = new AtomicInteger(0);
        this.expensiveProductSearches = new AtomicInteger(0);
    }

    public UserProfile(String userId) {
        this.userId = userId;
        this.readOperations = new AtomicInteger(0);
        this.writeOperations = new AtomicInteger(0);
    }
    // Ajoute des méthodes pour incrémenter les opérations de lecture et d'écriture
    public void incrementReadOperations() {
        this.readOperations.incrementAndGet();
    }

    public void incrementWriteOperations() {
        this.writeOperations.incrementAndGet();
    }

    // Getters pour les informations de profil
    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getAge() {
        return age;
    }

    public int getReadOperations() {
        return readOperations.get();
    }

    public int getWriteOperations() {
        return writeOperations.get();
    }

    public int getExpensiveProductSearches() {
        return expensiveProductSearches.get();
    }
}
