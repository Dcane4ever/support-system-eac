# 🚀 How to Build and Run the Application

## 📦 Quick Start (3 Easy Steps)

### Step 1: Build the JAR File
**Double-click:** `build.bat`

This will:
- Clean old builds
- Compile the project
- Create `customer-service-aims.jar` in the `target` folder
- Takes 2-5 minutes

### Step 2: Run the Application
**Double-click:** `run.bat`

This will:
- Start the Spring Boot application
- Open on port 8080
- Keep the console window open

### Step 3: Open in Browser
Go to: **http://localhost:8080**

---

## 📋 Requirements

✅ **Java 21** must be installed
✅ **Maven** must be installed
✅ **MySQL** database running (or H2 in-memory)

---

## 🔧 Manual Commands (Alternative)

### Build JAR:
```bash
mvn clean package
```

### Run JAR:
```bash
java -jar target/customer-service-aims.jar
```

### Run with specific profile:
```bash
java -jar target/customer-service-aims.jar --spring.profiles.active=prod
```

---

## 📂 File Locations

After building, you'll find:

```
target/
  └── customer-service-aims.jar  ← Your runnable JAR (double-clickable!)
```

**JAR File Size:** ~50-70 MB (includes all dependencies)

---

## 🎯 Features Included

✅ WebSocket support for real-time chat
✅ WebRTC voice call functionality
✅ Email verification system
✅ User authentication (students, teachers, agents)
✅ Help topic management
✅ Session tracking
✅ Embedded Tomcat server

---

## 🌐 Accessing the Application

### Login Pages:
- **Students:** http://localhost:8080/login
- **Teachers:** http://localhost:8080/login
- **Agents:** http://localhost:8080/login

### Default Users:
Check `data.sql` for pre-configured users

---

## ⚙️ Configuration

### Database Settings:
Edit `src/main/resources/application.properties`

**For H2 (default):**
```properties
spring.datasource.url=jdbc:h2:mem:testdb
```

**For MySQL:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/customer_service
spring.datasource.username=root
spring.datasource.password=yourpassword
```

### Port Settings:
```properties
server.port=8080
```

Change to 9090 or any other port if needed.

---

## 🐛 Troubleshooting

### "JAR file not found"
→ Run `build.bat` first to create the JAR

### "Port 8080 already in use"
→ Stop other applications using port 8080
→ Or change port in `application.properties`

### "Java not found"
→ Install Java 21 and add to PATH
→ Verify: `java -version`

### "Maven not found"
→ Install Maven and add to PATH
→ Verify: `mvn -version`

### Application won't start
→ Check console output for errors
→ Verify database is running
→ Check `application.properties` settings

---

## 📦 Distribution

To share your application:

1. **Copy the JAR file:**
   ```
   target/customer-service-aims.jar
   ```

2. **Include application.properties** (if needed)

3. **Recipients need:**
   - Java 21 installed
   - MySQL running (or use H2)

4. **They run:**
   ```bash
   java -jar customer-service-aims.jar
   ```

---

## 🚀 Production Deployment

### Cloud Deployment:
```bash
# Azure
az webapp deploy --src-path target/customer-service-aims.jar

# AWS
eb deploy

# Heroku
heroku deploy:jar target/customer-service-aims.jar
```

### Docker:
```bash
docker build -t customer-service-aims .
docker run -p 8080:8080 customer-service-aims
```

---

## 📝 Notes

- JAR file is **self-contained** (includes Tomcat)
- **No external server needed**
- Works on Windows, Mac, Linux
- **WebRTC works** with embedded Tomcat
- **Voice calls work** over localhost
- For cloud deployment, **HTTPS required**

---

## 🎉 Success!

If you see this in the console:
```
Started WelcomeApplication in X.XXX seconds
```

✅ Application is ready!
✅ Go to: http://localhost:8080
✅ Voice calls work!
✅ WebSocket connected!

---

## 📞 Support

For issues:
1. Check console output
2. Review error messages
3. Verify all requirements met
4. Check database connection

---

**Enjoy your Customer Service AIMS application with voice call support!** 🎉📞
