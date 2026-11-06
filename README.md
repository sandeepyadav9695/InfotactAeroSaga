# 🚁 AeroSaga – Autonomous Drone Mission Control System

AeroSaga is an autonomous drone mission control platform designed to manage and monitor a fleet of delivery drones. It combines **Java Spring Boot, Temporal.io, MySQL, React.js, and Docker** to manage long-running drone missions and provide real-time fleet visibility through a web dashboard.

The project demonstrates workflow orchestration, REST API development, database persistence, drone status management, and frontend-backend integration.

---

## 📌 1. Project Overview

Traditional REST APIs generally handle short-lived synchronous requests. However, autonomous drone missions involve multiple steps and can run for extended periods.

AeroSaga uses Temporal.io to orchestrate long-running mission workflows with persistent execution state.

### Main Objectives

- Manage multiple delivery drones.
- Monitor drone locations and battery levels.
- Activate and deactivate drones.
- Manage drone mission lifecycles.
- Store drone information in MySQL.
- Provide a React-based fleet monitoring dashboard.
- Use Temporal workflows for reliable long-running mission orchestration.

---

## 🏗️ 2. System Architecture

```text
                 AeroSaga Dashboard
                    React + Vite
                     Port 5173
                         |
                         | REST API
                         v
                 Spring Boot Backend
                     Port 8081
                         |
              +----------+----------+
              |                     |
              v                     v
          MySQL Database       Temporal Client
                                   |
                                   v
                            Temporal Server
                               Port 7233
                                   |
                                   v
                            Temporal Worker
                                   |
                                   v
                           Drone Mission Workflow
                                   |
                                   v
                       Drone Activities / Simulation
```

### Architecture Components

| Component | Responsibility |
|---|---|
| React + Vite | Frontend dashboard |
| Spring Boot | Backend REST APIs |
| MySQL | Persistent drone data |
| Temporal Server | Workflow orchestration |
| Temporal Worker | Executes workflow activities |
| Docker | Runs supporting services |

---

## 🛠️ 3. Technologies Used

### Backend
- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Maven
- REST APIs
- Temporal Java SDK

### Frontend
- React.js
- JavaScript
- HTML5
- CSS3
- Vite
- React Leaflet
- Leaflet Maps

### Database and DevOps
- MySQL
- Docker
- Docker Compose
- Git
- GitHub

---

## 📁 4. Project Structure

```text
AerosagaProject/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── Aerosaga/
│       │       └── withsky/
│       │           └── AerosagaProject/
│       │               │
│       │               ├── controller/
│       │               │   └── DroneController.java
│       │               │
│       │               ├── service/
│       │               │   └── DroneService.java
│       │               │
│       │               ├── entity/
│       │               │   └── Drone.java
│       │               │
│       │               ├── repositry/
│       │               │   └── DroneRepository.java
│       │               │
│       │               ├── workflow/
│       │               ├── activities/
│       │               └── config/
│       │
│       └── resources/
│           └── application.properties
│
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── App.jsx
│   │   ├── App.css
│   │   ├── index.css
│   │   └── main.jsx
│   │
│   ├── package.json
│   └── vite.config.js
│
├── docker-compose.yml
├── pom.xml
└── README.md
```

*Note: Workflow, activity, and configuration folders are shown as architectural locations; include them if they exist in your current source tree.*

---

# 🚀 5. Getting Started – Run the Complete Project

Follow these steps to start AeroSaga from Docker to the frontend dashboard.

## Step 1: Install Prerequisites

Install the following software:

| Software | Purpose |
|---|---|
| JDK 17 | Java development |
| Maven | Backend build |
| Node.js and npm | Frontend development |
| MySQL Server | Database |
| Docker Desktop | Run Temporal Server |
| Git | Version control |
| IntelliJ IDEA / VS Code | Development |

Verify installations:

```powershell
java -version
mvn -version
node -v
npm -v
docker --version
docker compose version
```

Make sure Docker Desktop is running before continuing.

## Step 2: Clone the GitHub Repository

```powershell
git clone https://github.com/sandeepyadav9695/AeroSaga.git
```

Move into the project directory:

```powershell
cd AeroSaga
```

If your repository has a different name, use its actual GitHub URL and folder name.

## Step 3: Start Temporal Server Using Docker

Open the main project directory.

Create a file named:

`docker-compose.yml`

Add the following configuration:

```yaml
services:
  temporal:
    image: temporalio/temporal:latest
    container_name: aerosaga-temporal
    command: server start-dev --ip 0.0.0.0 --ui-port 8080
    ports:
      - "7233:7233"
      - "8080:8080"
    restart: unless-stopped
```

