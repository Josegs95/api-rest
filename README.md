# API REST

![API Version](https://img.shields.io/badge/API%20Version-v1-blue.svg)
![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-brightgreen.svg)
![Database](https://img.shields.io/badge/Database-PostgreSQL-blue.svg)
![Docker Ready](https://img.shields.io/badge/Docker-Ready-blue?logo=docker)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)

## ℹ️ Overview

Third portfolio project. This time, a simple web API RESTful application where users can consume information about 
videogames through endpoints and admin users can do CRUD operations. I made this project to learn to use the Spring 
Framework, to develop a web application and to work with PostgreSQL.

> 🔒 Currently, only admin users can perform CRUD operations on other users.  
> I decided this for simplicity's sake. This may change in the future.

## ⭐ Highlights

Here are some highlights of the project. Even if it's a simple API, I wanted to add some features to enrich it.

* **Security:** Authentication and authorization based on **JWT (JSON Web Tokens)**.
* **User roles:** Role hierarchy with distinct permissions between the `USER` and `ADMIN` roles.
* **Protected Endpoints:** Only `ADMIN` users can do CRUD operations. `USER` users only can use `GET` methods.
* **Relational Database:** Persistence managed with **Spring Data JPA** (Hibernate) and **PostgreSQL**.
* **Validation:** Input data (`DTOs`) validated using `jakarta.validation`.
* **Testing:** High coverage with unit (`JUnit 5` and `Mockito`) and integration tests (`@SpringBootTest`).
* **Container ecosystem:** Fully containerized with **Docker** and **Docker Compose** for an easy execution.

## 🛠️ Tech Stack

* **Framework:** Spring Boot 3
* **Language:** Java 21
* **Security:** Spring Security 6
* **Persistence:** Spring Data JPA / Hibernate
* **Database engine:** PostgreSQL
* **Authentication:** JSON Web Tokens (JWT)
* **Testing:** JUnit 5, Mockito, AssertJ
* **Container platform:** Docker / Docker Compose
* **Build tool:** Maven

## 🚀 Setup Instructions

1. Install Docker Desktop and have the docker daemon running.
2. Clone the repository: `git clone {repository-url}`.
3. Rename `.env.example` file to `.env` and adjust it.
4. Build and run the project: `docker compose up --build` in the repo directory.
5. The API should now be running at: `http://localhost:8080`

## 📄 API Documentation

Once the application is running, you can access the interactive API documentation (powered by Swagger/Springdoc) at:

`http://localhost:8080/docs/swagger-ui`

## 📖 References

- [Spring Official Documentation](https://docs.spring.io/spring-framework/reference/index.html)
- [Stack Overflow](https://stackoverflow.com/questions)
- [Spring course](https://www.youtube.com/playlist?list=PLkVpKYNT_U9fGwrf_rVl-t_yjnixdsK6E) -- in spanish
- [ChatGPT](https://chatgpt.com/) and [Gemini](https://gemini.google.com/) as AI support