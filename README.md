🏢 Leave Management System
A microservices-based leave request management system built with Spring Boot, designed for efficient employee leave tracking and approval workflows.

📋 Table of Contents
Overview
Architecture
Features
Technologies
Getting Started
API Documentation
Database Schema
Usage Examples
Contributing
🌟 Overview
This system manages employee leave requests through a microservices architecture, separating authentication/user management from leave request processing. It automatically calculates working days by excluding weekends and public holidays.

Key Capabilities:
✅ User registration and authentication
✅ Leave request submission
✅ Admin approval/rejection workflow
✅ Automatic working days calculation
✅ Holiday management
✅ Multiple leave request states (Pending, Approved, Rejected, InProgress)
🏗️ Architecture
Microservices

┌─────────────────────┐         ┌──────────────────────┐
│   Auth Service      │         │  Leave Request       │
│   (Port: 9000)      │◄───────►│  Service             │
│                     │  Feign  │  (Port: 9001)        │
│  - User Management  │         │  - Leave Requests    │
│  - Authentication   │         │  - Approval Workflow │
│  - JWT Tokens       │         │  - Holiday Calc      │
└─────────────────────┘         └──────────────────────┘
         │                               │
         │                               │
         ▼                               ▼
  ┌─────────────┐               ┌─────────────┐
  │  conge_db   │               │demande_     │
  │             │               │conge_db     │
  └─────────────┘               └─────────────┘
Service Communication
Feign Client: Inter-service communication for user validation
REST APIs: External communication
MySQL: Separate databases for each service
✨ Features
1. User Management (Auth Service)
User registration (signup)
User authentication (login)
JWT token generation
User role management (Employer, Administration, TeamLeader)
Auto-creation of admin account on startup
2. Leave Request Management
Submit leave requests
Automatic working days calculation
Exclude weekends (Saturday/Sunday)
Exclude public holidays
Multiple leave types support
3. Admin Functions
View all leave requests
View pending requests
Approve/Reject requests
Change request status
Manage public holidays
4. Holiday Management
Add/Remove public holidays
Automatic exclusion from leave day calculations
Year-based holiday management
🛠️ Technologies
Backend
Java 17+
Spring Boot 3.x
Spring Security - Authentication & Authorization
Spring Data JPA - Database operations
Spring Cloud OpenFeign - Microservice communication
MySQL - Database
Lombok - Reduce boilerplate code
JWT (JSON Web Tokens) - Secure authentication
Build Tools
Maven - Dependency management
🚀 Getting Started
Prerequisites
Java 17 or higher
Maven 3.6+
MySQL 8.0+
IDE (IntelliJ IDEA, Eclipse, or VS Code)
Installation
1. Clone the Repository

bash
git clone https://github.com/yourusername/leave-management-system.git
cd leave-management-system
2. Setup Databases

sql
-- Create databases
CREATE DATABASE conge_db;
CREATE DATABASE demande_conge_db;
3. Configure Auth Service
File: auth-service/src/main/resources/application.properties


properties
spring.application.name=conge_db
server.port=9000

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/conge_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
4. Configure Leave Request Service
File: demande-conge/src/main/resources/application.properties


properties
spring.application.name=demande_conge_db
server.port=9001

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/demande_conge_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
5. Build and Run
Terminal 1 - Auth Service:


bash
cd auth-service
mvn clean install
mvn spring-boot:run
Terminal 2 - Leave Request Service:


bash
cd demande-conge
mvn clean install
mvn spring-boot:run
Default Admin Account
The system automatically creates an admin account on first run:

Email: administration@test.com
Password: admin
Role: Administration
📚 API Documentation
Auth Service (Port 9000)
1. User Signup

http
POST http://localhost:9000/api/auth/signup
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "cin": "12345678"
}
Response:


json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "userRole": "Employer"
}
2. User Login

http
POST http://localhost:9000/api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
Response:


json
{
  "jwt": "eyJhbGciOiJIUzI1NiJ9...",
  "userRole": "Employer",
  "userId": 1
}
3. Get User by ID

http
GET http://localhost:9000/api/users/1
Response:


json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "userRole": "Employer"
}
Leave Request Service (Port 9001)
1. Submit Leave Request

http
POST http://localhost:9001/api/employer/leave/request
Content-Type: application/json

{
  "userId": 1,
  "fromDate": "2026-03-01",
  "toDate": "2026-03-07",
  "fromTime": "09:00:00",
  "toTime": "17:00:00",
  "type": "Annual Leave",
  "note": "Family vacation"
}
Response:


