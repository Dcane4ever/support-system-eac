# Quick Start Guide - EAC Classroom Support System

## 🚀 Get Started in 5 Minutes

### Step 1: Build the Project
Open PowerShell in the project directory and run:
```powershell
.\mvnw.cmd clean install
```

### Step 2: Configure Email (Optional for now)
For email verification to work, update `src/main/resources/application.properties`:
```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-gmail-app-password
```

**Skip this for now** - You can still register and login, just email verification won't work.

### Step 3: Run the Application
```powershell
.\mvnw.cmd spring-boot:run
```

Wait for the message: `Started WelcomeApplication in X seconds`

### Step 4: Open Your Browser
Go to: **http://localhost:8080**

### Step 5: Register a New Account

1. Click **"Get Started"** or **"Sign In"** → **"Create New Account"**
2. Fill in the registration form:
   - **Role**: Choose Student or Teacher
   - **Full Name**: Your name
   - **Student ID**: (Only for students, e.g., 2021-12345)
   - **Username**: Choose a username
   - **Email**: Your email (you can use any email even if verification doesn't work)
   - **Password**: At least 6 characters

3. Click **"Create Account"**

### Step 6: Login

Since email verification might not be configured yet, you need to manually verify your account:

**Option A: Disable Email Verification (Quick Fix)**

Update `SecurityConfig.java` line 36 to allow unverified users to login temporarily.

**Option B: Manually Verify in Database**

1. Access H2 Console: http://localhost:8080/h2-console
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Username: `sa`
   - Password: (leave blank)

2. Run this SQL:
```sql
UPDATE users SET email_verified = true WHERE username = 'your-username';
```

**Option C: Configure Gmail (Recommended)**

See main README.md for full email setup instructions.

### Step 7: Explore the System

After logging in, you'll be redirected to your dashboard based on your role:

**Student Dashboard** (`/student/dashboard`):
- View classes
- Check assignments
- Access support chat
- View grades

**Teacher Dashboard** (`/teacher/dashboard`):
- Create classrooms
- Manage students
- Create assignments
- Provide support

## 🎨 System Features

### Landing Page
- Clean EAC-branded design with red color scheme
- Feature showcase
- Quick access to chat support
- Login and registration links

### Authentication
- ✅ User registration with role selection
- ✅ Email verification system
- ✅ Secure password encryption (BCrypt)
- ✅ Role-based access control
- ✅ Session management

### User Roles
- **STUDENT**: Access classes and assignments
- **TEACHER**: Create and manage classrooms
- **SUPPORT_AGENT**: Provide chat support
- **ADMIN**: System administration

## 🛠️ Project Structure

```
src/main/
├── java/com/cusservice/bsit/
│   ├── config/                    # Security & WebSocket config
│   ├── controller/                # Web controllers
│   ├── model/                     # Database entities
│   ├── repository/                # Data access
│   ├── service/                   # Business logic
│   └── WelcomeApplication.java   # Main application
└── resources/
    ├── application.properties     # Configuration
    └── templates/                 # HTML pages
        ├── landing.html           # Homepage
        ├── login.html             # Login page
        ├── register.html          # Registration
        ├── student-dashboard.html # Student view
        └── teacher-dashboard.html # Teacher view
```

## 📦 Dependencies Added

- Spring Boot Web
- Spring Boot Security
- Spring Boot Data JPA
- Spring Boot Thymeleaf
- Spring Boot WebSocket
- Spring Boot Mail
- MySQL Connector
- H2 Database
- Lombok
- WebJars (SockJS, STOMP)

## 🔍 Testing the System

### Test Student Registration
1. Go to http://localhost:8080
2. Click "Get Started"
3. Select "Student" role
4. Fill in: Name, Student ID, Username, Email, Password
5. Register and login

### Test Teacher Registration
1. Same process but select "Teacher" role
2. No Student ID required

### Test Authentication
1. Try accessing `/student/dashboard` without logging in → Redirected to login
2. Login as student → Access granted to student dashboard
3. Try accessing `/teacher/dashboard` as student → Access denied

## 🎯 Next Steps to Implement

### 1. Fix Email Verification (Priority 1)
- Configure Gmail SMTP in application.properties
- Test registration flow end-to-end

### 2. Implement Chat System (Priority 2)
From your original project, copy:
- Chat controllers
- WebSocket message handlers
- Chat UI templates

### 3. Implement Classroom Management (Priority 3)
- Create classroom functionality
- Join classroom with code
- Student enrollment

### 4. Add Assignments (Priority 4)
- Create assignments
- Submit work
- Grade submissions

## 🐛 Common Issues

### Port 8080 Already in Use
```properties
# Change in application.properties
server.port=8081
```

### Build Fails
```powershell
.\mvnw.cmd clean
.\mvnw.cmd install -DskipTests
```

### Cannot Login After Registration
The email isn't verified. Use H2 console to manually verify:
```sql
UPDATE users SET email_verified = true;
```

### Missing Dependencies
```powershell
.\mvnw.cmd dependency:resolve
```

## 📞 Support

Check the main README.md for detailed documentation.

---

**Built for EAC - Emilio Aguinaldo College** 🎓
