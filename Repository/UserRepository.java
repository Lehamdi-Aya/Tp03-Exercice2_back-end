package com.example.demo.Repository;

import com.example.demo.Model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

    // Custom method to find a user by ID
    User findUserById(String id);
    User findByEmail(String email);
    User getUserByEmailAndPassword(String loginEmail, String loginPassword);
}