# Inventory Management REST API

A Spring Boot 3 RESTful API for inventory management using Java 17 and Maven. The project uses a thread-safe in-memory data store with sample product catalog data, input validation, structured error responses, and unit/integration tests.

---

## Features

- **In-Memory Store**: Thread-safe storage powered by `ConcurrentHashMap` and `AtomicLong` (no database required).
- **Pre-populated Catalog**: Initialized with realistic sample inventory items upon startup.
- **Full CRUD API**:
  - Add product (`POST /api/products`) with validation
  - View all products (`GET /api/products`)
  - Search by product ID (`GET /api/products/{id}`)
  - Update product (`PUT /api/products/{id}`)
  - Delete product (`DELETE /api/products/{id}`)
  - Product count (`GET /api/products/count`)
- **Low-Stock Query**: Filter products below a configurable stock threshold (`GET /api/products/low-stock?threshold=10`).
- **Health Check**: Service status and inventory metrics (`GET /api/health`).
- **Input Validation**: Jakarta Bean Validation (`@NotBlank`, `@Positive`, `@Min`, `@Size`) with standardized HTTP 400 JSON error details.
- **Automated Tests**: JUnit 5 and MockMvc test coverage for controllers, services, and context loading.

---

## Prerequisites & System Verification

### Environment Status Check
During the project setup, the environment was checked for Java 17 and Maven:
- `java -version`: Not found in the system PATH.
- `mvn -version`: Not found in the system PATH.

### Installing Prerequisites on Windows

#### Option 1: Using Windows Package Manager (`winget`) (Recommended)
Open PowerShell as Administrator and run:
```powershell
# Install Eclipse Temurin OpenJDK 17
winget install EclipseAdoptium.Temurin.17.JDK

# Install Apache Maven
winget install Apache.Maven
```
After installation, close and reopen your PowerShell or Command Prompt terminal.

