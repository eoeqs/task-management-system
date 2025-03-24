# Task Management System

This is a simple Task Management System API built with Java, Spring Boot, and Spring Security. It allows users to create, edit, delete, and view tasks, with support for comments, authentication, role-based access control, filtering, and pagination.

## Features
- **Authentication**: Register and login with email/password, JWT-based access.
- **Roles**:
    - **Admin**: Full control over all tasks (CRUD, assign users, comments).
    - **User**: Manage own tasks (status updates, comments) if author or assignee.
- **Tasks**: Create, update, delete, view tasks with title, description, status, priority, author, and assignee.
- **Comments**: Add and view comments for tasks.
- **Filtering & Pagination**: Filter tasks by status, priority, author, assignee with paginated results.
- **API Documentation**: Swagger UI available at `/swagger-ui.html`.

## Prerequisites
- **Java**: 17 
- **Docker**: For running PostgreSQL and the app via Docker Compose

## Tech Stack
- **Backend**: Spring Boot, Spring Security, JPA/Hibernate
- **Database**: PostgreSQL
- **API Docs**: OpenAPI (Swagger)
- **Testing**: JUnit 5, Mockito

## Setup and running locally

### 1. Clone the repository
```bash
git clone https://github.com/eoeqs/task-management-system.git
cd task-management-system
```
### 2. Build and run with Docker Compose
```bash
docker-compose up --build
```

To stop the container:

```bash
docker-compose down
```

### 3. Running tests
```bash
./gradlew test
```
