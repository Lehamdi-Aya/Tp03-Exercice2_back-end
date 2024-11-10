package com.example.demo;


import com.example.demo.Model.Product;
import com.example.demo.Model.User;
import com.example.demo.Model.UserProfile;
import com.example.demo.Repository.ProductRepository;
import com.example.demo.Service.ProductService;
import com.example.demo.Repository.UserRepository;
import com.google.gson.Gson;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Scope;
import java.util.*;

@Component
public class ApplicationRunner implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationRunner.class);
    private final LogManager logManager = new LogManager();
    private final Map<String, UserProfile> userProfiles = new HashMap<>();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private Tracer tracer;

    ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        Span rootSpan = tracer.spanBuilder("application-execution")
                .startSpan();
        try (Scope scope = rootSpan.makeCurrent()) {
            Scanner scanner = new Scanner(System.in);

            Span createUsersSpan = tracer.spanBuilder("create-users")
                    .startSpan();
            try (Scope createUsersScope = createUsersSpan.makeCurrent()) {
                createUsers();
            } finally {
                createUsersSpan.end();
            }

            Span scenariosSpan = tracer.spanBuilder("execute-scenarios")
                    .startSpan();
            try (Scope scenariosScope = scenariosSpan.makeCurrent()) {
                executeUserScenarios(userRepository, productRepository);
                logManager.saveCombinedDataToJson("target/SequenceExcution.json", new ArrayList<>(userProfiles.values()));
            } finally {
                scenariosSpan.end();
            }

            Span interactionSpan = tracer.spanBuilder("user-interaction")
                    .startSpan();
            try (Scope interactionScope = interactionSpan.makeCurrent()) {
                handleUserInteraction(scanner);
            } finally {
                interactionSpan.end();
            }
        } finally {
            rootSpan.end();
        }
    }


    private void createUsers() {
        for (int i = 1; i <= 10; i++) {
            Span userSpan = tracer.spanBuilder("create-user-" + i)
                    .startSpan();
            try (Scope scope = userSpan.makeCurrent()) {
                String userId = String.valueOf(i);
                String name = generateRandomName(); // Utilise la fonction pour un nom aléatoire
                int age = 18 + (int)(Math.random() * 50);
                String email = "user" + i + "@example.com";
                String password = "password" + i;

                User newUser = new User(userId, name, age, email, password);
                userRepository.save(newUser);
                logger.info("Created user: {}", newUser);

                userSpan.setAttribute("user.id", userId);
                userSpan.setAttribute("user.email", email);
            } finally {
                userSpan.end();
            }
        }
    }

    // Méthode auxiliaire pour générer un nom de 3 ou 4 lettres
    private String generateRandomName() {
        Random random = new Random();
        int nameLength = random.nextBoolean() ? 3 : 4; // Nom de 3 ou 4 lettres
        StringBuilder name = new StringBuilder(nameLength);

        for (int i = 0; i < nameLength; i++) {
            char randomChar = (char) ('A' + random.nextInt(26)); // Génère une lettre majuscule entre A et Z
            name.append(randomChar);
        }

        return name.toString();
    }




    private void executeUserScenarios(UserRepository userRepository, ProductRepository productRepository) throws Exception {
        for (int userId = 1; userId <= 10; userId++) {
            Span userScenarioSpan = tracer.spanBuilder("user-scenario-" + userId)
                    .startSpan();
            try (Scope scope = userScenarioSpan.makeCurrent()) {
                User loggedInUser = userRepository.findUserById(String.valueOf(userId));
                if (loggedInUser == null) {
                    userScenarioSpan.setAttribute("error", "User not found");
                    System.out.println("User " + userId + " not found, skipping.");
                    continue;
                }

                userScenarioSpan.setAttribute("user.id", String.valueOf(userId));
                List<String> userProducts = new ArrayList<>();

                // Création de produits pour chaque utilisateur avec un nom aléatoire
                for (int j = 0; j < 5; j++) {
                    Span createProductSpan = tracer.spanBuilder("create-product")
                            .startSpan();
                    try (Scope productScope = createProductSpan.makeCurrent()) {
                        String baseProductId = "user" + userId + "_product" + (j + 1);
                        String productId = baseProductId;
                        int suffix = 1;

                        while (true) {
                            try {
                                String randomProductName = generateRandomProductName(); // Génère un nom de produit aléatoire
                                Product newProduct = new Product(productId, randomProductName, 10.0 + j, new Date());
                                productService.addProduct(newProduct);
                                userProducts.add(productId);
                                logManager.logAction("Add Product", loggedInUser.getId(), "write");
                                System.out.println("Product " + productId + " added successfully with name: " + randomProductName);

                                createProductSpan.setAttribute("product.id", productId);
                                createProductSpan.setAttribute("product.name", newProduct.getName());
                                break;
                            } catch (Exception e) {
                                if (e.getMessage().contains("already exists")) {
                                    productId = baseProductId + "_" + suffix++;
                                } else {
                                    createProductSpan.recordException(e);
                                    throw e;
                                }
                            }
                        }
                    } finally {
                        createProductSpan.end();
                    }
                }

                // Exécution d'actions aléatoires sur les produits
                for (int i = 0; i < 20; i++) {
                    if (userProducts.isEmpty()) {
                        System.out.println("No products available for user " + userId);
                        break;
                    }

                    int action = (int) (Math.random() * 4);
                    String productId = userProducts.get((int) (Math.random() * userProducts.size()));

                    Span actionSpan = tracer.spanBuilder("product-action")
                            .startSpan();
                    try (Scope actionScope = actionSpan.makeCurrent()) {
                        actionSpan.setAttribute("action.type", getActionName(action));
                        actionSpan.setAttribute("product.id", productId);

                        switch (action) {
                            case 0: // Add product case is handled in the previous loop
                                break;

                            case 1: // Fetch a product
                                Product fetchedProduct = productService.fetchProductById(productId);
                                if (fetchedProduct != null) {
                                    logManager.logAction("Fetch Product", loggedInUser.getId(), "read");
                                    System.out.println("Product " + productId + " fetched successfully.");
                                }
                                break;

                            case 2: // Update a product (keep the original name)
                                Product originalProduct = productService.fetchProductById(productId);
                                if (originalProduct != null) {
                                    Product updatedProduct = new Product(productId, originalProduct.getName(), 20.0 + i, new Date());
                                    productService.updateProduct(productId, updatedProduct);
                                    logManager.logAction("Update Product", loggedInUser.getId(), "write");
                                    System.out.println("Product " + productId + " updated successfully with original name: " + originalProduct.getName());
                                }
                                break;

                            case 3: // Delete a product
                                productService.deleteProduct(productId);
                                userProducts.remove(productId);
                                logManager.logAction("Delete Product", loggedInUser.getId(), "write");
                                System.out.println("Product " + productId + " deleted successfully.");
                                break;
                        }
                    } catch (Exception e) {
                        actionSpan.recordException(e);
                        System.err.println("Error during action " + action + " for product " + productId + ": " + e.getMessage());
                    } finally {
                        actionSpan.end();
                    }
                }

            } finally {
                userScenarioSpan.end();
            }
        }
    }


    // Méthode pour générer un nom aléatoire pour les produits
    private String generateRandomProductName() {
        Random random = new Random();
        int nameLength = random.nextBoolean() ? 3 : 4; // Longueur de nom de 3 ou 4 lettres
        StringBuilder name = new StringBuilder(nameLength);

        for (int i = 0; i < nameLength; i++) {
            char randomChar = (char) ('A' + random.nextInt(26)); // Génère une lettre majuscule entre A et Z
            name.append(randomChar);
        }

        return name.toString();
    }


    private void handleUserInteraction(Scanner scanner) {
        Span interactionSpan = tracer.spanBuilder("handle-user-interaction")
                .startSpan();
        try (Scope scope = interactionSpan.makeCurrent()) {
            System.out.println("1. Create a new user");
            System.out.println("2. Login");
            int option = scanner.nextInt();
            scanner.nextLine();

            User loggedInUser = null;

            if (option == 1) {
                Span createUserSpan = tracer.spanBuilder("create-new-user")
                        .startSpan();
                try (Scope createUserScope = createUserSpan.makeCurrent()) {
                    System.out.print("ID: ");
                    String userId = scanner.nextLine();
                    System.out.print("Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Age: ");
                    int age = Integer.parseInt(scanner.nextLine());
                    System.out.print("Email: ");
                    String email = scanner.nextLine();
                    System.out.print("Password: ");
                    String password = scanner.nextLine();

                    User newUser = new User(userId, name, age, email, password);
                    userRepository.save(newUser);
                    logger.info("User created: {}", newUser);
                    loggedInUser = newUser;
                    logManager.logAction("User Creation", newUser.getId(), "write");

                    createUserSpan.setAttribute("user.id", userId);
                    createUserSpan.setAttribute("user.email", email);
                } finally {
                    createUserSpan.end();
                }
            } else if (option == 2) {
                Span loginSpan = tracer.spanBuilder("user-login")
                        .startSpan();
                try (Scope loginScope = loginSpan.makeCurrent()) {
                    System.out.print("Email: ");
                    String loginEmail = scanner.nextLine();
                    System.out.print("Password: ");
                    String loginPassword = scanner.nextLine();

                    loggedInUser = userRepository.getUserByEmailAndPassword(loginEmail, loginPassword);

                    if (loggedInUser != null) {
                        logger.info("User logged in: {}", loggedInUser);
                        logManager.logAction("User Login", loggedInUser.getId(), "read");
                        System.out.println("Login successful! Welcome, " + loggedInUser.getName());

                        loginSpan.setAttribute("user.id", loggedInUser.getId());
                        loginSpan.setAttribute("user.email", loginEmail);
                    } else {
                        logger.warn("Failed login attempt for email: {}", loginEmail);
                        System.out.println("Login failed. Check your email and password.");
                        loginSpan.setAttribute("error", "Login failed");
                        return;
                    }
                } finally {
                    loginSpan.end();
                }
            } else {
                System.out.println("Invalid choice. Please restart the program.");
                return;
            }

            boolean exit = false;
            while (!exit && loggedInUser != null) {
                System.out.println("\nOptions:");
                System.out.println("1. Add Product");
                System.out.println("2. Fetch Product by ID");
                System.out.println("3. Update Product");
                System.out.println("4. Delete Product");
                System.out.println("5. Display All Products");
                System.out.println("6. Exit");
                System.out.print("Choose an option: ");

                int choice = scanner.nextInt();
                scanner.nextLine();

                Span operationSpan = tracer.spanBuilder("product-operation")
                        .startSpan();
                try (Scope operationScope = operationSpan.makeCurrent()) {
                    operationSpan.setAttribute("operation.type", String.valueOf(choice));
                    operationSpan.setAttribute("user.id", loggedInUser.getId());

                    switch (choice) {
                        case 1: // Add Product
                            System.out.print("Enter product ID: ");
                            String id = scanner.nextLine();
                            System.out.print("Enter product name: ");
                            String name = scanner.nextLine();
                            System.out.print("Enter product price: ");
                            double price = scanner.nextDouble();
                            scanner.nextLine();
                            System.out.print("Enter expiration date (yyyy-mm-dd): ");
                            Date expirationDate = java.sql.Date.valueOf(scanner.nextLine());

                            Product product = new Product(id, name, price, expirationDate);
                            productService.addProduct(product);
                            logger.info("User {} added product {}", loggedInUser.getId(), product);
                            operationSpan.setAttribute("product.id", id);
                            break;

                        case 2: // Fetch Product
                            System.out.print("Enter product ID to fetch: ");
                            String fetchId = scanner.nextLine();
                            Product fetchedProduct = productService.fetchProductById(fetchId);
                            if (fetchedProduct != null) {
                                logger.info("User {} fetched product {}", loggedInUser.getId(), fetchedProduct);
                                operationSpan.setAttribute("product.id", fetchId);
                            } else {
                                System.out.println("Product not found.");
                                operationSpan.setAttribute("error", "Product not found");
                            }
                            break;

                        case 3: // Update Product
                            System.out.print("Enter product ID to update: ");
                            String updateId = scanner.nextLine();
                            Product existingProduct = productService.fetchProductById(updateId);
                            if (existingProduct != null) {
                                System.out.print("Enter new product name (or press Enter to keep current): ");
                                String newName = scanner.nextLine();
                                System.out.print("Enter new product price (or press Enter to keep current): ");
                                String priceInput = scanner.nextLine();
                                System.out.print("Enter new expiration date (yyyy-mm-dd, or press Enter to keep current): ");
                                String dateInput = scanner.nextLine();

                                if (!newName.isEmpty()) existingProduct.setName(newName);
                                if (!priceInput.isEmpty()) existingProduct.setPrice(Double.parseDouble(priceInput));
                                if (!dateInput.isEmpty())
                                    existingProduct.setExpirationDate(java.sql.Date.valueOf(dateInput));

                                productService.updateProduct(updateId, existingProduct);
                                logger.info("User {} updated product {}", loggedInUser.getId(), existingProduct);
                                operationSpan.setAttribute("product.id", updateId);
                            } else {
                                System.out.println("Product not found.");
                                operationSpan.setAttribute("error", "Product not found");
                            }
                            break;

                        case 4: // Delete Product
                            System.out.print("Enter product ID to delete: ");
                            String deleteId = scanner.nextLine();
                            productService.deleteProduct(deleteId);
                            logger.info("User {} deleted product with ID {}", loggedInUser.getId(), deleteId);
                            operationSpan.setAttribute("product.id", deleteId);
                            break;

                        case 5: // Display All Products
                            List<Product> allProducts = productService.displayProducts();
                            System.out.println("All Products:");
                            for (Product p : allProducts) {
                                System.out.println(p);
                            }
                            break;

                        case 6:
                            exit = true;
                            System.out.println("Exiting...");
                            Gson gson = new Gson();

                            // logManager.saveUserProfilesToJson("target/userProfilesS.json");
                            // logManager.saveLogsToJson("target/logs.json"); // Sauvegarder les logs
                            logManager.saveCombinedDataToJson("target/userProfiles.json", new ArrayList<>(userProfiles.values()));

                            System.out.println("User profiles written successfully.");

                            exit = true;
                            break;

                        default:
                            System.out.println("Invalid choice. Please try again.");
                    }
                } catch (Exception e) {
                    System.err.println("Error: " + e.getMessage());
                }
            }

            scanner.close(); // Close the scanner when done
        }
    }



    private void updateUserProfile(String userId, boolean isWriteOperation) {
        userRepository.findById(userId).ifPresent(user -> {
            userProfiles.putIfAbsent(userId,
                    new UserProfile(userId, user.getName(), user.getEmail(), user.getAge()));

            if (isWriteOperation) {
                userProfiles.get(userId).incrementWriteOperations();
                logManager.logAction("Update Profile", userId, "write");
            } else {
                userProfiles.get(userId).incrementReadOperations();
                logManager.logAction("Read Profile", userId, "read");
            }
        });
    }
    private String getActionName(int action) {
        switch (action) {
            case 0: return "add_product";
            case 1: return "fetch_product";
            case 2: return "update_product";
            case 3: return "delete_product";
            default: return "unknown_action";
        }
    }

}
