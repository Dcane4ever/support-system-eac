# Classroom Support System

A comprehensive classroom support system with real-time chat, voice calling, and student-teacher interaction features.

## Features

- 🔐 User Authentication (Student, Teacher, Support Agent)
- 💬 Real-time Chat with WebSocket
- 📞 WebRTC Voice Calling
- 📧 Email Verification (SendGrid)
- 🔒 HTTPS Support
- 📊 Support Queue System

## Tech Stack

- **Backend**: Spring Boot 3.5.7, Java 21
- **Database**: MySQL
- **Real-time**: WebSocket, STOMP
- **Email**: SendGrid API
- **Security**: Spring Security, SSL/TLS

## Environment Variables

### For Local Development:
Set these in `.env` file:
```
SENDGRID_API_KEY=your_sendgrid_api_key
DB_URL=jdbc:mysql://localhost:3306/classroom_support?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=
```

### For Render Production:
Set these in Render dashboard:
```
SENDGRID_API_KEY=your_sendgrid_api_key
DB_URL=your_mysql_connection_string
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
ADMIN_PASSWORD=your_secure_admin_password
```

## MySQL Database Setup

For production deployment, you need a cloud MySQL database. Options:

1. **Aiven MySQL** (FREE tier available)
   - Sign up: https://aiven.io/
   - Create MySQL service (Free Startup-4 plan)
   - Get connection string

2. **PlanetScale** (FREE tier available)
   - Sign up: https://planetscale.com/
   - Create database
   - Get connection string

3. **Railway** (Free trial)
   - Sign up: https://railway.app/
   - Provision MySQL
   - Get connection details

## Render Deployment

1. Connect your GitHub repository to Render
2. Select "Web Service"
3. Build Command: `./mvnw clean package -DskipTests`
4. Start Command: `java -jar target/customer-service-aims.jar`
5. Add environment variables in Render dashboard

## Local Development

1. Copy `.env.example` to `.env`
2. Fill in your environment variables
3. Run: `./mvnw spring-boot:run` or `start.bat`
4. Access: `https://localhost:8443`

## License

Private Educational Project
