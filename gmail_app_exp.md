# 🎓 Project Interview Guide: Mail Automation (Personal Assistant)

This document provides a comprehensive guide for explaining your project and answering potential HR/Technical interview questions.

---

## 🚀 5-10 Minute Project Pitch

### 1. Project Title
**Personal Assistant - Gmail Automation**

### 2. The "Elevator Pitch" (1 min)
"I built an automated system that monitors your Gmail for high-stakes notifications like job interview invites, online assessments, and exam schedules. It extracts key details—like company name, date, and test links—and presents them in a clean, unified dashboard."

### 3. The "Why" (Problem Solved) (1.5 min)
*   **Email Fatigue**: As a student or job seeker, applying to dozens of companies results in an inbox flooded with newsletters, spam, and crucial invites.
*   **Missed Opportunities**: Manually scanning every email is error-prone. Missing one interview link can mean losing a job opportunity.
*   **Consolidation**: Having all dates and "action links" (Zoom, Test portals) in one dashboard saves time and reduces anxiety.

### 4. Tech Stack & Rationale (1.5 min)
*   **Backend**: Java Spring Boot.
    *   *Rationale*: Robust, handles background scheduling (`@Scheduled`) effortlessly, and has mature libraries for Google API integration.
*   **Frontend**: React (with Vite).
    *   *Rationale*: Modern, fast, and allows for a responsive dashboard that updates in real-time.
*   **Database**: PostgreSQL (Supabase).
    *   *Rationale*: Storing structured event data requires a reliable relational database. Supabase provides a cloud-hosted solution for easy deployment.
    *   *Note*: Using Supabase specifically allowed faster setup of a production-ready PostgreSQL instance.
*   **API**: Google Gmail API (OAuth 2.0).
    *   *Rationale*: Securely accessing user data without asking for their password through standard OAuth 2.0 flows.

### 5. Most Significant Challenges (2 min)
*   **Challenge 1: Unstructured Data Parsing**: Emails from various companies arrive in different formats.
    *   *Solution*: I used **Jsoup** for HTML sanitization to extract clean text and built a **Regex-based parser** that recognizes multiple date formats across different locales.
*   **Challenge 2: OAuth 2.0 Token Management**: Handling access token expiration to ensure the background poller keeps working.
    *   *Solution*: Implemented a service that uses the **Refresh Token** to automatically fetch new Access Tokens before they expire, ensuring 24/7 automation.

### 6. Future Scope (1 min)
*   Integrating **Generative AI (LLMs)** to understand the "intent" of an email more accurately than Regex.
*   Adding **Push Notifications** (WhatsApp/Telegram) for upcoming tests.

---

## 💼 Detailed HR & Technical Q&A

### Q1: "Can you walk me through your project?"
**Answer Focus**: Problem -> Solution -> Tech Stack.
> "My project, 'Personal Assistant,' is a productivity tool for job seekers. It solves the problem of 'inbox clutter' by automatically scanning Gmail for job-related events. It's built using Spring Boot and React. The system polls the Gmail API every few minutes, parses headers and body content, and stores them as structured 'Events' (Interview, Exam, Registration) in a PostgreSQL database."

### Q2: "Why did you choose this particular tech stack?"
**Answer Focus**: Architecture and reliability.
> "I chose Spring Boot for its strong ecosystem and built-in scheduling capabilities which were perfect for periodic email polling. React was my choice for the frontend because it provides a smooth, state-managed UI. PostgreSQL via Supabase ensured I had a scalable relational database with minimal infrastructure overhead."

### Q3: "What was the hardest technical challenge you faced?"
**Answer Focus**: Problem-solving skills.
> "The hardest part was **reliable data extraction**. Emails are mostly HTML, often messy. I had to learn how to use Jsoup to traverse the DOM and extract specific text without getting lost in CSS styling. Then, I had to write complex Regular Expressions to handle '24th Oct', 'October 24', and '2024-10-24' consistently."

### Q4: "How did you ensure the security of user data?"
**Answer Focus**: Security best practices.
> "Security was a priority. I implemented **OAuth 2.0**, so the app never sees your password. I store only necessary tokens. All sensitive configuration like Client Secrets and DB credentials are managed via environment variables (`.env`) and are never hardcoded."

### Q5: "How do you handle duplicates?"
**Answer Focus**: Data Integrity.
> "I leveraged the unique `messageId` provided by the Gmail API. Before saving an event, I perform an existence check in the database. This prevents duplicates even if the sync process is triggered multiple times for the same message."

### Q6: "Why did you choose PostgreSQL over MongoDB?"
**Answer Focus**: Structured vs Unstructured.
> "While emails are unstructured, the **Events** I extract (Date, Company, Link) are highly structured and have clear relationships. Using a relational database like PostgreSQL allowed me to enforce data types (like `LocalDateTime`) and ensures data integrity through schemas."

---

## 🛠️ Tech Stack Key Points
| Component | Technology | Rationale |
| :--- | :--- | :--- |
| **Backend** | Spring Boot 3 | Robust scheduling and REST API support. |
| **Frontend** | React (Vite) | Fast rendering and modern UI development. |
| **Auth** | OAuth 2.0 | Secure, industry-standard access control. |
| **Database** | PostgreSQL/Supabase | Reliable relational storage with easy cloud setup. |
| **Parsing** | Jsoup + Regex | Precise HTML cleaning and date extraction. |

---

## 📝 Tips for the Demo
*   **Dashboard View**: Show how different events are color-coded (Interviews vs Exams).
*   **The Sync Flow**: Mention that the app works in the background (via `@Scheduled`) so users don't have to manually check for updates.
*   **Actionable Links**: Highlight that you can join a meeting directly from the dashboard.
