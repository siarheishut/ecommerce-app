# Java Spring E-Commerce Web Application

This is a full-stack e-commerce web application built with Java and the Spring
Boot framework. It features a complete
shopping experience including product browsing, a shopping cart, user
authentication, and an admin panel for managing
products and categories.

## Features

- **User Features:**
    - User registration and login with password reset functionality.
    - Browse and filter products by name, category, price range, and
      availability.
    - View product details and write reviews.
    - Session-based shopping cart functionality (update quantity, remove items).
    - View order history.
    - Manage personal information and shipping addresses (up to 5).
    - Secure checkout process to create orders.
- **Admin Features:**
    - Role-based access control (ADMIN vs. USER).
    - Full CRUD functionality including soft delete and restore for products and
      categories.
- **Security:**
    - End-to-end security using Spring Security.
    - Password encoding with BCrypt.
- **Backend:**
    - The backend is built with a service-oriented architecture and uses Spring
      Data JPA with Hibernate for data persistence.
    - Asynchronous email service for order confirmations and password resets.

## Tech Stack

- **Backend:** Java 24, Spring Boot 3, Spring Security, Spring Data JPA
- **Database:** MySQL
- **Frontend:** Thymeleaf, Bootstrap CSS
- **Build Tool:** Maven
- **Testing:** JUnit 5, Mockito, H2

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
    - In the `src/main/resources/` directory, create a new file named
      `application.properties`.
    - Copy the contents from `application.properties.example` into your new
      file.
    - Update the `spring.datasource.username`, `spring.datasource.password`,
      `spring.mail.username` and
      `spring.mail.password` with your MySQL and Gmail credentials.

4. **Build and Run the Application:**
    - You can run the application using your IDE (like IntelliJ IDEA) or by
      using the Maven wrapper:
   ```bash
   ./mvnw spring-boot:run
   ```
    - The application will be accessible at `http://localhost:8080`.