### Start Docker Container

```powershell
docker compose up -d
```

### Check Running Containers

```powershell
docker ps
```

### View Temporal Logs

```powershell
docker logs -f aerosaga-temporal
```

### Stop Temporal Server

```powershell
docker compose down
```

### Temporal Dashboard

Open the following URL in your browser:

http://localhost:8080

**Important:** Keep Docker Desktop running while using Temporal. The Spring Boot Temporal client and worker must connect to `localhost:7233`.

## Step 4: Configure MySQL Database

Start your MySQL Server and open MySQL Workbench or the MySQL command-line client.

Create the database:

```sql
CREATE DATABASE AeroSaga;

USE AeroSaga;
```

If your project uses Hibernate schema generation, tables can be generated automatically based on your JPA entities.

Example `application.properties` configuration:

```properties
spring.application.name=AeroSagaProject

server.port=8081

spring.datasource.url=jdbc:mysql://localhost:3306/AeroSaga
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Temporal Server
temporal.service.target=localhost:7233
```

Use the property names expected by your actual Temporal configuration. The Temporal client may require a different configuration mechanism depending on your implementation.

**Security:** Never commit your actual MySQL password or other credentials to GitHub. Use environment variables or a local configuration file excluded through `.gitignore`.

## Step 5: Start the Spring Boot Backend

Open a new PowerShell terminal.

Navigate to the project root:

```powershell
cd AerosagaProject
```

Clean the previous build:

```powershell
mvn clean
```

Build the application:

```powershell
mvn install
```

Run the backend:

```powershell
mvn spring-boot:run
```

Backend URL:

http://localhost:8081

Check the health endpoint if Spring Boot Actuator is configured:

http://localhost:8081/actuator/health

The backend requires a running MySQL database and, if the Temporal worker starts with the application, an available Temporal Server.

## Step 6: Start the React Frontend

Open another PowerShell terminal.

Navigate to the frontend directory:

```powershell
cd AerosagaProject/frontend
```

Install dependencies:

```powershell
npm install
```

Install map dependencies if they are not already in `package.json`:

```powershell
npm install leaflet react-leaflet
```

Start the development server:

```powershell
npm run dev
```

Frontend URL:

http://localhost:5173

---

# 🔌 6. Frontend API Configuration

Configure the Vite development proxy in `frontend/vite.config.js`:

```javascript
import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: "http://localhost:8081",
        changeOrigin: true
      }
    }
  }
});
```

This allows the React frontend to communicate with Spring Boot through `/api` without hardcoding the backend host in each API request.

---

# 📡 7. REST API Endpoints

The following drone-management endpoints are implemented in the backend.

| HTTP Method | Endpoint | Description |
|---|---|---|
| GET | `/api/drones` | Get all drones |
| GET | `/api/drones/{id}` | Get drone by ID |
| POST | `/api/drones` | Register a new drone |
| PATCH | `/api/drones/{id}/activate` | Activate an idle drone |
| PATCH | `/api/drones/{id}/deactivate` | Deactivate an active drone |
| PATCH | `/api/drones/{id}/battery?batteryLevel=50` | Update battery level |

### Example: Get All Drones

```http
GET http://localhost:8081/api/drones
```

### Example: Activate Drone

```http
PATCH http://localhost:8081/api/drones/11/activate
```

### Example: Deactivate Drone

```http
PATCH http://localhost:8081/api/drones/11/deactivate
```

### Example: Update Battery

```http
PATCH http://localhost:8081/api/drones/11/battery?batteryLevel=75
```

### Example: Register Drone

```http
POST http://localhost:8081/api/drones
Content-Type: application/json
```

Request body:

```json
{
  "name": "Drone-17",
  "latitude": 25.7464,
  "longitude": 82.6837
}
```

The service initializes the battery level and status for a newly registered drone.

---

# 🛸 8. Drone Management Features

### Drone Registration
- Register new drones.
- Store drone details in MySQL.
- Prevent duplicate drone names.

### Drone Activation
- Activate drones with IDLE status.
- Validate battery levels before activation.
- Save updated status in the database.

### Drone Deactivation
- Deactivate drones with ACTIVE status.
- Change the simulated status to IDLE.
- Persist the updated status.

### Battery Management
- Track battery percentages.
- Validate battery levels between 0 and 100.
- Prevent activation when battery is 10% or lower.

### Fleet Monitoring
- View registered drones.
- Display battery levels and operational status.
- View drone positions on the map.
- Search and monitor fleet records.

---

