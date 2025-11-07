# 🪙 Wallet Management System — Spring Boot Project

### 🔧 Tech Stack
- **Spring Boot 3.2+**
- **MongoDB Atlas (or Local via Docker)**
- **Spring Security (JWT + OAuth2)**
- **Swagger / OpenAPI UI**
- **JUnit + Mockito + Testcontainers**
- **Dockerfile + docker-compose**

---

## ⚙️ 1️⃣ Project Setup

### **Clone or unzip project**
```bash
unzip wallet-management-project-updated.zip
cd wallet-management-project-updated
```

### **MongoDB options**
- **Option 1:** Use MongoDB Atlas (update `application.yml`)
- **Option 2:** Run local Mongo with Docker:
  ```bash
  docker-compose up -d
  ```

### **application.yml**
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/walletdb
server:
  port: 8080

security:
  jwt:
    secret: H!8kjs90@#2ZpQxYwF1mLrT5cV8bN0zSdGhPaKeRuTwXyZbCnMqJvFrDsLtPwEe
    expiration-ms: 3600000

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

---

## 🚀 2️⃣ Run the Application

### Using Maven
```bash
mvn spring-boot:run
```

### Or with Docker
```bash
docker build -t wallet-app .
docker run -p 8080:8080 wallet-app
```

---

## 🌐 3️⃣ Open Swagger UI

After starting the app, visit:  
👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

You’ll see:
- **AuthController** — for register/login/refresh  
- **WalletController** — for balance, credit, debit

---

## 🧭 4️⃣ API Workflow

### **1. Register User**
```
POST /auth/register
```
**Request:**
```json
{
  "username": "vishal",
  "email": "vishal@example.com",
  "password": "pass123"
}
```
**Response:**
```json
{ "status": "registered" }
```

### **2. Login**
```
POST /auth/login
```
**Request:**
```json
{
  "username": "vishal",
  "password": "pass123"
}
```
**Response:**
```json
{
  "accessToken": "<JWT_TOKEN>",
  "refreshToken": "<REFRESH_TOKEN>"
}
```

Use the JWT in Swagger “Authorize” →  
`Bearer <JWT_TOKEN>`

---

### **3. Get Wallet Balance**
```
GET /wallet/balance
```
**Response:**
```json
{ "balance": 0.0 }
```

### **4. Credit Wallet**
```
POST /wallet/credit
```
**Request:**
```json
{ "amount": 500 }
```
**Response:**
```json
{ "status": "credited", "balance": 500 }
```

### **5. Debit Wallet**
```
POST /wallet/debit
```
**Request:**
```json
{ "amount": 200 }
```
**Response:**
```json
{ "status": "debited", "balance": 300 }
```

---

### **6. Refresh Token**
```
POST /auth/refresh
```
**Request:**
```json
{ "refreshToken": "<REFRESH_TOKEN>" }
```
**Response:**
```json
{ "accessToken": "<NEW_JWT_TOKEN>" }
```

---

## 🧪 5️⃣ Test Coverage

- Unit Tests via **JUnit + Mockito**
- Integration Tests via **Testcontainers** (Mongo)
- Run with:
```bash
mvn test
```

---

## 🐳 6️⃣ Docker & Deployment

### **Build & Run Locally**
```bash
docker-compose up -d
docker build -t wallet-app .
docker run -p 8080:8080 wallet-app
```

### **Services**
| Service | Port | Description |
|----------|------|-------------|
| wallet-app | 8080 | Spring Boot API |
| mongo | 27017 | Local MongoDB (via docker-compose) |

---

## 💡 Highlights

| Feature | Description |
|----------|--------------|
| 🔐 JWT & Refresh Tokens | Secure authentication flow |
| ⚡ Atomic Balance Updates | Mongo `$inc` ensures race-free credit/debit |
| 📘 Swagger UI | Interactive documentation |
| 🧰 Testcontainers | Integration tests using real Mongo instance |
| ☁️ MongoDB Atlas | Cloud database integration |
| 🐳 Dockerized | Ready for container deployment |

---

## 👨‍💻 Author
**Vishal Kinge**  
Spring Boot | Java | MongoDB | JWT | OAuth2 | Docker  
