package com.example.demo.controller;

import com.example.demo.Model.Product;
import com.example.demo.Service.ProductService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    private final Tracer tracer;
    private final OpenTelemetry openTelemetry;

    public ProductController(Tracer tracer, OpenTelemetry openTelemetry, ProductService productService) {
        this.tracer = tracer;
        this.openTelemetry = openTelemetry;
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(@RequestHeader Map<String, String> headers) {
        // Extraire le contexte à partir des en-têtes HTTP
        Context extractedContext = openTelemetry.getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), headers, new TextMapGetter<>() {
                    @Override
                    public Iterable<String> keys(Map<String, String> carrier) {
                        return carrier.keySet();
                    }

                    @Override
                    public String get(Map<String, String> carrier, String key) {
                        return carrier.get(key);
                    }
                });

        Span span = tracer.spanBuilder("getAllProducts")
                .setParent(extractedContext)
                .startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("http.method", "GET");
            span.setAttribute("http.url", "/api/products");

            // Capture des en-têtes
            headers.forEach((key, value) -> span.setAttribute("http.header." + key, value));

            List<Product> products = productService.displayProducts();
            span.setAttribute("products.count", products.size());
            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Error occurred while fetching products");
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            span.end();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") String id, @RequestHeader Map<String, String> headers) {
        Span span = tracer.spanBuilder("getProductById")
                .startSpan();
        try (Scope scope = span.makeCurrent()) {
            // Capture des en-têtes
            headers.forEach((key, value) -> span.setAttribute("http.header." + key, value));

            span.setAttribute("http.method", "GET");
            span.setAttribute("http.url", "/api/products/{id}");
            span.setAttribute("product.id", id);

            Product product = productService.fetchProductById(id);
            span.setStatus(StatusCode.OK);
            return new ResponseEntity<>(product, HttpStatus.OK);
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Error fetching product by ID");
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } finally {
            span.end();
        }
    }

    @PostMapping("/add")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Span span = tracer.spanBuilder("createProduct")
                .startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("http.method", "POST");
            span.setAttribute("http.url", "/api/products");

            Product createdProduct = productService.addProduct(product);
            span.setStatus(StatusCode.OK);
            return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Error creating product");
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } finally {
            span.end();
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable("id") String id, @RequestBody Product product) {
        Span span = tracer.spanBuilder("updateProduct")
                .startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("http.method", "PUT");
            span.setAttribute("http.url", "/api/products/update/{id}");
            span.setAttribute("product.id", id);

            Product updatedProduct = productService.updateProduct(id, product);
            span.setStatus(StatusCode.OK);
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Error updating product");
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } finally {
            span.end();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<HttpStatus> deleteProduct(@PathVariable("id") String id) {
        Span span = tracer.spanBuilder("deleteProduct")
                .startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("http.method", "DELETE");
            span.setAttribute("http.url", "/api/products/delete/{id}");
            span.setAttribute("product.id", id);

            productService.deleteProduct(id);
            span.setStatus(StatusCode.OK);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            span.recordException(e);
            span.setStatus(StatusCode.ERROR, "Error deleting product");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } finally {
            span.end();
        }
    }
}