#### Option 2: Manual Installation
1. **Java 17 JDK**:
   - Download the Windows x64 MSI installer from [Eclipse Adoptium (Temurin 17)](https://adoptium.net/temurin/releases/?version=17).
   - Run the installer and ensure the option **"Set JAVA_HOME variable"** and **"Add to PATH"** are selected.
2. **Apache Maven**:
   - Download the binary zip from [Maven Official Site](https://maven.apache.org/download.cgi).
   - Extract to `C:\Program Files\apache-maven-3.9.x`.
   - Add `C:\Program Files\apache-maven-3.9.x\bin` to your system `PATH` environment variable.

#### Verification
Confirm the installations by running:
```powershell
java -version
mvn -version
```

---

## Project Structure

```
Inventory-Management-System
├── pom.xml
├── README.md
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── inventory
    │   │           ├── InventoryManagementApplication.java
    │   │           ├── controller
    │   │           │   ├── HealthController.java
    │   │           │   └── ProductController.java
    │   │           ├── dto
    │   │           │   ├── ApiResponse.java
    │   │           │   ├── HealthResponse.java
    │   │           │   └── ProductRequest.java
    │   │           ├── exception
    │   │           │   ├── ErrorResponse.java
    │   │           │   ├── GlobalExceptionHandler.java
    │   │           │   └── ResourceNotFoundException.java
    │   │           ├── model
    │   │           │   └── Product.java
    │   │           ├── repository
    │   │           │   ├── InMemoryProductRepository.java
    │   │           │   └── ProductRepository.java
    │   │           └── service
    │   │               └── ProductService.java
    │   └── resources
    │       └── application.properties
    └── test
        ├── java
        │   └── com
        │       └── inventory
        │           ├── InventoryManagementApplicationTests.java
        │           ├── controller
        │           │   ├── HealthControllerTest.java
        │           │   └── ProductControllerTest.java
        │           └── service
        │               └── ProductServiceTest.java
        └── resources
            └── application-test.properties
```

---

## Build & Execution Instructions (Windows)

Navigate to the project root directory in PowerShell or Command Prompt:

```powershell
cd c:\Users\LENOVO\OneDrive\Desktop\Inventory-Management-System
```

### 1. Run Unit & Integration Tests
Execute all JUnit 5 and MockMvc tests:
```powershell
mvn test
```

### 2. Run the Application Directly
Start the Spring Boot application using the Maven plugin:
```powershell
mvn spring-boot:run
```
The application will start on `http://localhost:8080`.

### 3. Package and Run as Executable JAR
```powershell
# Build executable JAR
mvn clean package

# Run the packaged JAR
java -jar target/inventory-management-system-1.0.0.jar
```

---

## REST API Reference

Default Base URL: `http://localhost:8080`

### 1. Health Check
- **Endpoint**: `GET /api/health`
- **Description**: Returns application health status, service name, version, and current item count.
- **PowerShell**:
  ```powershell
  Invoke-RestMethod -Uri "http://localhost:8080/api/health" -Method Get
  ```
- **cURL**:
  ```bash
  curl -X GET http://localhost:8080/api/health
  ```
- **Response `200 OK`**:
  ```json
  {
    "status": "UP",
    "service": "Inventory Management REST API",
    "timestamp": "2026-11-15T19:30:00",
    "totalProducts": 8,
    "version": "1.0.0"
  }
  ```

---

### 2. View All Products
- **Endpoint**: `GET /api/products`
- **Description**: Retrieves all products sorted by ID.
- **PowerShell**:
  ```powershell
  Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Get
  ```
- **cURL**:
  ```bash
  curl -X GET http://localhost:8080/api/products
  ```
- **Response `200 OK`**:
  ```json
  [
    {
      "id": 1,
      "name": "Standard Business Laptop",
      "description": "15.6 inch Quad-Core, 16GB RAM, 512GB SSD",
      "price": 899.99,
      "quantity": 15,
      "category": "Electronics",
      "createdAt": "2026-11-15T19:30:00",
      "updatedAt": "2026-11-15T19:30:00"
    }
  ]
  ```

---

### 3. Search Product by ID
- **Endpoint**: `GET /api/products/{id}`
- **Description**: Looks up a single product by its unique numeric ID.
- **PowerShell**:
  ```powershell
  Invoke-RestMethod -Uri "http://localhost:8080/api/products/1" -Method Get
  ```
- **cURL**:
  ```bash
  curl -X GET http://localhost:8080/api/products/1
  ```
- **Response `200 OK`**:
  ```json
  {
    "id": 1,
    "name": "Standard Business Laptop",
    "description": "15.6 inch Quad-Core, 16GB RAM, 512GB SSD",
    "price": 899.99,
    "quantity": 15,
    "category": "Electronics",
    "createdAt": "2026-11-15T19:30:00",
    "updatedAt": "2026-11-15T19:30:00"
  }
  ```
- **Response `404 Not Found`** (when ID does not exist):
  ```json
  {
    "timestamp": "2026-11-15T19:30:00",
    "status": 404,
    "error": "Not Found",
    "message": "Product not found with id: '999'",
    "path": "/api/products/999"
  }
  ```

---

### 4. Low-Stock Listing
- **Endpoint**: `GET /api/products/low-stock`
- **Optional Query Parameter**: `threshold` (default is `10` if omitted)
- **Description**: Returns all items where `quantity <= threshold`.
- **PowerShell**:
  ```powershell
  # Default threshold (10)
  Invoke-RestMethod -Uri "http://localhost:8080/api/products/low-stock" -Method Get

  # Custom threshold (5)
  Invoke-RestMethod -Uri "http://localhost:8080/api/products/low-stock?threshold=5" -Method Get
  ```
- **cURL**:
  ```bash
  curl -X GET "http://localhost:8080/api/products/low-stock?threshold=5"
  ```
- **Response `200 OK`**:
  ```json
  [
    {
      "id": 6,
      "name": "Ergonomic Desk Chair",
      "description": "Adjustable swivel chair with lumbar support",
      "price": 189.00,
      "quantity": 3,
      "category": "Furniture"
    },
    {
      "id": 3,
      "name": "Mechanical Gaming Keyboard",
      "description": "Compact mechanical keyboard with backlight",
      "price": 69.99,
      "quantity": 4,
      "category": "Electronics"
    },
    {
      "id": 4,
      "name": "Ultra HD Monitor 27-inch",
      "description": "IPS panel with HDMI and DisplayPort input",
      "price": 279.50,
      "quantity": 5,
      "category": "Electronics"
    }
  ]
  ```

---

### 5. Product Count
- **Endpoint**: `GET /api/products/count`
- **Description**: Returns the total number of products currently in inventory.
- **PowerShell**:
  ```powershell
  Invoke-RestMethod -Uri "http://localhost:8080/api/products/count" -Method Get
  ```
- **cURL**:
  ```bash
  curl -X GET http://localhost:8080/api/products/count
  ```
- **Response `200 OK`**:
  ```
  8
  ```

---

### 6. Add New Product
- **Endpoint**: `POST /api/products`
- **Header**: `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "name": "Noise Cancelling Earbuds",
    "description": "Wireless earbuds with active noise cancellation",
    "price": 79.99,
    "quantity": 40,
    "category": "Audio"
  }
  ```
- **PowerShell**:
  ```powershell
  $body = @{
      name = "Noise Cancelling Earbuds"
      description = "Wireless earbuds with active noise cancellation"
      price = 79.99
      quantity = 40
      category = "Audio"
  } | ConvertTo-Json

  Invoke-RestMethod -Uri "http://localhost:8080/api/products" -Method Post -Body $body -ContentType "application/json"
  ```
- **cURL**:
  ```bash
  curl -X POST http://localhost:8080/api/products \
    -H "Content-Type: application/json" \
    -d "{\"name\":\"Noise Cancelling Earbuds\",\"description\":\"Wireless earbuds with active noise cancellation\",\"price\":79.99,\"quantity\":40,\"category\":\"Audio\"}"
  ```
- **Response `201 Created`**:
  - `Location` header: `http://localhost:8080/api/products/9`
  ```json
  {
    "id": 9,
    "name": "Noise Cancelling Earbuds",
    "description": "Wireless earbuds with active noise cancellation",
    "price": 79.99,
    "quantity": 40,
    "category": "Audio",
    "createdAt": "2026-11-15T19:35:00",
    "updatedAt": "2026-11-15T19:35:00"
  }
  ```

---

### 7. Update Product
- **Endpoint**: `PUT /api/products/{id}`
- **Header**: `Content-Type: application/json`
- **Request Body**:
  ```json
  {
    "name": "Standard Business Laptop 15",
    "description": "Upgraded with 32GB RAM and 1TB SSD",
    "price": 999.00,
    "quantity": 20,
    "category": "Electronics"
  }
  ```
- **PowerShell**:
  ```powershell
  $body = @{
      name = "Standard Business Laptop 15"
      description = "Upgraded with 32GB RAM and 1TB SSD"
      price = 999.00
      quantity = 20
      category = "Electronics"
  } | ConvertTo-Json

  Invoke-RestMethod -Uri "http://localhost:8080/api/products/1" -Method Put -Body $body -ContentType "application/json"
  ```
- **cURL**:
  ```bash
  curl -X PUT http://localhost:8080/api/products/1 \
    -H "Content-Type: application/json" \
    -d "{\"name\":\"Standard Business Laptop 15\",\"description\":\"Upgraded with 32GB RAM and 1TB SSD\",\"price\":999.00,\"quantity\":20,\"category\":\"Electronics\"}"
  ```
- **Response `200 OK`**:
  ```json
  {
    "id": 1,
    "name": "Standard Business Laptop 15",
    "description": "Upgraded with 32GB RAM and 1TB SSD",
    "price": 999.00,
    "quantity": 20,
    "category": "Electronics",
    "createdAt": "2026-11-15T19:30:00",
    "updatedAt": "2026-11-15T19:36:00"
  }
  ```

---

### 8. Delete Product
- **Endpoint**: `DELETE /api/products/{id}`
- **PowerShell**:
  ```powershell
  Invoke-RestMethod -Uri "http://localhost:8080/api/products/1" -Method Delete
  ```
- **cURL**:
  ```bash
  curl -X DELETE http://localhost:8080/api/products/1
  ```
- **Response**: `204 No Content`

---

### 9. Validation Error Response Example
If an invalid request is submitted (e.g., negative price, blank name):
- **Response `400 Bad Request`**:
  ```json
  {
    "timestamp": "2026-11-15T19:37:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed for one or more fields",
    "path": "/api/products",
    "validationErrors": {
      "name": "Product name is required",
      "price": "Price must be greater than zero",
      "quantity": "Quantity cannot be negative",
      "category": "Category is required"
    }
  }
  ```