# ⏱️ 9. Temporal Workflow Concept

Temporal.io is used as the workflow orchestration technology for long-running drone missions.

An example mission lifecycle:

```text
        Start Drone Mission
                |
                v
           Drone Takeoff
                |
                v
          Navigate to Target
                |
                v
           Deliver Package
                |
                v
          Return to Base
                |
                v
          Mission Completed
```

### Temporal Components

- **Workflow:** Defines the mission execution sequence.
- **Activities:** Perform individual mission operations.
- **Worker:** Executes workflow and activity code.
- **Temporal Server:** Maintains workflow execution state.
- **Signals:** Can be used to communicate with running workflows, such as requesting cancellation or return-home behavior.

Temporal workflow reliability, mission recovery, and signal handling should be verified through end-to-end tests before being considered production-ready.

---

# 🧪 10. Testing the Application

Test the following areas after starting the application:

| Test | Expected Result |
|---|---|
| Start Docker | Temporal container runs |
| Open Temporal UI | Dashboard loads |
| Start MySQL | Database connection succeeds |
| Start Spring Boot | Backend starts on port 8081 |
| GET all drones | Drone records are returned |
| Activate drone | Status changes to ACTIVE |
| Deactivate drone | Status changes to IDLE |
| Update battery | Battery value is updated |
| Start React | Dashboard loads on port 5173 |
| Open map | Drone locations are displayed |

For mission workflows, additionally test successful completion, activity failures, worker restarts, and workflow recovery.

---

# 🐛 11. Common Problems and Solutions

### Problem 1: Docker command not recognized

**Solution:**
- Install Docker Desktop.
- Start Docker Desktop.
- Restart PowerShell.
- Verify:

```powershell
docker --version
```

### Problem 2: Temporal connection refused

Example:

```text
io.grpc.StatusRuntimeException: UNAVAILABLE
Connection refused: localhost:7233
```

**Solution:**

```powershell
docker compose up -d
docker ps
docker logs -f aerosaga-temporal
```

Verify that port 7233 is available and the Temporal Server has started.

### Problem 3: MySQL connection failed

Check:
- MySQL Server is running.
- Database `AeroSaga` exists.
- Username and password are correct.
- Database URL and port are correct.

### Problem 4: Frontend API connection refused

Example:

```text
ECONNREFUSED localhost:8081
```

**Solution:**
- Start the Spring Boot backend.
- Check that the backend runs on port 8081.
- Verify `vite.config.js`.
- Restart the Vite development server.

### Problem 5: Port 8081 already in use

Check the process:

```powershell
netstat -ano | findstr :8081
```

Identify the PID and close the process only if you have confirmed it is safe to stop.

### Problem 6: Maven build failure

Run:

```powershell
mvn clean
mvn install
```

Check Java and Maven versions and review the complete build error.

---

# 🔐 12. Security Considerations

- Do not upload database passwords to GitHub.
- Do not commit API keys or access tokens.
- Keep local environment files out of version control.
- Validate incoming REST API requests.
- Use authentication and authorization before production deployment.
- Do not treat a simulated status change as a physical drone safety command.

---

# 📅 13. Development Progress

| Week | Development Focus |
|---|---|
| Week 1 | Project setup, architecture, Docker and Temporal integration foundation |
| Week 2 | Backend development and database integration |
| Week 3 | Frontend development and drone fleet dashboard |
| Week 4 | Drone activation/deactivation APIs, validation, and dashboard improvements |
| Future | Temporal signal integration, emergency handling, workflow recovery, and end-to-end testing |

---

# 🔮 14. Future Enhancements

- Live drone telemetry integration.
- Real-time GPS tracking.
- Automated low-battery alerts.
- Emergency mission cancellation.
- Return-to-home functionality.
- Advanced Temporal workflow recovery testing.
- User authentication and role-based access.
- Mission history and analytics.
- Production deployment and monitoring.

---

# 👨‍💻 15. Developer

**Developer:** Sandeep Yadav

**Project:** AeroSaga – Autonomous Drone Mission Control System

**Primary Domain:** Java Full Stack Development

**Technologies:** Java, Spring Boot, Temporal.io, MySQL, React.js, Docker

**GitHub:** https://github.com/sandeepyadav9695

---

# 📜 16. License

This project is developed for learning, demonstration, and software engineering practice.

---

## ⭐ Conclusion

AeroSaga demonstrates how modern backend technologies, workflow orchestration, database management, and frontend development can be combined to build an autonomous drone mission control platform.

The project aims to provide a foundation for reliable long-running mission management, drone fleet monitoring, and scalable distributed application development.
