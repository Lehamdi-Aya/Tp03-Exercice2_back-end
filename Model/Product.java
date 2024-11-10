package com.example.demo.Model;



import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "products") // Spécifie la collection MongoDB, ici 'products'
public class Product {

    @Id  // Indique que cet attribut est l'identifiant unique dans MongoDB
    private String id;
    private String name;
    private double price;
    private Date expirationDate;

    // Constructeur par défaut nécessaire pour Spring et MongoDB
    public Product() {}

    // Constructeur avec paramètres
        public Product(String id, String name, double price, Date expirationDate) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.expirationDate = expirationDate;
    }



    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    // Pour afficher une représentation de l'objet utile dans les logs
    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", expirationDate=" + expirationDate +
                '}';
    }
}
