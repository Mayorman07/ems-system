#  Employee Management System (EMS) - Microservices

## Project Overview
This project implements a comprehensive Employee Management System using a microservices architecture.
It showcases proficiency in building distributed systems with Spring Boot, Spring Cloud, containerization with Docker, 
and deployment concepts relevant to cloud environments like AWS. The system handles core employee management tasks, authentication/authorization, 
and asynchronous communication patterns.

## Features Implemented ✨
- Employee Management (EmployeesService)
  * CRUD operations for employee data.
  * User registration with email verification.
  * Secure password encoding (BCrypt).
  * Role assignment (e.g., ADMIN, USER).
  * Initial Admin setup endpoint.
  * Password reset functionality (request token, reset password)

- Authentication & Authorization
  * Login endpoint (/employees/login via Gateway).
  * JWT generation upon successful login.
  * JWT validation via API Gateway and downstream services (JwtAuthorities library).
  * Role-based access control (implied via Spring Security).

- Asynchronous Email Notifications (EmailService)
 * Listens for UserCreatedEvent and PasswordResetEvent via RabbitMQ.
 * Sends verification and password reset emails using SendGrid.

- API Gateway (ApiGateway)
 * Single entry point for all client requests.
 * Route definitions mapping external paths to internal services (e.g., /setup/initial-admin -> employees-service/api/setup/create-admin).
 * Request filtering (e.g., path rewriting, header manipulation).
 * Integration with Eureka for dynamic routing (lb:// protocol).

- Service Discovery (DiscoveryService)
 * Eureka server for service registration and lookup.
    
- Centralized Configuration (ConfigServer)
 * Manages external application properties using a Git backend (https://github.com/Mayorman07/EmsConfiguration/).
 * Services fetch configuration on startup.
   
- Infrastructure
* RabbitMQ for message queuing.
* MySQL for persistent data storage.
* Flyway for database schema version control.

## Technology Stack 🛠️
* Backend: Java 17+, Spring Boot 3.x, Spring Cloud 2023.x (Netflix Eureka, Config, Gateway, Bus), Spring Data JPA (Hibernate), Spring Security, Spring AMQP.
* Database: MySQL 8.0.
* Messaging: RabbitMQ.
* Build: Apache Maven 3.8+.
* Containerization: Docker.
* Libraries: Lombok, ModelMapper, Flyway, JJWT (inferred from JwtAuthorities).
* External Services: SendGrid.

## Project Structure (High-Level)
```
ems-system/
├── ApiGateway/             # Spring Cloud Gateway Service
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── ConfigServer/           # Spring Cloud Config Server
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── DiscoveryService/       # Spring Cloud Eureka Server
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── EmailService/           # Handles sending emails via RabbitMQ
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── EmployeesService/       # Core employee logic, auth, persistence
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── JwtAuthorities/         # Local shared library for JWT parsing
│   ├── src/
│   └── pom.xml
├── docker-compose.yml      # Example for local infrastructure setup
└── README.md               # This file
```

## Local Development Setup ⚙️
- Prerequisites
* Java JDK 17+
* Apache Maven 3.8+
* Docker & Docker Compose
* Postman
* Git Client
* IDE (IntelliJ IDEA)

## Steps
* Clone Repository
  - git clone <your-repository-url>
  - cd <repository-directory-name>
* Build Shared Library : (If JwtAuthorities is used locally)
  - cd JwtAuthorities
* mvn clean install
* cd ..
* Start Infrastructure (Docker Compose Recommended):
  - Use the docker-compose.yml (see example below) to run Eureka, Config Server, RabbitMQ, and MySQL. (Ensure you have built local images for DiscoveryService and ConfigServer if specified in the compose file.)
* Run:
  - docker-compose up -d
* Example docker-compose.yml (Replace placeholders):
* services:
* discovery-service:
  - image or build directive for Eureka
* ports: ["9010:9010"]
* config-server:
  - image or build directive for Config Server
* ports: ["9012:9012"]
* environment
* SPRING_CLOUD_CONFIG_SERVER_GIT_URI: [https://github.com/Mayorman07/EmsConfiguration/](https://github.com/Mayorman07/EmsConfiguration/)
* SPRING_SECURITY_USER_NAME: <config-server-username>
* SPRING_SECURITY_USER_PASSWORD : <config-server-password>
* rabbitmq:
  - image: rabbitmq:3-management
  - ports: ["5672:5672", "15672:15672"]
* environment:
  - RABBITMQ_DEFAULT_USER: <your-rabbitmq-username>
  - RABBITMQ_DEFAULT_PASS: <your-rabbitmq-password>
  - RABBITMQ_DEFAULT_VHOST: <your-rabbitmq-vhost>
* mysql:
  - image: mysql:8.0
  - ports: ["3306:3306"]
  - volumes: ["mysql_data:/var/lib/mysql"]
* environment:
  - MYSQL_ROOT_PASSWORD: <your-mysql-root-password>
  - MYSQL_DATABASE: ems_app
  - MYSQL_USER: <your-mysql-app-username>
  - MYSQL_PASSWORD: <your-mysql-app-password>
  - volumes:
* mysql_data:
* Run Microservices:
  - Configure IDE run configurations with necessary environment variables (e.g., SENDGRID_API_KEY).
  - Ensure properties point to localhost or Docker Compose service names for infrastructure.
* Navigate to each service directory (ApiGateway, EmployeesService, EmailService) and run:
  - mvn spring-boot: run.

## Building Docker Images 🐳
* In each service directory (ApiGateway, EmployeesService, EmailService)
* mvn clean package -DskipTests
* docker build -t <your-dockerhub-username>/<service-name>:latest .
* docker push <your-dockerhub-username>/<service-name>:latest

## Deployment Overview ☁️
* Deploy containers to hosts (e.g., AWS EC2 instances).
* Inject configuration (IPs, credentials, API keys) securely via environment variables using -e flags in Docker run commands (see examples below).
* Configure network security (e.g., AWS Security Groups) for inter-service communication (Private IPs) and external access (API Gateway).
* Services register their Private IP with Eureka via EUREKA_INSTANCE_IP_ADDRESS.

Example Docker run (Placeholders Only)
* --- API Gateway ---
  ```
  docker run -d --name api-gateway -p 9082:9082 --restart always \
  -e EUREKA_INSTANCE_IP_ADDRESS=<ApiGateway-Private-IP> \
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://<Eureka-Private-IP>:9010/eureka \
  -e SPRING_RABBITMQ_HOST=<RabbitMQ-Private-IP> \
  -e SPRING_RABBITMQ_USERNAME=<your-rabbitmq-username> \
  -e SPRING_RABBITMQ_PASSWORD=<your-rabbitmq-password> \
  -e SPRING_RABBITMQ_VHOST=<your-rabbitmq-vhost> \
  -e TOKEN_SECRET_KEY=<Your-JWT-Secret-Key> \
  <your-dockerhub-username>/api-gateway:latest
  ```
* --- Email Service ---
  ```
  docker run -d --name email-service -p 9050:9050 --restart always \
  -e SERVER_PORT=9050 \
  -e EUREKA_INSTANCE_IP_ADDRESS=<EmailService-Private-IP> \
  -e EUREKA_SERVER_URL=http://<Eureka-Private-IP>:9010/eureka \
  -e SPRING_RABBITMQ_HOST=<RabbitMQ-Private-IP> \
  -e SPRING_RABBITMQ_USERNAME=<your-rabbitmq-username> \
  -e SPRING_RABBITMQ_PASSWORD=<your-rabbitmq-password> \
  -e SPRING_RABBITMQ_VHOST=<your-rabbitmq-vhost> \
  -e SENDGRID_API_KEY=<Your-Actual-SendGrid-Key> \
  -e GATEWAY_URL=http://<ApiGateway-Public-IP>:9082 \
  <your-dockerhub-username>/email-service:latest
  ```
* --- Employees Service ---
  ```
  docker run -d --name employees-service --restart always -p 8181:8181 \
  -e SERVER_PORT=8181 \
  -e EUREKA_INSTANCE_IP_ADDRESS=<EmployeesService-Private-IP> \
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://<Eureka-Private-IP>:9010/eureka \
  -e CONFIG_SERVER_URL=http://<Config-Private-IP>:9012 \
  -e CONFIG_SERVER_USERNAME=<config-server-username> \
  -e CONFIG_SERVER_PASSWORD=<config-server-password> \
  -e SPRING_RABBITMQ_HOST=<RabbitMQ-Private-IP> \
  -e SPRING_RABBITMQ_USERNAME=<your-rabbitmq-username> \
  -e SPRING_RABBITMQ_PASSWORD=<your-rabbitmq-password> \
  -e SPRING_RABBITMQ_VHOST=<your-rabbitmq-vhost> \
  -e DB_HOST=<MySQL-Private-IP> \
  -e DB_PORT=3306 \
  -e DB_NAME=ems_app \
  -e DB_USER=<your-mysql-app-username> \
  -e DB_PASSWORD=<your-mysql-app-password> \
  -e TOKEN_SECRET_KEY=<Your-JWT-Secret-Key> \
  -e GATEWAY_IP=<ApiGateway-Private-IP> \
  <your-dockerhub-username>/employees-service:latest
  ```
## API Endpoints (via API Gateway)

| Method | Gateway Endpoint                    | Description                       | Access     | Forwarded To (Service Endpoint)              |
| :----- | :---------------------------------- | :-------------------------------- | :--------- | :------------------------------------------- |
| POST   | `/setup/initial-admin`              | Creates the first admin user      | Public     | `employees-service/api/setup/create-admin`   |
| POST   | `/employees/login`                  | Authenticates user & gets JWT     | Public     | `employees-service/employees/login`          |
| GET    | `/employees/verify`                 | Verifies user email via token     | Public     | `employees-service/verify`                   |
| POST   | `/employees/verify`                 | Resends verification email?       | Public     | `employees-service/verify`                   |   
| POST   | `/employees/password-reset/request` | Requests password reset email     | Public     | `employees-service/password-reset/request`   |   
| POST   | `/employees/password-reset/reset`   | Submits new password with token   | Public     | `employees-service/password-reset/reset`     |   
| POST   | `/employees`                        | Creates a new employee            | Auth       | `employees-service/`                         |   
| GET    | `/employees/employees/{id}`         | Get employee details by ID        | Auth       | `employees-service/employees/{id}`           |   
| PUT    | `/employees/employees/{id}`         | Update employee details by ID     | Auth       | `employees-service/employees/{id}`           |   
| DELETE | `/employees/employees/{id}`         | Delete employee by ID             | Auth       | `employees-service/employees/{id}`           |   
| GET    | `/employees/actuator/**`            | Accesses employee service actuator| Internal   | `eemployees-service/actuator/**`                

- (Access column indicates whether authentication (JWT) is typically required)

## Access Points (Example Deployed) 🌍
* API Gateway: http://<ApiGateway-Public-IP>:9082
* Eureka Dashboard: http://<Eureka-Public-IP>:9010
* RabbitMQ Management: http://<RabbitMQ-Public-IP>:15672
  



 
