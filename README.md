# Java Spring E-Commerce Web Application

[![Live Demo](https://img.shields.io/badge/demo-online-green.svg)](https://siarhei-shut-ecommerce.com)

This is a full-stack e-commerce web application built with Java and the Spring
Boot framework. It features a complete
shopping experience including product browsing, a shopping cart, user
authentication, and an admin panel for managing
products and categories.

## Features

- **User Features:**
    - User registration and login with password reset functionality and **"
      Remember Me"** option.
    - Browse and filter products by name, category, price range, and
      availability.
    - **Pagination** support for the product list.
    - View product details and write reviews.
    - Advanced Shopping Cart:
        - Session-based cart for guests.
        - **Automatic merging** of guest cart with the user's database cart upon
          successful login.
        - Update quantities and remove items.
    - View order history.
    - Manage personal information and shipping addresses (up to 5).
    - Secure checkout process to create orders.

- **Admin Features:**
    - Role-based access control (ADMIN vs. USER).
    - Full CRUD functionality for products and categories.
    - **Soft delete** and **Restore** functionality for products and categories.
    - Search and filter products/categories in the admin panel (by status:
      active, deleted, all).

- **Security:**
    - End-to-end security using Spring Security.
    - Password encoding with BCrypt.
    - CSRF protection.

- **Backend & Infrastructure:**
    - Service-oriented architecture using Spring Data JPA and Hibernate.
    - Asynchronous email service for order confirmations and password resets.
    - **Docker** and **Docker Compose** support for containerized deployment.
    - **CI/CD pipeline** configured with GitHub Actions for building and
      deploying to Google Cloud (Cloud Run).
    - **API Documentation:** Interactive **OpenAPI (Swagger)** documentation
      with **segregated access**:
        - Public API group for storefront features.
        - Secured Admin API group (accessible only to authenticated admins).

## Tech Stack

- **Backend:** Java 24, Spring Boot 3.5, Spring Security, Spring Data JPA,
  Spring Session JDBC
- **Database:** MySQL
- **Frontend:** Thymeleaf, Bootstrap CSS
- **Build Tool:** Maven
- **DevOps:** Docker, GitHub Actions, Google Cloud Platform
- **Testing:** JUnit 5, Mockito, H2

## Setup and Installation

You can run this project locally using Docker (recommended) or via a standard
Maven setup.

### Option 1: Run with Docker Compose (Recommended)

1. **Clone the repository:**
   ```bash
   git clone https://github.com/siarheishut/ecommerce-app.git
   cd ecommerce-app
   ```
2. **Configure Environment:**

- Create a `.env` file in the root directory (or rely on the defaults in
  `docker-compose.yml`).
- Set the required environment variables (DB credentials, Mail settings) as
  seen in `application.properties.example`.

3. **Build and Run:**
   ```bash
   docker-compose up --build
   ```
   The application will be accessible at `http://localhost:8080`. The MySQL
   database will be automatically provisioned in a container.

### Option 2: Local Run (Maven)

1. **Clone the repository:**
   ```bash
   git clone https://github.com/siarheishut/ecommerce-app.git
   cd ecommerce-app
   ```

2. **Database Setup:**
    - Ensure you have a MySQL server installed and running.
    - Create a new database named `ecommerce_db`.
    - Create a MySQL user with privileges to access this database.

3. **Configure Application Properties:**
    - In the `src/main/resources/` directory, create a new file named
      `application.properties`.
    - Copy the contents from `application.properties.example` into your new
      file.
    - Update `spring.datasource.username`, `spring.datasource.password`,
      `spring.mail.username`, and `spring.mail.password` with your MySQL and
      Gmail (App Password) credentials.

4. **Build and Run the Application:**
    - You can run the application using your IDE (like IntelliJ IDEA) or by
      using the Maven wrapper:
      ```bash
      ./mvnw spring-boot:run
      ```
    - The application will be accessible at `http://localhost:8080`.

> 📘 **API Documentation:** Once the application is running, access the
> interactive UI at:  
> `http://localhost:8080/swagger-ui/index.html`
>
> By default, the **Public** API definition is shown. To view **Admin**
> endpoints:
> 1. Log in as an administrator.
> 2. Select **"admin"** from the definition dropdown in the top-right corner of
     the Swagger UI.