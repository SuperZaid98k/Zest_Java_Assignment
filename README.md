# Java Backend Developer Assignment - Product REST API Solution

This repository contains a RESTful API solution built with Java 17 and Spring Boot for managing Products and associated Items.

## Technical Architecture

- **Java Version**: 17
- **Framework**: Spring Boot 3.2.5
- **Persistence**: Spring Data JPA & Hibernate
- **Database**: PostgreSQL (Production/Docker), H2 (In-memory Test Database)
- **Security**: Spring Security with JWT (Stateless) & Refresh Token Rotation
- **Documentation**: Swagger UI / OpenAPI 3 (`/swagger-ui.html`)
- **Containerization**: Docker & Docker Compose

## Database Schema & Tables

```sql
CREATE TABLE product (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(255) NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    created_on TIMESTAMP NOT NULL,
    modified_by VARCHAR(100),
    modified_on TIMESTAMP
);

CREATE TABLE item (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES product(id)
);
```

## API Endpoints

### Authentication Endpoints

- `POST /api/v1/auth/register` - Register a new user
- `POST /api/v1/auth/login` - User login and JWT token issuance
- `POST /api/v1/auth/refresh` - Refresh token rotation

### Product & Item Endpoints

- `GET /api/v1/products` - Retrieve all products (Supports pagination and search)
- `GET /api/v1/products/{id}` - Retrieve product details by ID
- `POST /api/v1/products` - Create a new product
- `PUT /api/v1/products/{id}` - Update product details
- `DELETE /api/v1/products/{id}` - Delete product
- `GET /api/v1/products/{id}/items` - Retrieve items belonging to a product
- `POST /api/v1/products/{id}/items` - Add item to product
- `DELETE /api/v1/products/{id}/items/{itemId}` - Delete item from product

## Local Setup & Execution

### Prerequisites

- Java 17+
- Maven 3.8+ (or Maven Wrapper `./mvnw`)
- Docker & Docker Compose (Optional for container deployment)

### Running Tests

Execute unit and integration tests using H2 database:

```bash
mvn test
```

### Running Application with Docker Compose

Start PostgreSQL database and Spring Boot application:

```bash
docker-compose up --build
```

Access Swagger UI documentation at `http://localhost:8080/swagger-ui.html`.
