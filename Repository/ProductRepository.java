package com.example.demo.Repository;



import com.example.demo.Model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface ProductRepository extends MongoRepository<Product, String> {
    // Define your query methods here
}
