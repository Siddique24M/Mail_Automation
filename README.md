# Personal Assistant - Gmail Automation

An automated reminder system that parses Gmail notifications for job applications, interviews, and exam schedules. Built with a React frontend, Spring Boot backend, and PostgreSQL database, leveraging the Gmail API for intelligent event extraction and management.

## 📋 Table of Contents
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Setup Instructions](#setup-instructions)
  - [1. Google Cloud Console Setup](#1-google-cloud-console-setup)
  - [2. Database Setup](#2-database-setup)
  - [3. Backend Setup](#3-backend-setup)
  - [4. Frontend Setup](#4-frontend-setup)
- [Running the Application](#running-the-application)
- [Usage](#usage)
- [Troubleshooting](#troubleshooting)

---

## ✨ Features

- 🔐 **OAuth 2.0 Authentication** - Secure Gmail integration using Google OAuth
- 📧 **Automatic Email Parsing** - Extracts job applications, interviews, and exam details
- 📅 **Event Dashboard** - View all your important events in one place
- 🔄 **Auto-Sync** - Periodic polling for new emails
- 👥 **Multi-Account Support** - Switch between multiple Gmail accounts
- 🎨 **Modern UI** - Clean, responsive React interface

---

## 📦 Prerequisites

Before setting up this project, ensure you have the following installed:

### Required Software
- **Java 21** or higher ([Download](https://www.oracle.com/java/technologies/downloads/))
- **Node.js 18+** and npm ([Download](https://nodejs.org/))
- **PostgreSQL 14+** ([Download](https://www.postgresql.org/download/))
- **Maven 3.8+** (usually comes with Java)
- **Git** ([Download](https://git-scm.com/))

### Verify Installation
```bash
java -version    # Should show Java 21+
node -v          # Should show v18+
npm -v           # Should show npm version
psql --version   # Should show PostgreSQL 14+
mvn -v           # Should show Maven 3.8+
```

---

## 🚀 Setup Instructions

### 1. Google Cloud Console Setup

You need to create a Google Cloud project and enable Gmail API to allow the application to access Gmail.

#### Step 1.1: Create a Google Cloud Project
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Click **"Select a project"** → **"New Project"**
3. Enter project name (e.g., "Personal-Assistant")
4. Click **"Create"**

#### Step 1.2: Enable Gmail API
1. In your project, go to **"APIs & Services"** → **"Library"**
2. Search for **"Gmail API"**
3. Click on it and press **"Enable"**

#### Step 1.3: Configure OAuth Consent Screen
1. Go to **"APIs & Services"** → **"OAuth consent screen"**
2. Select **"External"** user type → Click **"Create"**
3. Fill in the required fields:
   - **App name**: Personal Assistant
   - **User support email**: Your email
   - **Developer contact**: Your email
4. Click **"Save and Continue"**
5. On **Scopes** page, click **"Add or Remove Scopes"**
6. Add the following scopes:
   - `https://www.googleapis.com/auth/gmail.readonly`
   - `https://www.googleapis.com/auth/userinfo.email`
   - `https://www.googleapis.com/auth/userinfo.profile`
7. Click **"Update"** → **"Save and Continue"**
8. On **Test users** page, click **"Add Users"**
9. Add your Gmail address (the account you want to monitor)
10. Click **"Save and Continue"**

#### Step 1.4: Create OAuth 2.0 Credentials
1. Go to **"APIs & Services"** → **"Credentials"**
2. Click **"Create Credentials"** → **"OAuth client ID"**
3. Select **"Web application"**
4. Configure:
   - **Name**: Personal Assistant Web Client
   - **Authorized JavaScript origins**:
     - `http://localhost:5173` (for local frontend)
     - `http://localhost:9090` (for local backend)
   - **Authorized redirect URIs**:
     - `http://localhost:9090/login/oauth2/code/google`
5. Click **"Create"**
6. **IMPORTANT**: Copy the **Client ID** and **Client Secret** - you'll need these!

---

### 2. Database Setup

#### Step 2.1: Install PostgreSQL
If not already installed, download and install PostgreSQL from [postgresql.org](https://www.postgresql.org/download/)

#### Step 2.2: Create Database
Open PostgreSQL command line (psql) or pgAdmin and run:

```sql
-- Create database
CREATE DATABASE mail_assistant;

-- Connect to the database
\c mail_assistant;

-- Verify connection
SELECT current_database();
```

#### Step 2.3: Create Database User (Optional but Recommended)
```sql
-- Create a dedicated user for the application
CREATE USER assistant_user WITH PASSWORD 'your_secure_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE mail_assistant TO assistant_user;

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO assistant_user;
```

**Note**: The application will automatically create the required tables (`job_event` and `user_credential`) when it first runs, thanks to Hibernate's `ddl-auto=update` setting.

---

### 3. Backend Setup

#### Step 3.1: Clone the Repository
```bash
git clone <your-repository-url>
cd Personal-Assistant/backend
```

#### Step 3.2: Configure Environment Variables
Create a `.env` file in the `backend` directory:

```bash
# Copy the example file
cp .env.example .env
```

Edit the `.env` file with your actual credentials:

```properties
# Database Credentials
DB_USERNAME=postgres
DB_PASSWORD=your_postgresql_password

# Google OAuth Credentials (from Step 1.4)
GOOGLE_CLIENT_ID=your_client_id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your_client_secret
```

**Important**: 
- Replace `your_postgresql_password` with your PostgreSQL password
- Replace `your_client_id` and `your_client_secret` with values from Google Cloud Console
- **Never commit the `.env` file to Git** (it's already in `.gitignore`)

#### Step 3.3: Verify application.properties
The `backend/src/main/resources/application.properties` file should already be configured to use environment variables:

```properties
# Database Configuration
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/mail_assistant}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Google OAuth Configuration
google.client.client-id=${GOOGLE_CLIENT_ID}
google.client.client-secret=${GOOGLE_CLIENT_SECRET}
google.client.redirect-uri=${BACKEND_URL:http://localhost:9090}/login/oauth2/code/google

# Frontend URL
frontend.url=${FRONTEND_URL:http://localhost:5173}
```

#### Step 3.4: Build the Backend
```bash
# Windows
mvnw.cmd clean install

# Linux/Mac
./mvnw clean install
```

---

### 4. Frontend Setup

#### Step 4.1: Navigate to Frontend Directory
```bash
cd ../frontend
```

#### Step 4.2: Install Dependencies
```bash
npm install
```

#### Step 4.3: Configure Environment Variables
Create a `.env` file in the `frontend` directory:

```properties
VITE_API_URL=http://localhost:9090
```

This tells the frontend where to find the backend API.

---

## 🎯 Running the Application

### Start the Backend
```bash
cd backend

# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

The backend will start on `http://localhost:9090`

### Start the Frontend (in a new terminal)
```bash
cd frontend
npm run dev
```

The frontend will start on `http://localhost:5173`

### Access the Application
Open your browser and navigate to: **http://localhost:5173**

---

## 📖 Usage

### First Time Setup

1. **Open the Application**: Navigate to `http://localhost:5173`
2. **Login with Google**: Click the "Login with Google" button
3. **Authorize Access**: 
   - Select your Gmail account
   - Review permissions (read-only access to Gmail)
   - Click "Allow"
4. **Wait for Sync**: The application will automatically sync your emails
5. **View Dashboard**: See all extracted events (job applications, interviews, exams)

### Features

- **📊 Dashboard**: View all your events in a clean interface
- **🔄 Sync Button**: Manually trigger email sync
- **👤 User Info**: See which account is currently logged in
- **🔀 Switch Account**: Add and switch between multiple Gmail accounts
- **🔍 Filters**: Filter events by type (Job, Interview, Exam)

### How It Works

1. The application polls your Gmail every 5 minutes (configurable)
2. It searches for emails with keywords like:
   - "interview", "assessment", "test", "exam"
   - "registration", "application", "offer"
3. Extracts important information:
   - Company name
   - Event type
   - Date and time
   - Action links (Zoom, test links, etc.)
4. Stores events in the database
5. Displays them on your dashboard

---

## 🔧 Troubleshooting

### Common Issues

#### 1. "Access Denied" or OAuth Error
**Problem**: Google OAuth not configured correctly

**Solution**:
- Verify your Client ID and Client Secret in `.env`
- Check that redirect URI in Google Cloud Console matches: `http://localhost:9090/login/oauth2/code/google`
- Ensure your Gmail account is added as a test user in OAuth consent screen

#### 2. Database Connection Error
**Problem**: Cannot connect to PostgreSQL

**Solution**:
- Verify PostgreSQL is running: `pg_isready`
- Check database exists: `psql -l | grep mail_assistant`
- Verify credentials in `.env` file
- Ensure database URL is correct in `application.properties`

#### 3. "Port Already in Use"
**Problem**: Port 9090 or 5173 is already occupied

**Solution**:
```bash
# Windows - Find and kill process
netstat -ano | findstr :9090
taskkill /PID <process_id> /F

# Linux/Mac
lsof -ti:9090 | xargs kill -9
```

Or change the port in `application.properties` (backend) or `vite.config.js` (frontend)

#### 4. No Emails Being Synced
**Problem**: Emails not appearing in dashboard

**Solution**:
- Check backend logs for errors
- Verify Gmail API is enabled in Google Cloud Console
- Ensure you have emails matching the search criteria
- Check the `EmailPoller.java` scheduler is running (logs should show "Polling emails...")

#### 5. Build Errors
**Problem**: Maven or npm build fails

**Solution**:
```bash
# Backend - clean and rebuild
mvnw.cmd clean install -U

# Frontend - clear cache and reinstall
rm -rf node_modules package-lock.json
npm install
```

---

## 📁 Project Structure

```
Personal-Assistant/
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/personal/assistant/
│   │       │   ├── controller/      # REST API endpoints
│   │       │   ├── entity/          # Database models
│   │       │   ├── repository/      # Data access layer
│   │       │   ├── service/         # Business logic
│   │       │   └── scheduler/       # Email polling
│   │       └── resources/
│   │           └── application.properties
│   ├── .env                         # Environment variables (create this)
│   ├── .env.example                 # Template for .env
│   └── pom.xml                      # Maven dependencies
├── frontend/
│   ├── src/
│   │   ├── components/              # React components
│   │   ├── pages/                   # Page components
│   │   └── App.jsx                  # Main app component
│   ├── .env                         # Frontend environment variables
│   └── package.json                 # npm dependencies
└── README.md                        # This file
```

---

## 🔒 Security Notes

- **Never commit** `.env` files to version control
- **Keep your OAuth credentials secret**
- The application only requests **read-only** access to Gmail
- User credentials are stored encrypted in the database
- Use strong passwords for your database

---

## 🤝 Contributing

Feel free to fork this project and submit pull requests for improvements!

---

## 📄 License

This project is for educational purposes. Ensure compliance with Google's API Terms of Service.

---

## 📞 Support

If you encounter issues:
1. Check the [Troubleshooting](#troubleshooting) section
2. Review backend logs in the terminal
3. Check browser console for frontend errors
4. Verify all environment variables are set correctly

---

**Happy Automating! 🚀**
