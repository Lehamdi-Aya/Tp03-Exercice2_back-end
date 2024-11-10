package com.example.demo.controller;

import com.example.demo.Model.LoginRequest;
import com.example.demo.Model.LoginResponse;
import com.example.demo.Model.User;
import com.example.demo.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Vérifier les credentials
            User user = userService.findByEmail(loginRequest.getEmail());

            if (user != null && user.getPassword().equals(loginRequest.getPassword())) {
                // Créer une réponse de succès
                LoginResponse response = new LoginResponse(
                        user.getId(),
                        user.getEmail(),
                        "Connexion réussie"
                );
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Email ou mot de passe incorrect");
            }
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la connexion");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            // Vérifier si l'email existe déjà
            if (userService.findByEmail(user.getEmail()) != null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Cet email est déjà utilisé");
            }

            // Créer le nouvel utilisateur
            userService.createUser(user);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(user);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'inscription");
        }
    }
}