200 OK
Leave request submitted successfully!
2. Get All Leave Requests (Admin)

http
GET http://localhost:9001/api/admin/leave/all
Response:


json
[
  {
    "id": 1,
    "fromDate": "2026-03-01",
    "toDate": "2026-03-07",
    "days": 5,
    "state": "Pending",
    "type": "Annual Leave",
    "note": "Family vacation",
    "userId": 1
  }
]
3. Get Pending Leave Requests

http
GET http://localhost:9001/api/admin/leave/pending
4. Change Leave Request Status

http
PUT http://localhost:9001/api/admin/leave/1/status/APPROVED
Valid status values:

APPROVED / Approved
REJECTED / Rejected
PENDING / Pending
INPROGRESS / InProgress
Response:


200 OK
Leave request status updated successfully!
Holiday Management (Port 9001)
1. Add Holiday

http
POST http://localhost:9001/api/admin/holidays
Content-Type: application/json

{
  "date": "2026-01-01",
  "name": "New Year",
  "description": "New Year's Day",
  "year": 2026
}
2. Get All Holidays

http
GET http://localhost:9001/api/admin/holidays
3. Get Holidays by Year

http
GET http://localhost:9001/api/admin/holidays/year/2026
4. Delete Holiday

http
DELETE http://localhost:9001/api/admin/holidays/1
🗄️ Database Schema
Auth Service Database (conge_db)
Table: users


sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    prenom VARCHAR(255),
    cin VARCHAR(8) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    num_tel VARCHAR(20),
    user_role VARCHAR(50) NOT NULL
);
Leave Request Service Database (demande_conge_db)
Table: leave_request


sql
CREATE TABLE leave_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    from_time TIME,
    to_time TIME,
    days BIGINT,
    state VARCHAR(50) NOT NULL,
    note TEXT,
    type VARCHAR(100),
    user_id BIGINT NOT NULL
);
Table: holidays


sql
CREATE TABLE holidays (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    year INT NOT NULL
);
💡 Usage Examples
Complete Workflow Example
Step 1: Create Employee

bash
curl -X POST http://localhost:9000/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ahmed Trabelsi",
    "email": "ahmed@company.tn",
    "password": "ahmed123",
    "cin": "11223344"
  }'
Step 2: Login

bash
curl -X POST http://localhost:9000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ahmed@company.tn",
    "password": "ahmed123"
  }'
Step 3: Submit Leave Request

bash
curl -X POST http://localhost:9001/api/employer/leave/request \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "fromDate": "2026-07-15",
    "toDate": "2026-07-30",
    "type": "Annual Leave",
    "note": "Summer vacation"
  }'
Step 4: Admin Approves Request

bash
curl -X PUT http://localhost:9001/api/admin/leave/1/status/APPROVED
📊 Leave Request States
State	Description
Pending	Request submitted, awaiting review
Approved	Request approved by admin
Rejected	Request rejected by admin
InProgress	Leave currently in progress
📅 Supported Leave Types
Annual Leave - Paid vacation days
Sick Leave - Medical leave
Maternity Leave - Maternity/paternity leave
Training Leave - Professional development
Unpaid Leave - Leave without pay
Emergency Leave - Urgent family matters
🔐 Security
JWT Authentication: Secure token-based authentication
BCrypt Password Encoding: Passwords are hashed before storage
Spring Security: Role-based access control
CORS Configuration: Cross-origin requests handled securely
🧪 Testing
Using Postman
Import the provided Postman collection
Set environment variables:
AUTH_URL: http://localhost:9000
LEAVE_URL: http://localhost:9001
Run the test scenarios in order
Manual Testing
See the API Documentation section for complete request examples.

🐛 Troubleshooting
Common Issues
1. Port Already in Use


bash
Error: Port 9000 is already in use
Solution: Kill the process or change the port in application.properties

2. Database Connection Error


bash
Error: Access denied for user 'root'@'localhost'
Solution: Check database credentials in application.properties

3. User Not Found Error


Failed to submit leave request. User may not exist.
Solution: Ensure the user exists in the auth service database

4. Invalid Status Error


Invalid status value: APPROVED
Solution: Use correct enum values (case-insensitive): Pending, Approved, Rejected, InProgress

🤝 Contributing
Contributions are welcome! Please follow these steps:

Fork the repository
Create a feature branch (git checkout -b feature/AmazingFeature)
Commit your changes (git commit -m 'Add some AmazingFeature')
Push to the branch (git push origin feature/AmazingFeature)
Open a Pull Request



Made with ❤️ using Spring Boot
