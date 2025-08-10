# Medical Prescription App

## Overview
This project is a medical prescription management app consisting of a Kotlin Spring Boot backend and a Kotlin Android frontend. It allows users to upload prescription PDFs, perform OCR extraction, generate personalized medicine schedules using Google Gemini API, and receive medicine reminders. The backend uses MongoDB for storing user data, prescriptions, and schedules.

## Features
- User registration and login with JWT authentication
- Upload prescription PDF files
- OCR extraction of prescription text from PDFs
- Integration with Google Gemini API to generate personalized medicine schedules
- Store and manage schedules in MongoDB
- Android app displays schedules with editable medicine times and notifications for reminders
- Schedule archiving after completion

## Technologies Used
- **Backend:** Kotlin, Spring Boot, MongoDB, JWT, Google Gemini API
- **Frontend:** Kotlin, Android (Jetpack Compose)
- **Other:** PDF file upload, OCR (Tesseract or external OCR), REST APIs

## Getting Started

### Prerequisites
- Java 17+
- Kotlin
- Android Studio
- MongoDB instance (local or cloud)
- Google Gemini API access and credentials

### Backend Setup
1. Clone the repository:
   ```bash
   git clone <repo-url>
   ```
2. Configure MongoDB connection string in application.properties or application.yml.
3. Configure Google Gemini API keys and endpoints in application config.
4. Build and run the Spring Boot backend:
   ```bash
   ./gradlew bootRun
   ```
5. Backend runs on http://localhost:8080
| Method | Endpoint                | Description                   |
| ------ | ----------------------- | ----------------------------- |
| POST   | `/auth/register`        | Register a new user           |
| POST   | `/auth/login`           | User login, returns JWT token |
| POST   | `/schedule/upload`      | Upload prescription PDF       |
| GET    | `/schedule/my`          | Get current user’s schedules  |
| PATCH  | `/schedule/update/{id}` | Update a medicine schedule    |
| DELETE | `/schedule/delete/{id}` | Delete a medicine schedule    |
