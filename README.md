# Java Spring E-Commerce Web Application

This is a full-stack e-commerce web application built with Java and the Spring Boot framework. It features a complete
shopping experience including product browsing, a shopping cart, user authentication, and an admin panel for managing
products and categories.

## Features

- **User Features:**
    - User registration and login.
    - Browse and filter products by name and category.
    - Session-based shopping cart functionality.
    - Secure checkout process to create orders.
- **Admin Features:**
    - Role-based access control (ADMIN vs. USER).
    - Full CRUD (Create, Read, Update, Delete) functionality for products.
    - Full CRUD functionality for categories.
- **Security:**
    - End-to-end security using Spring Security.
- **Backend:**
    - The backend is built with a service-oriented architecture and uses Spring Data JPA with Hibernate for data
      persistence.

## Tech Stack

- **Backend:** Java 24, Spring Boot 3, Spring Security, Spring Data JPA
- **Database:** MySQL
- **Frontend:** Thymeleaf, Bootstrap CSS
- **Build Tool:** Maven

## Setup and Installation

To run this project locally, please follow these steps:

1. **Clone the repository:**
   ```bash
   git clone https://github.com/siarheishut/ecommerce-app.git
   cd ecommerce-app
   ```

2. **Database Setup:**
    - Ensure you have MySQL server installed and running.
    - Create a new database named `ecommerce_db`.
    - Create a MySQL user with privileges to access this database.

3. **Configure Application Properties:**
    - In the `src/main/resources/` directory, create a new file named `application.properties`.
    - Copy the contents from `application.properties.example` into your new file.
    - Update the `spring.datasource.username`, `spring.datasource.password`, `spring.mail.username` and
      `spring.mail.password` with your MySQL and Gmail credentials.

4. **Build and Run the Application:**
    - You can run the application using your IDE (like IntelliJ IDEA) or by using the Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
    - The application will be accessible at `http://localhost:8080`.