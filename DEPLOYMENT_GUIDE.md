# 🚀 Complete MySQL Deployment Guide for Render

## Step-by-Step Instructions

### 📋 **Prerequisites**
- GitHub account (✅ Done - code is pushed)
- Render account (free)
- MySQL cloud database (choose one option below)

---

## 🗄️ **STEP 1: Set Up MySQL Database**

### **Option A: Aiven MySQL (RECOMMENDED - FREE)**

1. **Sign Up**
   - Go to https://aiven.io/
   - Click "Sign Up"
   - Use your email or GitHub

2. **Create MySQL Service**
   - Click "Create Service"
   - Select "MySQL"
   - Choose **"Startup-4" plan (FREE)**
   - Select region closest to you
   - Give it a name (e.g., `classroom-support-db`)
   - Click "Create Service"

3. **Wait for Database to Start**
   - Takes 2-3 minutes
   - Status will change to "Running"

4. **Get Connection Details**
   - Click on your database
   - Go to "Overview" tab
   - Copy these details:
     ```
     Host: mysql-xxxxx-yyyy.aivencloud.com
     Port: 13123
     User: avnadmin
     Password: [auto-generated - click to reveal]
     Database: defaultdb
     ```

5. **Create Connection String**
   ```
   jdbc:mysql://mysql-xxxxx-yyyy.aivencloud.com:13123/defaultdb?ssl-mode=REQUIRED
   ```

### **Option B: PlanetScale (FREE)**

1. Go to https://planetscale.com/
2. Sign up with GitHub
3. Click "New Database"
4. Name it: `classroom-support`
5. Select region
6. Click "Create database"
7. Go to "Connect" → "Generate new password"
8. Select "Java" format
9. Copy the JDBC connection string

---

## 🌐 **STEP 2: Deploy on Render**

1. **Go to Render**
   - https://render.com/
   - Sign up/Login with GitHub

2. **Create New Web Service**
   - Click "New +" → "Web Service"
   - Click "Connect account" (if first time)
   - Select repository: `support-system-eac`
   - Click "Connect"

3. **Configure Service**
   - **Name**: `classroom-support-system`
   - **Region**: Oregon (US West) or closest
   - **Branch**: `main`
   - **Root Directory**: Leave blank
   - **Environment**: Java
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -Dspring.profiles.active=prod -jar target/customer-service-aims.jar`
   - **Instance Type**: Free

4. **Add Environment Variables**
   Click "Advanced" → "Add Environment Variable":

   ```
   SPRING_PROFILES_ACTIVE = prod
   
   SENDGRID_API_KEY = your_sendgrid_api_key_here
   
   DB_URL = jdbc:mysql://YOUR-MYSQL-HOST:PORT/DATABASE?ssl-mode=REQUIRED
   DB_USERNAME = your_mysql_username
   DB_PASSWORD = your_mysql_password
   
   ADMIN_PASSWORD = SecurePassword123!
   ```

   **Replace with your actual values from Step 1!**

5. **Create Web Service**
   - Click "Create Web Service"
   - Render will start building (takes 5-10 minutes)

6. **Wait for Deployment**
   - Watch the build logs
   - When you see "Started WelcomeApplication"
   - Your app is live! 🎉

7. **Get Your URL**
   - Render gives you: `https://classroom-support-system.onrender.com`
   - Visit it to test!

---

## ✅ **STEP 3: Verify Deployment**

1. **Check Build Logs**
   - Should see: "Started WelcomeApplication"
   - No errors about database connection

2. **Test Application**
   - Visit your Render URL
   - Try to register a new user
   - Check if email is sent

3. **Check Database**
   - Go back to Aiven/PlanetScale
   - Verify tables were created

---

## 🐛 **Troubleshooting**

### **Build Fails**
- Check build logs in Render
- Make sure Java 21 is detected

### **Database Connection Error**
- Verify DB_URL format is correct
- Check username/password
- Ensure database allows connections from Render's IP

### **Application Crashes**
- Check "Logs" tab in Render
- Look for errors
- Verify all environment variables are set

---

## 📝 **Important Notes**

### **Free Tier Limitations:**
- ⏰ Render free tier sleeps after 15 min of inactivity
- 🔄 First request after sleep takes ~30 seconds
- 💾 Database resets monthly (Aiven free tier)

### **SSL/HTTPS:**
- ✅ Render provides automatic HTTPS
- ✅ Your app URLs are secure
- ✅ Production profile disables app-level SSL

### **WebRTC Voice Calls:**
- ⚠️ May not work on free tier (needs persistent WebSocket)
- ✅ Chat and basic features will work
- 💰 Consider paid tier for full voice call support

---

## 🎓 **For Your Professor**

**Database**: MySQL (cloud-hosted)
**Platform**: Render (PaaS)
**Build Tool**: Maven
**Framework**: Spring Boot 3.5.7
**Java Version**: 21
**Security**: HTTPS enabled, environment variables for secrets

---

## 📞 **Need Help?**

1. Check Render logs
2. Check Aiven/PlanetScale status
3. Verify environment variables
4. Test database connection separately

**Your app URL will be**: `https://classroom-support-system.onrender.com`

🎉 **Good luck with your deployment!**
