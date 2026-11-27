# 🎬 Cine Cinema — Postman API Testing Guide

> **Complete guide for importing, configuring, and running every API test in the Cine Cinema backend.**
>
> **Project Stack:** Java 21 · Spring Boot 4.1.2 · Gradle · PostgreSQL (Docker) · Apache Kafka  
> **Backend URL:** `http://localhost:8082`

---

## Table of Contents

1. [A. How to Import the Postman Files](#a-how-to-import-the-postman-files)
2. [B. How to Start the Cinema Backend](#b-how-to-start-the-cinema-backend)
3. [C. How to Execute Each Test](#c-how-to-execute-each-test)
4. [D. Postman Collection Runner](#d-postman-collection-runner)
5. [E. Troubleshooting](#e-troubleshooting)
6. [F. Sinhala Notes (සිංහල උපදෙස්)](#f-sinhala-notes-සිංහල-උපදෙස්)
7. [G. Workflow Guides](#g-workflow-guides)
8. [H. Test Image Setup for Poster Testing](#h-test-image-setup-for-poster-testing)

---

## A. How to Import the Postman Files

### Prerequisites

| Requirement | Details |
|---|---|
| Postman Desktop | Download from [postman.com/downloads](https://www.postman.com/downloads/) (v10+ recommended) |
| Docker | Running with PostgreSQL container on port `5432` |
| Kafka Broker | Available at `10.20.11.179:9092` (needed only for reservation/catalog tests) |
| Java 21 | Installed and on PATH |
| Test Images | Already included in `test_image/test.jpeg` and `test_image/test2.jpeg` |

### Files You Will Import

| File | Purpose |
|---|---|
| `Cine_Cinema_Complete_API_Tests.postman_collection.json` | All API requests organized into folders with test scripts |
| `Cine_Cinema_Local.postman_environment.json` | Environment variables (`baseUrl`, `token`, IDs, etc.) |

Both files are located in this `postman/` directory.

---

### Step-by-Step Import Instructions

#### Step 1 — Open Postman

Launch the Postman desktop application. If this is your first time, create a free account or skip sign-in to use the Scratch Pad (offline mode).

#### Step 2 — Import the Collection

1. Click the **Import** button in the top-left area of Postman (or press `Ctrl+O`).
2. A dialog box titled **"Import"** appears.
3. Click **"Upload Files"** (or drag-and-drop the file).
4. Navigate to this directory:
   ```
   cinema-app-cinema-side/postman/
   ```
5. Select **`Cine_Cinema_Complete_API_Tests.postman_collection.json`**.
6. Click **Open**.
7. Postman shows a preview of the collection. Click **Import** to confirm.
8. ✅ You should now see **"Cine Cinema Complete API Tests"** in your Collections sidebar on the left.

#### Step 3 — Import the Environment

1. Click **Import** again (`Ctrl+O`).
2. Click **"Upload Files"**.
3. Select **`Cine_Cinema_Local.postman_environment.json`** from the same `postman/` directory.
4. Click **Open**, then **Import**.
5. ✅ The environment is now available in Postman.

#### Step 4 — Select the Environment

1. Look at the **top-right corner** of Postman — there is an environment dropdown (it may say "No Environment").
2. Click the dropdown and select **"Cine Cinema Local"**.
3. ✅ The environment is now active. All `{{baseUrl}}`, `{{token}}`, and other variables will resolve automatically.

#### Step 5 — Verify and Configure Environment Variables

1. Click the **eye icon** (👁) next to the environment dropdown to quick-view the variables.
2. Or click **Environments** in the left sidebar → select **"Cine Cinema Local"** to edit.
3. Verify these variables are set:

| Variable | Initial Value | Description |
|---|---|---|
| `baseUrl` | `http://localhost:8082` | Backend server URL |
| `token` | *(empty — set automatically after login)* | JWT Bearer token |
| `cinemaEmail` | `cinema@example.com` | Registration/login email |
| `cinemaPassword` | `password123` | Registration/login password |
| `cinemaName` | `Cine Cinema Colombo` | Cinema name for registration |
| `cinemaAddress` | `123 Galle Road, Colombo 03` | Cinema address |
| `movieId` | *(empty — set automatically)* | Created movie ID |
| `showtimeId` | *(empty — set automatically)* | Created showtime ID |
| `reservationShowtimeId` | *(empty — set automatically)* | Showtime ID used for reservation tests |

> **Note:** You can change `cinemaEmail`, `cinemaPassword`, `cinemaName`, and `cinemaAddress` to whatever values you prefer. The test scripts will use whatever is configured here.

#### Step 6 — Start the Backend Services

Before running any tests, you need three things running:

1. **PostgreSQL** (Docker):
   ```bash
   # If using docker-compose in the project:
   docker compose up -d
   
   # Or manually:
   docker run -d --name cinema-postgres \
     -e POSTGRES_DB=cinema_db \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 \
     postgres:latest
   ```

2. **Kafka** (if testing reservations/catalog): Ensure the broker at `10.20.11.179:9092` is accessible from your machine.

3. **Spring Boot** (see [Section B](#b-how-to-start-the-cinema-backend) for all startup modes).

#### Step 7 — Run Authentication Requests First

1. Expand the collection in the sidebar: **Cine Cinema Complete API Tests**.
2. Open folder **`00 - Health / Smoke`** (if present) or go directly to **`01 - Authentication`**.
3. Run **Register Cinema** first, then **Login**.
4. The Login request's test script automatically saves the JWT token to `{{token}}`.
5. ✅ All subsequent authenticated requests will use this token automatically.

#### Step 8 — Run Remaining Folders in Order

Execute the folders in numerical order:

```
00 - Health / Smoke         → Verify server is up
01 - Authentication         → Register + Login (sets {{token}})
02 - Cinema Profile         → GET/PUT profile (needs {{token}})
03 - Movies                 → CRUD movies (sets {{movieId}})
04 - Movie Posters          → Upload/replace/delete poster (needs {{movieId}})
05 - Showtimes              → CRUD showtimes (needs {{movieId}}, sets {{showtimeId}})
06 - Seat Availability      → Check seats (needs {{showtimeId}})
07 - Showtime Status Toggle → Activate/deactivate (sets {{reservationShowtimeId}})
08 - Reservations (Dev)     → Process reservation (needs dev profile)
09 - Catalog (Dev)          → Publish catalog (needs dev profile)
10 - Cleanup / Negative     → Error cases, delete operations
```

---

## B. How to Start the Cinema Backend

All commands assume you are in the project root directory:

```bash
cd "/home/isuru-dananjaya-sampath/Desktop/Cine Cinema/cinema-app-cinema-side"
```

### Normal Mode (Standard API Testing)

```bash
cd backend && ./gradlew bootRun
```

- Starts the application on port **8082**.
- All endpoints under `/api/auth/`, `/api/cinema/`, `/api/movies/`, `/api/showtimes/` are available.
- **Dev endpoints (`/api/dev/*`) will return 404.**

### Dev Mode (Enables Dev Endpoints)

```bash
cd backend && ./gradlew bootRun --args='--spring.profiles.active=dev'
```

- Enables the `dev` Spring profile.
- Unlocks:
  - `POST /api/dev/reservations` — Process a reservation
  - `POST /api/dev/catalog/publish` — Publish catalog to Kafka
- Required for folders **08** and **09** in the Postman collection.

### Dev Mode + Kafka Consumer (Full Integration)

```bash
cd backend && ./gradlew bootRun --args='--spring.profiles.active=dev --app.kafka.reservation.consumer-enabled=true'
```

- Everything from Dev Mode, **plus** the Kafka reservation consumer is activated.
- The app will listen to the Kafka reservation topic and process incoming reservation messages.
- Use this when testing the full reservation flow end-to-end with Kafka.

### Which Endpoints Require Dev Profile?

| Endpoint | Dev Profile Required? |
|---|---|
| `POST /api/dev/reservations` | ✅ Yes |
| `POST /api/dev/catalog/publish` | ✅ Yes |
| `POST /api/dev/catalog/publish-live/{showtimeId}` | ✅ Yes (but NOT IMPLEMENTED — Lesson 1.70) |
| All other `/api/*` endpoints | ❌ No |

> **If you call a `/api/dev/*` endpoint without the dev profile active, the server will return `404 Not Found`** because the controller beans are not registered.

---

## C. How to Execute Each Test

### 00 — Health / Smoke Tests

| Property | Details |
|---|---|
| **Purpose** | Verify the backend server is running and reachable |
| **Auth Required** | ❌ No |
| **Modifies Database** | ❌ No |

#### Request: Health Check
- **Method:** `GET`
- **URL:** `{{baseUrl}}/actuator/health` (or simply `{{baseUrl}}`)
- **Expected Response:** `200 OK`
- **Test Assertion:** Status code is 200

---

### 01 — Authentication

#### Request: Register Cinema Account

| Property | Details |
|---|---|
| **Purpose** | Create the one-and-only cinema account in the system |
| **Method** | `POST` |
| **URL** | `{{baseUrl}}/api/auth/register` |
| **Auth Required** | ❌ No (public) |
| **Modifies Database** | ✅ Yes — inserts into `cinemas` table |
| **Env Variables Needed** | `baseUrl`, `cinemaName`, `cinemaAddress`, `cinemaEmail`, `cinemaPassword` |

**Request Body:**
```json
{
    "name": "{{cinemaName}}",
    "address": "{{cinemaAddress}}",
    "email": "{{cinemaEmail}}",
    "password": "{{cinemaPassword}}"
}
```

**Success Response (201 Created):**
```json
{
    "id": 1,
    "name": "Cine Cinema Colombo",
    "address": "123 Galle Road, Colombo 03",
    "email": "cinema@example.com"
}
```

**Negative Cases:**

| Scenario | Status | Response |
|---|---|---|
| Duplicate email | `409 Conflict` | `{"message": "Cinema with this email already exists", ...}` |
| Cinema already exists | `409 Conflict` | `{"message": "Only one cinema account is allowed", ...}` |
| Missing required fields | `400 Bad Request` | `{"name": "Name is required", "email": "Email is required", ...}` |
| Invalid email format | `400 Bad Request` | `{"email": "Must be a valid email address"}` |
| Empty password | `400 Bad Request` | `{"password": "Password is required"}` |

**Test Assertions:**
- Status code is 201
- Response has `id`, `name`, `address`, `email`
- No `password` field in response
- `email` matches what was sent

---

#### Request: Login

| Property | Details |
|---|---|
| **Purpose** | Authenticate and obtain a JWT token |
| **Method** | `POST` |
| **URL** | `{{baseUrl}}/api/auth/login` |
| **Auth Required** | ❌ No (public) |
| **Modifies Database** | ❌ No |
| **Env Variables Needed** | `baseUrl`, `cinemaEmail`, `cinemaPassword` |
| **Sets Env Variables** | ✅ `token` (automatically via test script) |

**Request Body:**
```json
{
    "email": "{{cinemaEmail}}",
    "password": "{{cinemaPassword}}"
}
```

**Success Response (200 OK):**
```json
{
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer"
}
```

**Post-request Test Script** (auto-saves token):
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.accessToken);
}
```

**Negative Cases:**

| Scenario | Status | Response |
|---|---|---|
| Wrong password | `401 Unauthorized` | `{"message": "Authentication required", ...}` |
| Non-existent email | `401 Unauthorized` | `{"message": "Authentication required", ...}` |
| Missing fields | `400 Bad Request` | Validation error map |

**Test Assertions:**
- Status code is 200
- Response has `accessToken` (non-empty string)
- `tokenType` equals `"Bearer"`

---

### 02 — Cinema Profile

#### Request: Get Cinema Profile

| Property | Details |
|---|---|
| **Purpose** | Retrieve the current cinema's profile information |
| **Method** | `GET` |
| **URL** | `{{baseUrl}}/api/cinema/profile` |
| **Auth Required** | ✅ Yes — `Authorization: Bearer {{token}}` |
| **Modifies Database** | ❌ No |
| **Env Variables Needed** | `baseUrl`, `token` |

**Success Response (200 OK):**
```json
{
    "id": 1,
    "name": "Cine Cinema Colombo",
    "address": "123 Galle Road, Colombo 03",
    "email": "cinema@example.com"
}
```

**Negative Cases:**

| Scenario | Status |
|---|---|
| No token provided | `401 Unauthorized` |
| Invalid/expired token | `401 Unauthorized` |

**Test Assertions:**
- Status code is 200
- Response has `id`, `name`, `address`, `email`
- No `password` field returned

---

#### Request: Update Cinema Profile

| Property | Details |
|---|---|
| **Purpose** | Update the cinema's name, address, or email |
| **Method** | `PUT` |
| **URL** | `{{baseUrl}}/api/cinema/profile` |
| **Auth Required** | ✅ Yes — `Authorization: Bearer {{token}}` |
| **Modifies Database** | ✅ Yes — updates `cinemas` table |
| **Env Variables Needed** | `baseUrl`, `token` |

**Request Body:**
```json
{
    "name": "Cine Cinema Kandy",
    "address": "456 Peradeniya Road, Kandy",
    "email": "kandy@cinecinema.lk"
}
```

**Success Response (200 OK):** Updated profile object.

**Negative Cases:**

| Scenario | Status |
|---|---|
| Missing name | `400 Bad Request` |
| Invalid email | `400 Bad Request` |
| No token | `401 Unauthorized` |

**Test Assertions:**
- Status code is 200
- Updated fields match the request body

---

### 03 — Movies

#### Request: Create Movie

| Property | Details |
|---|---|
| **Purpose** | Add a new movie to the cinema's catalog |
| **Method** | `POST` |
| **URL** | `{{baseUrl}}/api/movies` |
| **Auth Required** | ✅ Yes |
| **Modifies Database** | ✅ Yes — inserts into `movies` table |
| **Env Variables Needed** | `baseUrl`, `token` |
| **Sets Env Variables** | ✅ `movieId` (automatically via test script) |

**Request Body:**
```json
{
    "title": "Inception",
    "description": "A mind-bending thriller by Christopher Nolan",
    "durationMinutes": 148
}
```

**Success Response (201 Created):**
```json
{
    "id": 1,
    "title": "Inception",
    "description": "A mind-bending thriller by Christopher Nolan",
    "durationMinutes": 148
}
```

**Post-request Test Script** (auto-saves movieId):
```javascript
if (pm.response.code === 201) {
    var jsonData = pm.response.json();
    pm.environment.set("movieId", jsonData.id);
}
```

**Negative Cases:**

| Scenario | Status | Response |
|---|---|---|
| Missing title | `400 Bad Request` | `{"title": "Title is required"}` |
| Negative duration | `400 Bad Request` | `{"durationMinutes": "Must be positive"}` |
| No auth token | `401 Unauthorized` | Auth error |

**Test Assertions:**
- Status code is 201
- Response has `id`, `title`, `description`, `durationMinutes`
- `title` matches request

---

#### Request: List All Movies

| Property | Details |
|---|---|
| **Purpose** | Retrieve all movies |
| **Method** | `GET` |
| **URL** | `{{baseUrl}}/api/movies` |
| **Auth Required** | ✅ Yes |
| **Modifies Database** | ❌ No |

**Success Response (200 OK):** Array of movie objects.

**Test Assertions:**
- Status code is 200
- Response is an array
- Each element has `id`, `title`, `description`, `durationMinutes`

---

#### Request: Get Movie by ID

| Property | Details |
|---|---|
| **Purpose** | Retrieve a specific movie |
| **Method** | `GET` |
| **URL** | `{{baseUrl}}/api/movies/{{movieId}}` |
| **Auth Required** | ✅ Yes |
| **Modifies Database** | ❌ No |
| **Env Variables Needed** | `baseUrl`, `token`, `movieId` |

**Success Response (200 OK):** Single movie object.

**Negative Cases:**

| Scenario | Status |
|---|---|
| Non-existent ID (e.g., 99999) | `404 Not Found` |
| Invalid ID format (e.g., "abc") | `400 Bad Request` |

**Test Assertions:**
- Status code is 200
- `id` matches `{{movieId}}`

---

#### Request: Update Movie

| Property | Details |
|---|---|
| **Purpose** | Update movie details |
| **Method** | `PUT` |
| **URL** | `{{baseUrl}}/api/movies/{{movieId}}` |
| **Auth Required** | ✅ Yes |
| **Modifies Database** | ✅ Yes |
| **Env Variables Needed** | `baseUrl`, `token`, `movieId` |

**Request Body:**
```json
{
    "title": "Inception (Director's Cut)",
    "description": "Updated description",
    "durationMinutes": 160
}
```

**Success Response (200 OK):** Updated movie object.

---

#### Request: Delete Movie

| Property | Details |
|---|---|
| **Purpose** | Delete a movie (blocked if it has associated showtimes or a poster) |
| **Method** | `DELETE` |
| **URL** | `{{baseUrl}}/api/movies/{{movieId}}` |
| **Auth Required** | ✅ Yes |
| **Modifies Database** | ✅ Yes — deletes from `movies` table |
| **Env Variables Needed** | `baseUrl`, `token`, `movieId` |

**Success Response:** `204 No Content` (empty body) or `200 OK`.

**Negative Cases:**

| Scenario | Status | Response |
|---|---|---|
| Movie has showtimes | `409 Conflict` | Cannot delete — has associated showtimes |
| Movie has a poster | `409 Conflict` | Cannot delete — has associated poster |
| Movie not found | `404 Not Found` | Standard not found error |

> ⚠️ **Important:** Delete the poster and all showtimes for a movie before attempting to delete the movie itself.

---

### 04 — Movie Posters

#### Request: Upload Poster

| Property | Details |
|---|---|
| **Purpose** | Upload a poster image for a movie |
| **Method** | `POST` |
| **URL** | `{{baseUrl}}/api/movies/{{movieId}}/poster` |
| **Auth Required** | ✅ Yes |
| **Content-Type** | `multipart/form-data` |
| **Modifies Database** | ✅ Yes — stores poster metadata / file |
| **Env Variables Needed** | `baseUrl`, `token`, `movieId` |

**Request Body (form-data):**

| Key | Type | Value |
|---|---|---|
| `file` | File | Select `test_image/test.jpeg` from your project directory |

**Success Response (201 Created):** Poster metadata or confirmation message.

**Negative Cases:**

| Scenario | Status |
|---|---|
| No file attached | `400 Bad Request` |
| Poster already exists | `409 Conflict` |
| Movie not found | `404 Not Found` |

**Test Assertions:**
- Status code is 201
- Response confirms upload success

> 📁 See [Section H](#h-test-image-setup-for-poster-testing) for how to select the test image files in Postman.

---

#### Request: Replace Poster

| Property | Details |
|---|---|
| **Purpose** | Replace an existing poster with a new image |
| **Method** | `PUT` |
| **URL** | `{{baseUrl}}/api/movies/{{movieId}}/poster` |
| **Auth Required** | ✅ Yes |
| **Content-Type** | `multipart/form-data` |
| **Modifies Database** | ✅ Yes |

**Request Body (form-data):**

| Key | Type | Value |
|---|---|---|
| `file` | File | Select `test_image/test2.jpeg` |

**Success Response:** `200 OK`.

---

#### Request: Get Poster (Public)

| Property | Details |
|---|---|
| **Purpose** | Retrieve the poster image for a movie |
| **Method** | `GET` |
| **URL** | `{{baseUrl}}/api/movies/{{movieId}}/poster` |
| **Auth Required** | ❌ **No — this is a public endpoint** |
| **Modifies Database** | ❌ No |

**Success Response (200 OK):**
- Content-Type: `image/jpeg` (or appropriate image MIME type)
- Body: Raw image bytes (Postman will render a preview)

**Negative Cases:**

| Scenario | Status |
|---|---|
| No poster uploaded | `404 Not Found` |
| Movie not found | `404 Not Found` |

**Test Assertions:**
- Status code is 200
- Content-Type header contains `image/`

---

#### Request: Delete Poster

| Property | Details |
|---|---|
| **Purpose** | Remove the poster from a movie |
| **Method** | `DELETE` |
| **URL** | `{{baseUrl}}/api/movies/{{movieId}}/poster` |
| **Auth Required** | ✅ Yes |
| **Modifies Database** | ✅ Yes |

**Success Response:** `204 No Content` or `200 OK`.

---

### 05 — Showtimes

#### Request: Create Showtime

| Property | Details |
|---|---|
| **Purpose** | Schedule a new showing for a movie |
| **Method** | `POST` |
| **URL** | `{{baseUrl}}/api/showtimes` |
| **Auth Required** | ✅ Yes |
| **Modifies Database** | ✅ Yes — inserts into `showtimes` table |
| **Env Variables Needed** | `baseUrl`, `token`, `movieId` |
| **Sets Env Variables** | ✅ `showtimeId` |

**Request Body:**
```json
{
    "movieId": {{movieId}},
    "showDate": "2025-12-25",
    "showTime": "18:30",
    "price": 1500.00,
    "totalSeats": 100,
    "seatsPerRow": 10
}
```

> 💡 **Tip:** Use a future date for `showDate`. The pre-request script may generate this dynamically.

**Success Response (201 Created):**
```json
{
    "id": 1,
    "movieId": 1,
    "showDate": "2025-12-25",
    "showTime": "18:30:00",
    "price": 1500.00,
    "totalSeats": 100,
    "seatsPerRow": 10,
    "active": true
}
```

**Post-request Test Script** (auto-saves showtimeId):
```javascript
if (pm.response.code === 201) {
    var jsonData = pm.response.json();
    pm.environment.set("showtimeId", jsonData.id);
}
```

**Negative Cases:**

| Scenario | Status | Response |
|---|---|---|
| Non-existent movieId | `404 Not Found` | Movie not found |
| Missing fields | `400 Bad Request` | Validation errors |
| Overlapping showtime (same movie, date, time) | `409 Conflict` | Showtime overlap |
| Past date | `400 Bad Request` | Date must be in the future |
| `totalSeats` not divisible by `seatsPerRow` | `400 Bad Request` | Validation error |

---

#### Request: List Showtimes

| Property | Details |
|---|---|
| **Purpose** | Get all showtimes, optionally filtered |
| **Method** | `GET` |
| **URL** | `{{baseUrl}}/api/showtimes` |
| **Auth Required** | ✅ Yes |
| **Query Parameters (optional)** | `?date=2025-12-25&movieId=1` |

**Success Response (200 OK):** Array of showtime objects.

---

#### Request: Get Showtime by ID

| Property | Details |
|---|---|
| **Method** | `GET` |
| **URL** | `{{baseUrl}}/api/showtimes/{{showtimeId}}` |
| **Auth Required** | ✅ Yes |

**Negative Cases:**

| Scenario | Status |
|---|---|
| Non-existent ID | `404 Not Found` |

---

#### Request: Update Showtime

| Property | Details |
|---|---|
| **Method** | `PUT` |
| **URL** | `{{baseUrl}}/api/showtimes/{{showtimeId}}` |
| **Auth Required** | ✅ Yes |
| **Modifies Database** | ✅ Yes |

**Request Body:**
```json
{
    "movieId": {{movieId}},
    "showDate": "2025-12-26",
    "showTime": "20:00",
    "price": 1800.00,
    "totalSeats": 120,
    "seatsPerRow": 12
}
```

---

### 06 — Seat Availability

#### Request: Get Seat Availability

| Property | Details |
|---|---|
| **Purpose** | Check which seats are available/reserved for a showtime |
| **Method** | `GET` |
| **URL** | `{{baseUrl}}/api/showtimes/{{showtimeId}}/seats` |
| **Auth Required** | ✅ Yes |
| **Modifies Database** | ❌ No |
| **Env Variables Needed** | `baseUrl`, `token`, `showtimeId` |

**Success Response (200 OK):**
```json
{
    "showtimeId": 1,
    "totalSeats": 100,
    "seatsPerRow": 10,
    "reservedSeats": ["A1", "A2", "B5"],
    "availableSeats": 97,
    "active": true
}
```

**Test Assertions:**
- Status code is 200
- Response has `showtimeId`, `totalSeats`, `seatsPerRow`, `reservedSeats`, `availableSeats`, `active`
- `reservedSeats` is an array
- `availableSeats` = `totalSeats` - length of `reservedSeats`

---

### 07 — Showtime Status Toggle

#### Request: Activate Showtime

| Property | Details |
|---|---|
| **Purpose** | Set a showtime's active status to true |
| **Method** | `PATCH` |
| **URL** | `{{baseUrl}}/api/showtimes/{{showtimeId}}/status` |
| **Auth Required** | ✅ Yes |
| **Modifies Database** | ✅ Yes — updates `active` column |
| **Sets Env Variables** | ✅ `reservationShowtimeId` (for use by reservation tests) |

**Request Body:**
```json
{
    "active": true
}
```

**Success Response (200 OK):** Updated showtime with `"active": true`.

---

#### Request: Deactivate Showtime

Same endpoint, with:
```json
{
    "active": false
}
```

**Success Response (200 OK):** Updated showtime with `"active": false`.

**Test Assertions:**
- Status code is 200
- `active` field matches the boolean sent in request body

---

### 08 — Reservations (Dev Profile Required)

> ⚠️ **The backend MUST be running with `--spring.profiles.active=dev` for these endpoints to exist.**

#### Request: Process Reservation

| Property | Details |
|---|---|
| **Purpose** | Simulate processing a seat reservation for a showtime |
| **Method** | `POST` |
| **URL** | `{{baseUrl}}/api/dev/reservations` |
| **Auth Required** | ✅ Yes |
| **Modifies Database** | ✅ Yes — inserts into `reservations` / `reserved_seats` tables |
| **Env Variables Needed** | `baseUrl`, `token`, `reservationShowtimeId` |

**Request Body:**
```json
{
    "reservationId": "{{$guid}}",
    "showtimeId": {{reservationShowtimeId}},
    "customerId": "customer-001",
    "customerName": "John Doe",
    "seatNumbers": ["A1", "A2", "A3"]
}
```

> 💡 `{{$guid}}` is a Postman dynamic variable that generates a random UUID for each request.

**Success Response (200 OK or 201 Created):** Reservation confirmation.

**Negative Cases:**

| Scenario | Status | Response |
|---|---|---|
| Seats already reserved | `409 Conflict` | Seat(s) already taken |
| Showtime not found | `404 Not Found` | Showtime not found |
| Showtime inactive | `409 Conflict` | Showtime is not active |
| Invalid seat numbers | `400 Bad Request` | Validation error |
| Missing fields | `400 Bad Request` | Validation errors |
| Dev profile not active | `404 Not Found` | Endpoint not mapped |

**Test Assertions:**
- Status code is 200 or 201
- Response confirms reservation ID

---

### 09 — Catalog (Dev Profile Required)

> ⚠️ **Also requires dev profile.**

#### Request: Publish Catalog

| Property | Details |
|---|---|
| **Purpose** | Publish showtime catalog data (e.g., to Kafka) |
| **Method** | `POST` |
| **URL** | `{{baseUrl}}/api/dev/catalog/publish` |
| **Auth Required** | ✅ Yes |
| **Modifies Database** | Depends on implementation — may publish to Kafka |
| **Env Variables Needed** | `baseUrl`, `token`, `showtimeId` |

**Request Body:**
```json
{
    "showtimeId": {{showtimeId}}
}
```

> The `showtimeId` field is **required** and must be a valid numeric showtime ID.

**Success Response (200 OK):** Publish confirmation.

**Negative Cases:**

| Scenario | Status |
|---|---|
| Missing showtimeId | `400 Bad Request` |
| Invalid showtimeId | `404 Not Found` |
| Kafka broker unreachable | `500 Internal Server Error` |
| Dev profile not active | `404 Not Found` |

---

### 10 — Cleanup / Negative Tests

This folder contains requests designed to test error handling:

- **Delete movie with showtimes** → expects `409 Conflict`
- **Get non-existent movie** → expects `404 Not Found`
- **Register duplicate cinema** → expects `409 Conflict`
- **Login with wrong password** → expects `401 Unauthorized`
- **Access endpoint without token** → expects `401 Unauthorized`
- **Send malformed JSON** → expects `400 Bad Request`

Each request has test assertions verifying the error response format matches the expected structure.

---

## D. Postman Collection Runner

The Collection Runner lets you execute an entire folder or the whole collection automatically.

### How to Open the Runner

1. Click on the collection name **"Cine Cinema Complete API Tests"** in the sidebar.
2. Click the **"Run"** button (▶) at the top-right of the collection view.
3. Or: Right-click the collection → **"Run collection"**.

### Folder Independence & Dependencies

#### Independent Folders (Can Run Alone)

| Folder | Why It's Independent |
|---|---|
| **00 - Health / Smoke** | No auth or data needed |
| **01 - Authentication** | Creates its own data (register + login) |
| **10 - Cleanup / Negative** | Tests error cases with known invalid inputs |

#### Sequential Dependencies (Must Run in Order)

```
01 - Authentication
 └──▶ Sets {{token}}
      │
      ├──▶ 02 - Cinema Profile        (needs {{token}})
      │
      ├──▶ 03 - Movies                (needs {{token}}, sets {{movieId}})
      │    │
      │    ├──▶ 04 - Movie Posters    (needs {{movieId}})
      │    │
      │    └──▶ 05 - Showtimes        (needs {{movieId}}, sets {{showtimeId}})
      │         │
      │         ├──▶ 06 - Seat Availability   (needs {{showtimeId}})
      │         │
      │         └──▶ 07 - Status Toggle       (needs {{showtimeId}},
      │              │                          sets {{reservationShowtimeId}})
      │              │
      │              └──▶ 08 - Reservations   (needs {{reservationShowtimeId}},
      │                                        needs dev profile)
      │
      └──▶ 09 - Catalog (Dev)         (needs {{showtimeId}}, needs dev profile)
```

### Runner Execution Order

When running the **entire collection** through the Runner:

1. **Recommended order:** Run folders **00 → 01 → 02 → 03 → 04 → 05 → 06 → 07 → 08 → 09 → 10**
2. The runner executes requests **top-to-bottom** within each folder.
3. Test scripts at each step save environment variables for subsequent requests.
4. If a request fails, dependent requests downstream **will also fail** (cascading failure).

### Runner Configuration Tips

| Setting | Recommendation |
|---|---|
| **Iterations** | 1 (default — run once) |
| **Delay** | 200–500ms between requests (avoids overwhelming the server) |
| **Save responses** | ✅ Enable to review responses later |
| **Keep variable values** | ✅ Enable to retain `{{token}}`, `{{movieId}}`, etc. |

### Running Individual Folders

You can also run a single folder:
1. Right-click the folder (e.g., **"03 - Movies"**).
2. Select **"Run folder"**.
3. ⚠️ Make sure the required environment variables (`token`, etc.) are already set from a prior run.

---

## E. Troubleshooting

### HTTP 400 — Bad Request

**Cause:** The request body is invalid.

| Specific Issue | Solution |
|---|---|
| **Validation errors** | Check the response body — it's a map of `{"fieldName": "errorMessage"}`. Fix the highlighted fields. |
| **Malformed JSON** | Verify JSON syntax — missing commas, unclosed braces, trailing commas. Use Postman's "Beautify" button (in Body tab). |
| **Invalid date format** | Dates must be `YYYY-MM-DD` (e.g., `2025-12-25`). Time must be `HH:mm` (e.g., `18:30`). |
| **Invalid field types** | `durationMinutes`, `totalSeats`, `seatsPerRow` must be integers. `price` must be a number. `movieId` / `showtimeId` must be numeric. |
| **Past date for showtime** | `showDate` must be today or in the future. |
| **totalSeats not divisible by seatsPerRow** | Example: 100 seats with 10 per row ✅ — 100 seats with 7 per row ❌ |
| **Empty request body** | The endpoint expects JSON but received nothing. Check Content-Type is `application/json`. |
| **Request body is missing** | Response: `"Request body is missing, contains invalid JSON, or has incompatible field values."` — verify you selected `raw` → `JSON` in Postman body tab. |

**Error Response Format (validation):**
```json
{
    "title": "Title is required",
    "durationMinutes": "Must be a positive number"
}
```

**Error Response Format (malformed JSON / missing body):**
```json
{
    "timestamp": "2025-01-15T10:30:00.000+00:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Request body is missing, contains invalid JSON, or has incompatible field values.",
    "path": "/api/movies"
}
```

---

### HTTP 401 — Unauthorized

**Cause:** Authentication failed or token is missing/invalid.

| Specific Issue | Solution |
|---|---|
| **No Authorization header** | Go to the request's **Authorization** tab → Type: **Bearer Token** → Token: `{{token}}`. |
| **Token is empty** | The `{{token}}` variable has no value. Run **01 - Login** first to populate it. |
| **Token is expired** | JWT tokens expire (typically after 24h). Re-run **01 - Login** to get a fresh token. |
| **Token is malformed** | Don't manually edit the token value. Let the login test script set it automatically. |
| **Wrong credentials in login** | Verify `{{cinemaEmail}}` and `{{cinemaPassword}}` match what was used during registration. |

**Error Response Format:**
```json
{
    "timestamp": "2025-01-15T10:30:00.000+00:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Authentication required",
    "path": "/api/movies"
}
```

or:

```json
{
    "timestamp": "2025-01-15T10:30:00.000+00:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid or expired JWT",
    "path": "/api/movies"
}
```

**Quick Fix Checklist:**
1. Run Login request → check response for `accessToken`.
2. Click the **eye icon** (👁) → verify `token` has a value.
3. Check the failing request's Auth tab → must use `Bearer Token` with `{{token}}`.

---

### HTTP 403 — Forbidden

**Not applicable in the current setup.** The Cine Cinema backend uses a single-role system (one cinema account). There are no role-based access restrictions that would produce a 403.

If you encounter a 403, it likely means:
- A reverse proxy or firewall is blocking the request.
- The endpoint path is wrong (some frameworks return 403 instead of 404 for security).

---

### HTTP 404 — Not Found

**Cause:** The requested resource doesn't exist.

| Specific Issue | Solution |
|---|---|
| **Movie/showtime/reservation not found** | Verify the ID in the URL. Check `{{movieId}}` or `{{showtimeId}}` is set correctly. |
| **Wrong URL path** | Double-check the endpoint spelling. Example: `/api/movies` not `/api/movie`. |
| **Dev endpoint returns 404** | **The backend is running without `--spring.profiles.active=dev`.** Restart with the dev profile. |
| **Poster not found** | No poster has been uploaded for this movie yet. Upload one first. |

**Error Response Format:**
```json
{
    "timestamp": "2025-01-15T10:30:00.000+00:00",
    "status": 404,
    "error": "Not Found",
    "message": "Movie not found with id: 999",
    "path": "/api/movies/999"
}
```

---

### HTTP 409 — Conflict

**Cause:** The operation conflicts with existing data.

| Specific Issue | Solution |
|---|---|
| **Duplicate cinema registration** | Only one cinema account is allowed. Use login instead. |
| **Duplicate email** | The email is already in use. |
| **Movie has showtimes** | Delete all showtimes for the movie before deleting the movie. |
| **Movie has a poster** | Delete the poster before deleting the movie. |
| **Overlapping showtime** | A showtime already exists for the same movie at the same date and time. Use a different date/time. |
| **Seats already reserved** | The requested seats are taken. Choose different seat numbers. |
| **Reservation conflict** | A reservation with this ID or conflicting data already exists. Use a new `{{$guid}}`. |

**Error Response Format:**
```json
{
    "timestamp": "2025-01-15T10:30:00.000+00:00",
    "status": 409,
    "error": "Conflict",
    "message": "Cannot delete movie: it has associated showtimes",
    "path": "/api/movies/1"
}
```

---

### HTTP 500 — Internal Server Error

**Cause:** Unexpected server-side error.

**Steps to diagnose:**
1. Check the **Spring Boot console/terminal** for the full stack trace.
2. Look for `NullPointerException`, `DataAccessException`, or connection errors.
3. Common causes:
   - PostgreSQL is down or unreachable.
   - Kafka broker is unreachable (for catalog/reservation endpoints).
   - Application bug — report with the stack trace.

---

### PostgreSQL Connection Failures

**Symptoms:** Backend fails to start, or requests return 500 errors with database-related messages.

| Check | Command / Action |
|---|---|
| Is Docker running? | `docker ps` — look for the postgres container |
| Is the container healthy? | `docker logs <container-name>` |
| Can you connect manually? | `psql -h localhost -p 5432 -U postgres -d cinema_db` |
| Is port 5432 in use by something else? | `sudo lsof -i :5432` or `ss -tlnp \| grep 5432` |
| Are credentials correct? | Check `application.properties` / `application.yml` for DB username/password |

**Fix:** Restart the PostgreSQL container:
```bash
docker compose down && docker compose up -d
```

---

### Kafka Broker Unavailable

**Symptoms:** Catalog publish or reservation processing fails with connection errors / timeouts.

| Check | Command / Action |
|---|---|
| Is Kafka running? | Check the broker at `10.20.11.179:9092` |
| Network connectivity | `ping 10.20.11.179` and `telnet 10.20.11.179 9092` |
| Kafka logs | Check Kafka container/service logs |
| Is consumer enabled? | For consuming reservations, start with `--app.kafka.reservation.consumer-enabled=true` |

> **Note:** Kafka is only needed for folders 08 (Reservations) and 09 (Catalog). All other tests work without Kafka.

---

### Dev Endpoints Returning 404

**Problem:** `POST /api/dev/reservations` or `POST /api/dev/catalog/publish` returns 404.

**Root Cause:** The backend is running in normal mode without the dev profile.

**Fix:** Stop the backend and restart with:
```bash
cd backend && ./gradlew bootRun --args='--spring.profiles.active=dev'
```

The dev profile activates the `@Profile("dev")` annotated controllers that expose `/api/dev/*` endpoints.

---

### Missing Postman Environment Variables

**Symptoms:** Requests show `{{token}}` or `{{movieId}}` literally in the URL/headers instead of resolved values.

| Fix | Steps |
|---|---|
| **Environment not selected** | Top-right dropdown → select **"Cine Cinema Local"** |
| **Variable not set** | Run the prerequisite requests (Login sets `token`, Create Movie sets `movieId`, etc.) |
| **Wrong scope** | Click the eye icon (👁) → ensure variables exist in the correct environment (not globals) |
| **Typo in variable name** | Variable names are case-sensitive: `{{token}}` ≠ `{{Token}}` |

---

### Incorrect Multipart Configuration for Poster Upload

**Symptoms:** Poster upload returns 400 or the server doesn't receive the file.

**Checklist:**
1. In Postman, go to the **Body** tab of the poster upload request.
2. Select **form-data** (not raw, not x-www-form-urlencoded).
3. Set the key to `file`.
4. Change the key type from **Text** to **File** (hover over the key field — a dropdown appears).
5. Click **Select Files** and browse to `test_image/test.jpeg`.
6. Make sure the **Authorization** tab has `Bearer Token` = `{{token}}`.
7. **Do NOT** manually set `Content-Type` header — Postman sets `multipart/form-data` with boundary automatically.

---

## F. Sinhala Notes (සිංහල උපදෙස්)

### 1. Authentication — ලියාපදිංචිය සහ පිවිසුම

Register endpoint එකෙන් cinema account එකක් සාදනු ලැබේ. මෙම system එකේ cinema account එකක් පමණක් තිබිය හැකි නිසා, දෙවන වතාවක් register කිරීමට උත්සාහ කළ විට `409 Conflict` error එකක් ලැබේ. Login endpoint එක මගින් JWT access token එකක් ලබා දෙයි — මෙය `cinemas` table එකේ credentials සමඟ සංසන්දනය කර authenticate කරයි. Token එක නිවැරදිව ලැබුණොත් (`200 OK`), ඉදිරි සියලුම authenticated requests සඳහා එය `Authorization` header එකේ භාවිතා කරයි. Login අසාර්ථක වුවහොත් `401 Unauthorized` ලැබේ — email හෝ password වැරදියි.

### 2. Cinema Profile — සිනමා ප්‍රොෆයිලය

Cinema profile test එකෙන් `cinemas` table එකේ ගබඩා කර ඇති cinema එකේ නම, ලිපිනය, සහ email කියවීමට (`GET`) සහ යාවත්කාලීන කිරීමට (`PUT`) හැකිය. Profile GET request එක සාර්ථක නම් (`200 OK`), database connection එක සහ authentication නිවැරදිව ක්‍රියා කරන බව තහවුරු වේ. Profile update කරන විට `cinemas` table එකේ record එක වෙනස් වේ. Token නොමැතිව access කිරීමට උත්සාහ කළ විට `401` error ලැබේ.

### 3. Movies — චිත්‍රපට කළමනාකරණය

Movies CRUD test මගින් `movies` table එකට data ඇතුළත් කිරීම, කියවීම, යාවත්කාලීන කිරීම, සහ මැකීම පරීක්ෂා කරයි. `POST /api/movies` මගින් නව චිත්‍රපටයක් සාදන අතර, response එකෙන් ලැබෙන `movieId` ස්වයංක්‍රීයව Postman environment variable එකක් ලෙස save වේ. Movie එකක් delete කරන විට, එයට showtimes හෝ poster එකක් තිබේ නම් `409 Conflict` error එකක් ලැබේ — මුලින්ම ඒවා මැකිය යුතුය. සාර්ථක CRUD operations (`201`, `200`, `204`) ලැබීම database ක්‍රියාකාරිත්වය නිවැරදි බව තහවුරු කරයි.

### 4. Posters — පෝස්ටර් කළමනාකරණය

Poster upload test එකෙන් `multipart/form-data` ආකාරයෙන් image file එකක් server එකට upload කිරීම පරීක්ෂා කරයි. `POST` මගින් නව poster එකක් upload කරන අතර, `PUT` මගින් පවතින poster එක replace කරයි. `GET /api/movies/{movieId}/poster` endpoint එක **public** වන නිසා token අවශ්‍ය නැත — මෙය customer-facing app එකට poster පෙන්වීමට භාවිතා කරයි. Poster upload සාර්ථක නම් (`201 Created`), file storage system එක නිවැරදිව ක්‍රියා කරන බව තහවුරු වේ. Poster upload අසාර්ථක වුවහොත්, `form-data` configuration එක සහ file selection එක පරීක්ෂා කරන්න.

### 5. Showtimes — ප්‍රදර්ශන කාල කළමනාකරණය

Showtime test එකෙන් `showtimes` table එකට show schedule එකක් ඇතුළත් කිරීම, කියවීම, යාවත්කාලීන කිරීම පරීක්ෂා කරයි. සෑම showtime එකක්ම `movieId`, `showDate`, `showTime`, `price`, `totalSeats`, `seatsPerRow` අවශ්‍ය කරයි. එකම movie එකට එකම date සහ time එකේ showtime දෙකක් සෑදීමට හැකි නැත — `409 Conflict` ලැබේ. සාර්ථකව showtime සෑදීමෙන් (`201 Created`) `showtimeId` auto-save වන අතර, එය seat availability සහ reservation tests සඳහා භාවිතා කරයි.

### 6. Seat Availability — ආසන ලබාගත හැකි බව

Seat availability test එකෙන් යම් showtime එකක ආසන තත්ත්වය පරීක්ෂා කරයි — මුළු ආසන ගණන, වෙන්කර ඇති ආසන, ලබාගත හැකි ආසන, සහ showtime active ද යන්න. `reservedSeats` array එකේ length එක `totalSeats` එකෙන් අඩු කළ විට `availableSeats` ගණනට සමාන විය යුතුය. Reservation test එකක් run කළ පසු නැවත seat availability check කිරීමෙන්, reservations නිවැරදිව database එකේ record වී ඇත්ද යන්න තහවුරු කළ හැකිය.

### 7. Reservations — වෙන්කිරීම්

Reservation test (`POST /api/dev/reservations`) මගින් customer reservation එකක් process කිරීම simulate කරයි. මෙය `reservations` සහ `reserved_seats` tables වලට data ඇතුළත් කරයි. **Dev profile activate කර තිබිය යුතුය** — නැතිනම් `404` ලැබේ. දැනටමත් reserve කර ඇති seats නැවත reserve කිරීමට උත්සාහ කළ විට `409 Conflict` ලැබේ. සාර්ථක reservation එකකින් පසු seat availability endpoint එකෙන් එම seats `reservedSeats` list එකේ පෙන්විය යුතුය.

### 8. Dev Endpoints — සංවර්ධන අන්ත ලක්ෂ්‍ය

Dev endpoints (`/api/dev/*`) යනු development සහ testing අවස්ථා සඳහා පමණක් භාවිතා කරන endpoints වේ. Production environment එකේ මේවා ක්‍රියා නොකරයි. Catalog publish endpoint එක Kafka broker එකට showtime data publish කරයි — Kafka broker එක `10.20.11.179:9092` හි ක්‍රියාත්මක විය යුතුය. මෙම endpoints `404` return කරන්නේ නම්, backend එක `--spring.profiles.active=dev` flag එක සමඟ start කර ඇත්ද පරීක්ෂා කරන්න.

---

## G. Workflow Guides

The Postman collection is designed around six logical test workflows. Each workflow tests a complete user story from start to finish.

---

### Workflow A: Authentication Flow

**Purpose:** Register the cinema account and obtain a JWT token.

```
┌─────────────────────────┐
│  Register Cinema (POST) │
│  /api/auth/register     │
│  → 201 Created          │
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────┐
│  Login (POST)           │
│  /api/auth/login        │
│  → 200 OK               │
│  → Saves {{token}}      │
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────┐
│  Register Again (POST)  │
│  → 409 Conflict         │
│  (negative test)        │
└─────────────────────────┘
```

**Execution Order:**
1. `POST /api/auth/register` — creates the account
2. `POST /api/auth/login` — gets the JWT token (saved to `{{token}}`)
3. `POST /api/auth/register` (repeat) — verifies only one account is allowed (409)

**After this workflow:** `{{token}}` is set and all authenticated requests can proceed.

---

### Workflow B: Movie Management Flow

**Purpose:** Create, read, update, and manage movies with posters.

**Prerequisites:** `{{token}}` must be set (run Workflow A first).

```
┌──────────────────────────┐
│  Create Movie (POST)     │
│  /api/movies             │
│  → 201 Created           │
│  → Saves {{movieId}}     │
└───────────┬──────────────┘
            │
            ▼
┌──────────────────────────┐
│  Get Movie (GET)         │
│  /api/movies/{{movieId}} │
│  → 200 OK                │
└───────────┬──────────────┘
            │
            ▼
┌──────────────────────────┐
│  List All Movies (GET)   │
│  /api/movies             │
│  → 200 OK (array)        │
└───────────┬──────────────┘
            │
            ▼
┌──────────────────────────┐
│  Update Movie (PUT)      │
│  /api/movies/{{movieId}} │
│  → 200 OK                │
└───────────┬──────────────┘
            │
            ▼
┌──────────────────────────┐
│  Upload Poster (POST)    │
│  /api/movies/{{movieId}} │
│  /poster                 │
│  → 201 Created           │
└───────────┬──────────────┘
            │
            ▼
┌──────────────────────────┐
│  Get Poster (GET)        │
│  (PUBLIC - no auth)      │
│  → 200 OK (image bytes)  │
└───────────┬──────────────┘
            │
            ▼
┌──────────────────────────┐
│  Replace Poster (PUT)    │
│  → 200 OK                │
└───────────┬──────────────┘
            │
            ▼
┌──────────────────────────┐
│  Delete Poster (DELETE)  │
│  → 204 No Content        │
└──────────────────────────┘
```

**Execution Order:**
1. Create Movie → saves `{{movieId}}`
2. Get Movie by ID → verify creation
3. List All Movies → verify it appears in the list
4. Update Movie → modify fields
5. Upload Poster → attach `test.jpeg`
6. Get Poster (public) → verify image retrieval
7. Replace Poster → swap with `test2.jpeg`
8. Delete Poster → remove before attempting movie deletion

---

### Workflow C: Showtime Management Flow

**Purpose:** Create and manage showtimes for a movie.

**Prerequisites:** `{{token}}` and `{{movieId}}` must be set (run Workflows A and B first).

```
┌───────────────────────────┐
│  Create Showtime (POST)   │
│  /api/showtimes           │
│  → 201 Created            │
│  → Saves {{showtimeId}}   │
└───────────┬───────────────┘
            │
            ▼
┌───────────────────────────┐
│  Get Showtime (GET)       │
│  /api/showtimes/{{id}}    │
│  → 200 OK                 │
└───────────┬───────────────┘
            │
            ▼
┌───────────────────────────┐
│  List Showtimes (GET)     │
│  /api/showtimes           │
│  → 200 OK (array)         │
└───────────┬───────────────┘
            │
            ▼
┌───────────────────────────┐
│  Filter by Date (GET)     │
│  /api/showtimes?date=...  │
│  → 200 OK                 │
└───────────┬───────────────┘
            │
            ▼
┌───────────────────────────┐
│  Update Showtime (PUT)    │
│  /api/showtimes/{{id}}    │
│  → 200 OK                 │
└───────────────────────────┘
```

**Execution Order:**
1. Create Showtime → saves `{{showtimeId}}`
2. Get Showtime by ID → verify creation
3. List All Showtimes → verify it appears
4. List with filter (`?date=` and/or `?movieId=`) → verify filtering works
5. Update Showtime → change date/time/price

---

### Workflow D: Seat Availability and Reservations Flow

**Purpose:** Check seat availability, make a reservation, and verify seats update.

**Prerequisites:** `{{token}}`, `{{showtimeId}}` or `{{reservationShowtimeId}}`, dev profile active.

```
┌────────────────────────────────┐
│  Get Seat Availability (GET)   │
│  /api/showtimes/{{id}}/seats   │
│  → 200 OK                     │
│  → reservedSeats: []           │
│  → availableSeats: 100        │
└───────────┬────────────────────┘
            │
            ▼
┌────────────────────────────────┐
│  Process Reservation (POST)    │
│  /api/dev/reservations         │
│  → seats: ["A1","A2","A3"]     │
│  → 200 OK                     │
└───────────┬────────────────────┘
            │
            ▼
┌────────────────────────────────┐
│  Get Seat Availability (GET)   │
│  → reservedSeats: ["A1",...]   │
│  → availableSeats: 97         │
└───────────┬────────────────────┘
            │
            ▼
┌────────────────────────────────┐
│  Reserve Same Seats (POST)     │
│  → 409 Conflict               │
│  (negative test)               │
└────────────────────────────────┘
```

**Execution Order:**
1. Check initial seat availability → all seats available
2. Process reservation for seats A1, A2, A3
3. Re-check seat availability → 3 seats now reserved
4. Try to reserve A1 again → 409 Conflict (seats already taken)

---

### Workflow E: Showtime Status Toggle Flow

**Purpose:** Test activating and deactivating showtimes.

**Prerequisites:** `{{token}}` and `{{showtimeId}}` must be set.

```
┌────────────────────────────────┐
│  Deactivate Showtime (PATCH)   │
│  /api/showtimes/{{id}}/status  │
│  body: {"active": false}       │
│  → active: false               │
└───────────┬────────────────────┘
            │
            ▼
┌────────────────────────────────┐
│  Check Seat Availability (GET) │
│  → active: false               │
└───────────┬────────────────────┘
            │
            ▼
┌────────────────────────────────┐
│  Activate Showtime (PATCH)     │
│  body: {"active": true}        │
│  → active: true                │
│  → Saves {{reservationShowtimeId}} │
└───────────┬────────────────────┘
            │
            ▼
┌────────────────────────────────┐
│  Check Seat Availability (GET) │
│  → active: true                │
└────────────────────────────────┘
```

**Execution Order:**
1. Deactivate the showtime
2. Verify seat availability reports `active: false`
3. Reactivate the showtime → saves `{{reservationShowtimeId}}`
4. Verify seat availability reports `active: true`

---

### Workflow F: Catalog Development Testing Flow

**Purpose:** Test the catalog publishing functionality (Kafka integration).

**Prerequisites:** `{{token}}`, `{{showtimeId}}`, dev profile active, Kafka broker reachable at `10.20.11.179:9092`.

```
┌──────────────────────────────┐
│  Publish Catalog (POST)      │
│  /api/dev/catalog/publish    │
│  body: {"showtimeId": {{id}}}│
│  → 200 OK                   │
└───────────┬──────────────────┘
            │
            ▼
┌──────────────────────────────┐
│  Verify in Kafka             │
│  (check consumer logs or     │
│   Kafka UI tool)             │
└──────────────────────────────┘
```

**Execution Order:**
1. Ensure the backend is running with dev profile and Kafka is accessible
2. Send catalog publish request with a valid `showtimeId`
3. Verify success response (200 OK)
4. Optionally check Kafka topic for the published message (via Kafka CLI or UI tool)

> ⚠️ **Note:** `POST /api/dev/catalog/publish-live/{showtimeId}` is **NOT IMPLEMENTED** (Lesson 1.70 incomplete). Do not test this endpoint — it will return an error or unexpected behavior.

---

## H. Test Image Setup for Poster Testing

The project includes two test images for poster upload testing:

| File | Path | Size |
|---|---|---|
| `test.jpeg` | `test_image/test.jpeg` | 30,945 bytes (~30 KB) |
| `test2.jpeg` | `test_image/test2.jpeg` | 60,456 bytes (~59 KB) |

### How to Select Test Images in Postman

#### For Upload Poster (`POST /api/movies/{{movieId}}/poster`):

1. Open the **Upload Poster** request in Postman.
2. Go to the **Body** tab.
3. Select **form-data**.
4. You'll see a key named `file`.
5. **Important:** Hover over the `file` key — a dropdown appears on the right side showing **Text** or **File**. Select **File**.
6. Click **"Select Files"** (or **"New file from local machine"**).
7. Navigate to the project directory:
   ```
   /home/isuru-dananjaya-sampath/Desktop/Cine Cinema/cinema-app-cinema-side/test_image/
   ```
8. Select **`test.jpeg`**.
9. Click **Open**.
10. The file name appears next to the key. ✅ Ready to send.

#### For Replace Poster (`PUT /api/movies/{{movieId}}/poster`):

1. Same steps as above, but select **`test2.jpeg`** to demonstrate replacing with a different image.

#### Tips:

- If the file dialog doesn't show `.jpeg` files, change the file type filter to **"All Files"** or **"Images"**.
- Do **NOT** set the `Content-Type` header manually. Postman automatically sets `Content-Type: multipart/form-data` with the correct boundary when you use form-data body type.
- The `Authorization` header must still have `Bearer {{token}}` — poster upload/replace/delete endpoints require authentication.
- The **Get Poster** (`GET /api/movies/{{movieId}}/poster`) endpoint is **public** — no need for a token or file attachment. It returns the raw image bytes and Postman will display a preview in the response body.

#### Verifying Upload Success:

After uploading, send the **Get Poster** request:
- `GET {{baseUrl}}/api/movies/{{movieId}}/poster`
- You should see the image rendered in Postman's response body preview.
- The `Content-Type` response header should be `image/jpeg`.

---

## Quick Reference Card

| Action | Endpoint | Method | Auth | Key Variable |
|---|---|---|---|---|
| Register | `/api/auth/register` | POST | ❌ | — |
| Login | `/api/auth/login` | POST | ❌ | Sets `{{token}}` |
| Get Profile | `/api/cinema/profile` | GET | ✅ | — |
| Update Profile | `/api/cinema/profile` | PUT | ✅ | — |
| Create Movie | `/api/movies` | POST | ✅ | Sets `{{movieId}}` |
| List Movies | `/api/movies` | GET | ✅ | — |
| Get Movie | `/api/movies/{id}` | GET | ✅ | `{{movieId}}` |
| Update Movie | `/api/movies/{id}` | PUT | ✅ | `{{movieId}}` |
| Delete Movie | `/api/movies/{id}` | DELETE | ✅ | `{{movieId}}` |
| Upload Poster | `/api/movies/{id}/poster` | POST | ✅ | `{{movieId}}` |
| Replace Poster | `/api/movies/{id}/poster` | PUT | ✅ | `{{movieId}}` |
| Get Poster | `/api/movies/{id}/poster` | GET | ❌ | `{{movieId}}` |
| Delete Poster | `/api/movies/{id}/poster` | DELETE | ✅ | `{{movieId}}` |
| Create Showtime | `/api/showtimes` | POST | ✅ | Sets `{{showtimeId}}` |
| List Showtimes | `/api/showtimes` | GET | ✅ | — |
| Get Showtime | `/api/showtimes/{id}` | GET | ✅ | `{{showtimeId}}` |
| Update Showtime | `/api/showtimes/{id}` | PUT | ✅ | `{{showtimeId}}` |
| Toggle Status | `/api/showtimes/{id}/status` | PATCH | ✅ | `{{showtimeId}}` |
| Seat Availability | `/api/showtimes/{id}/seats` | GET | ✅ | `{{showtimeId}}` |
| Process Reservation | `/api/dev/reservations` | POST | ✅ | `{{reservationShowtimeId}}` |
| Publish Catalog | `/api/dev/catalog/publish` | POST | ✅ | `{{showtimeId}}` |

---

> **📌 Remember:** Always run **01 - Authentication** first to populate `{{token}}`, then follow the folder order for the best experience. If anything breaks, check [Section E — Troubleshooting](#e-troubleshooting).
