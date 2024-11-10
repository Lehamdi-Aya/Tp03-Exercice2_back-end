package com.example.demo.Service;

import com.example.demo.Model.Product;
import com.example.demo.Repository.ProductRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    private final Tracer tracer;

    public ProductService(Tracer tracer) {
        this.tracer = tracer;
    }

    // Afficher tous les produits
    public List<Product> displayProducts() {
        Span span = tracer.spanBuilder("displayProducts").startSpan();
        try (var scope = span.makeCurrent()) {
            span.setAttribute("method", "findAll");
            List<Product> products = productRepository.findAll();
            span.setAttribute("productCount", products.size());
            return products;
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Error occurred while fetching products");
            return Collections.emptyList();
        } finally {
            span.end();
        }
    }

    // Récupérer un produit par ID
    public Product fetchProductById(String id) throws Exception {
        Span span = tracer.spanBuilder("fetchProductById").startSpan();
        try (var scope = span.makeCurrent()) {
            span.setAttribute("method", "findById");
            span.setAttribute("productId", id);
            Optional<Product> product = productRepository.findById(id);
            if (product.isPresent()) {
                span.setAttribute("found", true);
                return product.get();
            } else {
                span.setAttribute("found", false);
                throw new Exception("Product not found with ID: " + id);
            }
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Error occurred while fetching product with ID: " + id);
            throw e;
        } finally {
            span.end();
        }
    }

    // Ajouter un nouveau produit
    public Product addProduct(Product product) throws Exception {
        Span span = tracer.spanBuilder("addProduct").startSpan();
        try (var scope = span.makeCurrent()) {
            span.setAttribute("method", "addProduct");
            span.setAttribute("productId", product.getId());
            if (productRepository.existsById(product.getId())) {
                span.setAttribute("exists", true);
                throw new Exception("Product with ID " + product.getId() + " already exists.");
            } else {
                span.setAttribute("exists", false);
            }
            Product savedProduct = productRepository.save(product);
            span.setAttribute("savedProductId", savedProduct.getId());
            return savedProduct;
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Error occurred while adding product");
            throw e;
        } finally {
            span.end();
        }
    }

    // Supprimer un produit par ID
    public void deleteProduct(String id) throws Exception {
        Span span = tracer.spanBuilder("deleteProduct").startSpan();
        try (var scope = span.makeCurrent()) {
            span.setAttribute("method", "deleteProduct");
            span.setAttribute("productId", id);
            if (!productRepository.existsById(id)) {
                span.setAttribute("exists", false);
                throw new Exception("Product not found with ID: " + id);
            } else {
                span.setAttribute("exists", true);
            }
            productRepository.deleteById(id);
            span.setAttribute("deleted", true);
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Error occurred while deleting product");
            throw e;
        } finally {
            span.end();
        }
    }

    // Mettre à jour les informations d'un produit
    public Product updateProduct(String id, Product product) throws Exception {
        Span span = tracer.spanBuilder("updateProduct").startSpan();
        try (var scope = span.makeCurrent()) {
            span.setAttribute("method", "updateProduct");
            span.setAttribute("productId", id);
            if (!productRepository.existsById(id)) {
                span.setAttribute("exists", false);
                throw new Exception("Product not found with ID: " + id);
            } else {
                span.setAttribute("exists", true);
            }
            product.setId(id);
            span.setAttribute("updateAttempt", "attempting to update product");
            Product updatedProduct = productRepository.save(product);
            span.setAttribute("updateSuccess", true);
            return updatedProduct;
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Error occurred while updating product");
            throw e;
        } finally {
            span.end();
        }
    }
}