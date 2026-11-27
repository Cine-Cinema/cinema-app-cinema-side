# 🎬 Cine Cinema — API Test Case Matrix

> **Project:** Cine Cinema (Cinema-Side Backend)  
> **API Version:** v1  
> **Base URL:** `{{baseUrl}}` (default: `http://localhost:8082`)  
> **Generated:** Auto-generated from API inventory  
> **Total Test Cases:** 79

---

## 📖 Status Legend

| Icon | Status | Description |
|------|--------|-------------|
| ✅ | Implemented & Testable | Endpoint exists, fully functional, and can be tested with Postman |
| ⚙️ | Requires Manual Setup | Test requires specific preconditions or manual data setup first |
| 🔧 | Requires Dev Profile | Endpoint only available when Spring profile `dev` is active |
| ❌ | Not Implemented | Endpoint is defined but not yet implemented in code |
| 📋 | Planned | Feature is planned for a future lesson/iteration |

---

## 1. Authentication Tests (AUTH)

| Test Case ID | Test Case Name | Method | Endpoint | Auth | Request Body / Input | Expected Status | Expected Response / Behavior | Preconditions | Postman Request Name | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| AUTH-001 | Register cinema — valid data | POST | `/api/auth/register` | No | `{ "name": "Cine Cinema", "address": "Colombo", "email": "admin@cine.lk", "password": "Pass1234" }` | `201 Created` | Returns cinema object with id, name, email; no password in response | No existing cinema account | Register Cinema | ✅ |
| AUTH-002 | Register cinema — duplicate (single cinema only) | POST | `/api/auth/register` | No | Same valid payload | `409 Conflict` | Error: only one cinema account allowed | Cinema already registered (AUTH-001) | Register Cinema - Duplicate | ✅ |
| AUTH-003 | Register cinema — blank name | POST | `/api/auth/register` | No | `{ "name": "", "email": "a@b.com", "password": "Pass1234" }` | `400 Bad Request` | Validation error on `name` (@NotBlank) | None | Register Cinema - Blank Name | ✅ |
| AUTH-004 | Register cinema — invalid email format | POST | `/api/auth/register` | No | `{ "name": "Test", "email": "not-an-email", "password": "Pass1234" }` | `400 Bad Request` | Validation error on `email` (@Email) | None | Register Cinema - Invalid Email | ✅ |
| AUTH-005 | Register cinema — password too short | POST | `/api/auth/register` | No | `{ "name": "Test", "email": "a@b.com", "password": "short" }` | `400 Bad Request` | Validation error on `password` (@Size min=8) | None | Register Cinema - Short Password | ✅ |
| AUTH-006 | Register cinema — name exceeds max length | POST | `/api/auth/register` | No | `{ "name": "<101 chars>", "email": "a@b.com", "password": "Pass1234" }` | `400 Bad Request` | Validation error on `name` (@Size max=100) | None | Register Cinema - Long Name | ✅ |
| AUTH-007 | Login — valid credentials | POST | `/api/auth/login` | No | `{ "email": "admin@cine.lk", "password": "Pass1234" }` | `200 OK` | Returns JWT token string | Cinema registered | Login - Valid | ✅ |
| AUTH-008 | Login — wrong password | POST | `/api/auth/login` | No | `{ "email": "admin@cine.lk", "password": "WrongPass" }` | `401 Unauthorized` | Authentication failure error | Cinema registered | Login - Wrong Password | ✅ |
| AUTH-009 | Login — non-existent email | POST | `/api/auth/login` | No | `{ "email": "nobody@cine.lk", "password": "Pass1234" }` | `401 Unauthorized` | Authentication failure error | None | Login - Unknown Email | ✅ |
| AUTH-010 | Login — blank email and password | POST | `/api/auth/login` | No | `{ "email": "", "password": "" }` | `400 Bad Request` | Validation errors on both fields | None | Login - Blank Fields | ✅ |

---

## 2. Cinema Profile Tests (PROFILE)

| Test Case ID | Test Case Name | Method | Endpoint | Auth | Request Body / Input | Expected Status | Expected Response / Behavior | Preconditions | Postman Request Name | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| PROFILE-001 | Get cinema profile | GET | `/api/cinema/profile` | Yes | — | `200 OK` | Returns cinema id, name, address, email | Authenticated | Get Cinema Profile | ✅ |
| PROFILE-002 | Get profile — no auth | GET | `/api/cinema/profile` | No | — | `401 Unauthorized` | Unauthorized error | None | Get Profile - No Auth | ✅ |
| PROFILE-003 | Update profile — valid data | PUT | `/api/cinema/profile` | Yes | `{ "name": "Updated Cinema", "address": "New Address", "email": "new@cine.lk" }` | `200 OK` | Returns updated profile data | Authenticated | Update Cinema Profile | ✅ |
| PROFILE-004 | Update profile — blank name | PUT | `/api/cinema/profile` | Yes | `{ "name": "", "address": "Addr", "email": "a@b.com" }` | `400 Bad Request` | Validation error on `name` | Authenticated | Update Profile - Blank Name | ✅ |
| PROFILE-005 | Update profile — invalid email | PUT | `/api/cinema/profile` | Yes | `{ "name": "Cinema", "email": "bad-email" }` | `400 Bad Request` | Validation error on `email` | Authenticated | Update Profile - Invalid Email | ✅ |
| PROFILE-006 | Update profile — no auth | PUT | `/api/cinema/profile` | No | Valid body | `401 Unauthorized` | Unauthorized error | None | Update Profile - No Auth | ✅ |

---

## 3. Movie CRUD Tests (MOVIE)

| Test Case ID | Test Case Name | Method | Endpoint | Auth | Request Body / Input | Expected Status | Expected Response / Behavior | Preconditions | Postman Request Name | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| MOVIE-001 | Create movie — valid data | POST | `/api/movies` | Yes | `{ "title": "Inception", "description": "A mind-bending thriller", "durationMinutes": 148 }` | `201 Created` | Returns movie object with generated id | Authenticated | Create Movie | ✅ |
| MOVIE-002 | Create movie — blank title | POST | `/api/movies` | Yes | `{ "title": "", "durationMinutes": 120 }` | `400 Bad Request` | Validation error on `title` (@NotBlank) | Authenticated | Create Movie - Blank Title | ✅ |
| MOVIE-003 | Create movie — negative duration | POST | `/api/movies` | Yes | `{ "title": "Test", "durationMinutes": -5 }` | `400 Bad Request` | Validation error on `durationMinutes` (@Positive) | Authenticated | Create Movie - Negative Duration | ✅ |
| MOVIE-004 | Create movie — null duration | POST | `/api/movies` | Yes | `{ "title": "Test" }` | `400 Bad Request` | Validation error on `durationMinutes` (@NotNull) | Authenticated | Create Movie - Null Duration | ✅ |
| MOVIE-005 | Create movie — title exceeds max length | POST | `/api/movies` | Yes | `{ "title": "<151 chars>", "durationMinutes": 90 }` | `400 Bad Request` | Validation error on `title` (@Size max=150) | Authenticated | Create Movie - Long Title | ✅ |
| MOVIE-006 | List all movies | GET | `/api/movies` | Yes | — | `200 OK` | Returns array of movie objects | Authenticated, at least one movie exists | List All Movies | ✅ |
| MOVIE-007 | Get movie by ID — exists | GET | `/api/movies/{id}` | Yes | Path: valid movie ID | `200 OK` | Returns single movie object | Authenticated, movie exists | Get Movie By ID | ✅ |
| MOVIE-008 | Get movie by ID — not found | GET | `/api/movies/{id}` | Yes | Path: `99999` | `404 Not Found` | Movie not found error | Authenticated | Get Movie - Not Found | ✅ |
| MOVIE-009 | Update movie — valid data | PUT | `/api/movies/{id}` | Yes | `{ "title": "Updated Title", "description": "Updated", "durationMinutes": 150 }` | `200 OK` | Returns updated movie object | Authenticated, movie exists | Update Movie | ✅ |
| MOVIE-010 | Delete movie — no dependents | DELETE | `/api/movies/{id}` | Yes | Path: movie ID with no showtimes/posters | `204 No Content` | Movie deleted successfully | Authenticated, movie has no showtimes or posters | Delete Movie | ✅ |
| MOVIE-011 | Delete movie — has showtimes (FK constraint) | DELETE | `/api/movies/{id}` | Yes | Path: movie ID with showtimes | `409 Conflict` | Error: cannot delete movie with existing showtimes | Movie has associated showtimes | Delete Movie - Has Showtimes | ⚙️ |

---

## 4. Movie Poster Tests (POSTER)

| Test Case ID | Test Case Name | Method | Endpoint | Auth | Request Body / Input | Expected Status | Expected Response / Behavior | Preconditions | Postman Request Name | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| POSTER-001 | Upload poster — valid JPEG | POST | `/api/movies/{movieId}/poster` | Yes | Multipart: JPEG file < 2MB | `201 Created` | Poster uploaded, returns poster metadata or URL | Authenticated, movie exists, no existing poster | Upload Poster - JPEG | ✅ |
| POSTER-002 | Upload poster — valid PNG | POST | `/api/movies/{movieId}/poster` | Yes | Multipart: PNG file < 2MB | `201 Created` | Poster uploaded successfully | Authenticated, movie exists, no existing poster | Upload Poster - PNG | ✅ |
| POSTER-003 | Upload poster — invalid file type (PDF) | POST | `/api/movies/{movieId}/poster` | Yes | Multipart: PDF file | `400 Bad Request` | Error: only JPEG, PNG, WebP allowed | Authenticated, movie exists | Upload Poster - Invalid Type | ✅ |
| POSTER-004 | Upload poster — file exceeds 2MB | POST | `/api/movies/{movieId}/poster` | Yes | Multipart: image > 2MB | `400 Bad Request` | Error: file size exceeds 2MB limit | Authenticated, movie exists | Upload Poster - Too Large | ⚙️ |
| POSTER-005 | Get poster — public access | GET | `/api/movies/{movieId}/poster` | No | Path: movieId with poster | `200 OK` | Returns image binary with correct Content-Type | Poster exists for movie | Get Poster - Public | ✅ |
| POSTER-006 | Get poster — not found | GET | `/api/movies/{movieId}/poster` | No | Path: movieId without poster | `404 Not Found` | Poster not found error | Movie exists but has no poster | Get Poster - Not Found | ✅ |
| POSTER-007 | Replace poster — valid image | PUT | `/api/movies/{movieId}/poster` | Yes | Multipart: new JPEG file < 2MB | `200 OK` | Poster replaced, returns updated metadata | Authenticated, poster already exists | Replace Poster | ✅ |
| POSTER-008 | Delete poster | DELETE | `/api/movies/{movieId}/poster` | Yes | Path: movieId with poster | `204 No Content` | Poster deleted successfully | Authenticated, poster exists | Delete Poster | ✅ |
| POSTER-009 | Upload poster — movie not found | POST | `/api/movies/99999/poster` | Yes | Multipart: valid JPEG | `404 Not Found` | Movie not found error | Authenticated | Upload Poster - Movie Not Found | ✅ |

---

## 5. Showtime Tests (SHOWTIME)

| Test Case ID | Test Case Name | Method | Endpoint | Auth | Request Body / Input | Expected Status | Expected Response / Behavior | Preconditions | Postman Request Name | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| SHOWTIME-001 | Create showtime — valid data | POST | `/api/showtimes` | Yes | `{ "movieId": 1, "showDate": "2025-12-25", "showTime": "18:00", "price": 1500.00, "totalSeats": 100, "seatsPerRow": 10 }` | `201 Created` | Returns showtime object with id, active=true | Authenticated, movie exists, date in future | Create Showtime | ✅ |
| SHOWTIME-002 | Create showtime — past date | POST | `/api/showtimes` | Yes | `{ "movieId": 1, "showDate": "2020-01-01", "showTime": "10:00", "price": 500.00, "totalSeats": 50, "seatsPerRow": 10 }` | `400 Bad Request` | Error: showtime must be in the future (Asia/Colombo TZ) | Authenticated, movie exists | Create Showtime - Past Date | ✅ |
| SHOWTIME-003 | Create showtime — seatsPerRow > totalSeats | POST | `/api/showtimes` | Yes | `{ "movieId": 1, "showDate": "2025-12-25", "showTime": "20:00", "price": 500.00, "totalSeats": 5, "seatsPerRow": 10 }` | `400 Bad Request` | Error: seatsPerRow cannot exceed totalSeats | Authenticated | Create Showtime - Invalid Seats | ✅ |
| SHOWTIME-004 | Create showtime — overlapping time (single hall) | POST | `/api/showtimes` | Yes | Same date/time as existing showtime | `409 Conflict` | Error: overlapping showtime exists (single hall) | Authenticated, showtime exists at same slot | Create Showtime - Overlap | ⚙️ |
| SHOWTIME-005 | Create showtime — zero price | POST | `/api/showtimes` | Yes | `{ ..., "price": 0.00, ... }` | `400 Bad Request` | Validation error: price must be > 0.00 (@DecimalMin exclusive=false) | Authenticated | Create Showtime - Zero Price | ✅ |
| SHOWTIME-006 | Create showtime — negative price | POST | `/api/showtimes` | Yes | `{ ..., "price": -100.00, ... }` | `400 Bad Request` | Validation error on `price` | Authenticated | Create Showtime - Negative Price | ✅ |
| SHOWTIME-007 | Create showtime — null movieId | POST | `/api/showtimes` | Yes | `{ "showDate": "2025-12-25", "showTime": "14:00", "price": 500.00, "totalSeats": 50, "seatsPerRow": 10 }` | `400 Bad Request` | Validation error on `movieId` (@NotNull) | Authenticated | Create Showtime - Null MovieId | ✅ |
| SHOWTIME-008 | Create showtime — non-existent movie | POST | `/api/showtimes` | Yes | `{ "movieId": 99999, ... }` | `404 Not Found` | Movie not found error | Authenticated | Create Showtime - Movie Not Found | ✅ |
| SHOWTIME-009 | List all showtimes | GET | `/api/showtimes` | Yes | — | `200 OK` | Returns array of showtime objects | Authenticated | List Showtimes | ✅ |
| SHOWTIME-010 | List showtimes — filter by date | GET | `/api/showtimes?date=2025-12-25` | Yes | Query: `date=2025-12-25` | `200 OK` | Returns only showtimes on that date | Authenticated, showtimes exist | List Showtimes - By Date | ✅ |
| SHOWTIME-011 | List showtimes — filter by movieId | GET | `/api/showtimes?movieId=1` | Yes | Query: `movieId=1` | `200 OK` | Returns only showtimes for that movie | Authenticated, showtimes exist | List Showtimes - By Movie | ✅ |
| SHOWTIME-012 | Get showtime by ID — exists | GET | `/api/showtimes/{id}` | Yes | Path: valid showtime ID | `200 OK` | Returns single showtime object | Authenticated, showtime exists | Get Showtime By ID | ✅ |
| SHOWTIME-013 | Get showtime by ID — not found | GET | `/api/showtimes/{id}` | Yes | Path: `99999` | `404 Not Found` | Showtime not found error | Authenticated | Get Showtime - Not Found | ✅ |
| SHOWTIME-014 | Update showtime — valid (no reservations) | PUT | `/api/showtimes/{id}` | Yes | Full valid body with updated values | `200 OK` | Returns updated showtime | Authenticated, showtime exists, no reservations | Update Showtime | ✅ |
| SHOWTIME-015 | Update showtime — change price with reservations | PUT | `/api/showtimes/{id}` | Yes | Body with different price | `409 Conflict` | Error: cannot change price when CONFIRMED/PAID reservations exist | Showtime has confirmed reservations | Update Showtime - Price With Reservations | ⚙️ |
| SHOWTIME-016 | Update showtime — reduce totalSeats below reserved | PUT | `/api/showtimes/{id}` | Yes | Body with totalSeats < highest reserved seat | `409 Conflict` | Error: cannot reduce totalSeats below highest reserved seat number | Showtime has reservations with high seat numbers | Update Showtime - Reduce Seats | ⚙️ |

---

## 6. Seat Availability Tests (SEAT)

| Test Case ID | Test Case Name | Method | Endpoint | Auth | Request Body / Input | Expected Status | Expected Response / Behavior | Preconditions | Postman Request Name | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| SEAT-001 | Get seat availability — no reservations | GET | `/api/showtimes/{id}/seats` | Yes | Path: showtime ID | `200 OK` | All seats available; returns seat map/grid | Authenticated, showtime exists, no reservations | Get Seats - All Available | ✅ |
| SEAT-002 | Get seat availability — partial reservations | GET | `/api/showtimes/{id}/seats` | Yes | Path: showtime ID with some reserved seats | `200 OK` | Shows mix of available and reserved seats | Showtime has some reservations | Get Seats - Partial | ⚙️ |
| SEAT-003 | Get seat availability — showtime not found | GET | `/api/showtimes/99999/seats` | Yes | Path: `99999` | `404 Not Found` | Showtime not found error | Authenticated | Get Seats - Not Found | ✅ |

---

## 7. Reservation Tests — Dev Profile (RESERVATION)

| Test Case ID | Test Case Name | Method | Endpoint | Auth | Request Body / Input | Expected Status | Expected Response / Behavior | Preconditions | Postman Request Name | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| RESERVATION-001 | Process reservation — valid | POST | `/api/dev/reservations` | Yes | `{ "reservationId": "{{$guid}}", "showtimeId": 1, "customerId": 1, "customerName": "John Doe", "seatNumbers": [1, 2, 3] }` | `200 OK` or `201 Created` | Reservation processed, seats marked reserved | Dev profile active, authenticated, showtime exists, seats available | Process Reservation - Valid | 🔧 |
| RESERVATION-002 | Process reservation — duplicate reservationId (idempotent) | POST | `/api/dev/reservations` | Yes | Same reservationId as RESERVATION-001 | `200 OK` | Returns previous result (idempotent behavior) | Previous reservation processed | Process Reservation - Idempotent | 🔧 |
| RESERVATION-003 | Process reservation — seats already taken | POST | `/api/dev/reservations` | Yes | `{ ..., "seatNumbers": [1, 2] }` (already reserved) | `409 Conflict` | Error: one or more seats already reserved | Seats 1,2 already reserved | Process Reservation - Seats Taken | 🔧 |
| RESERVATION-004 | Process reservation — invalid seat numbers | POST | `/api/dev/reservations` | Yes | `{ ..., "seatNumbers": [999] }` (exceeds totalSeats) | `400 Bad Request` | Error: seat number exceeds total seats | Authenticated, showtime exists | Process Reservation - Invalid Seat | 🔧 |
| RESERVATION-005 | Process reservation — blank reservationId | POST | `/api/dev/reservations` | Yes | `{ "reservationId": "", ... }` | `400 Bad Request` | Validation error on `reservationId` (@NotBlank) | Dev profile active | Process Reservation - Blank ID | 🔧 |
| RESERVATION-006 | Process reservation — invalid UUID format | POST | `/api/dev/reservations` | Yes | `{ "reservationId": "not-a-uuid", ... }` | `400 Bad Request` | Validation error on `reservationId` (@Pattern UUID) | Dev profile active | Process Reservation - Bad UUID | 🔧 |
| RESERVATION-007 | Process reservation — blank customerName | POST | `/api/dev/reservations` | Yes | `{ ..., "customerName": "", ... }` | `400 Bad Request` | Validation error on `customerName` (@NotBlank) | Dev profile active | Process Reservation - Blank Name | 🔧 |
| RESERVATION-008 | Process reservation — empty seatNumbers | POST | `/api/dev/reservations` | Yes | `{ ..., "seatNumbers": [] }` | `400 Bad Request` | Validation error on `seatNumbers` (@NotEmpty) | Dev profile active | Process Reservation - Empty Seats | 🔧 |
| RESERVATION-009 | Process reservation — showtime not found | POST | `/api/dev/reservations` | Yes | `{ ..., "showtimeId": 99999, ... }` | `404 Not Found` | Showtime not found error | Dev profile active, authenticated | Process Reservation - Showtime Not Found | 🔧 |
| RESERVATION-010 | Process reservation — negative seat number | POST | `/api/dev/reservations` | Yes | `{ ..., "seatNumbers": [-1, 0] }` | `400 Bad Request` | Validation error: seat numbers must be positive | Dev profile active | Process Reservation - Negative Seat | 🔧 |
| RESERVATION-011 | Process reservation — customerName exceeds max | POST | `/api/dev/reservations` | Yes | `{ ..., "customerName": "<101 chars>", ... }` | `400 Bad Request` | Validation error on `customerName` (@Size max=100) | Dev profile active | Process Reservation - Long Name | 🔧 |

---

## 8. Showtime Status Toggle Tests (STATUS)

| Test Case ID | Test Case Name | Method | Endpoint | Auth | Request Body / Input | Expected Status | Expected Response / Behavior | Preconditions | Postman Request Name | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| STATUS-001 | Deactivate showtime | PATCH | `/api/showtimes/{id}/status` | Yes | `{ "active": false }` | `200 OK` | Showtime status set to inactive | Authenticated, showtime exists, currently active | Toggle Status - Deactivate | ✅ |
| STATUS-002 | Reactivate showtime — future date | PATCH | `/api/showtimes/{id}/status` | Yes | `{ "active": true }` | `200 OK` | Showtime status set to active | Authenticated, showtime is inactive, date is in future | Toggle Status - Reactivate | ✅ |
| STATUS-003 | Reactivate showtime — past date | PATCH | `/api/showtimes/{id}/status` | Yes | `{ "active": true }` | `400 Bad Request` | Error: cannot reactivate a past showtime | Showtime date is in the past, currently inactive | Toggle Status - Past Reactivate | ⚙️ |
| STATUS-004 | Toggle status — null active field | PATCH | `/api/showtimes/{id}/status` | Yes | `{}` | `400 Bad Request` | Validation error on `active` (@NotNull) | Authenticated | Toggle Status - Null Active | ✅ |
| STATUS-005 | Toggle status — showtime not found | PATCH | `/api/showtimes/99999/status` | Yes | `{ "active": false }` | `404 Not Found` | Showtime not found error | Authenticated | Toggle Status - Not Found | ✅ |

---

## 9. Catalog Dev Tests (CATALOG)

| Test Case ID | Test Case Name | Method | Endpoint | Auth | Request Body / Input | Expected Status | Expected Response / Behavior | Preconditions | Postman Request Name | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| CATALOG-001 | Publish catalog snapshot | POST | `/api/dev/catalog/publish` | Yes | — | `200 OK` | Catalog snapshot published successfully | Dev profile active, authenticated, showtimes exist | Publish Catalog Snapshot | 🔧 |
| CATALOG-002 | Publish catalog — no auth | POST | `/api/dev/catalog/publish` | No | — | `401 Unauthorized` | Unauthorized error | Dev profile active | Publish Catalog - No Auth | 🔧 |
| CATALOG-003 | Publish live catalog — not implemented | POST | `/api/dev/catalog/publish-live/{showtimeId}` | Yes | Path: showtime ID | `501 Not Implemented` or `404 Not Found` | Endpoint not implemented (Lesson 1.70 incomplete) | Dev profile active | Publish Live Catalog - Not Implemented | ❌ |

---

## 10. Negative & Security Tests (SECURITY)

| Test Case ID | Test Case Name | Method | Endpoint | Auth | Request Body / Input | Expected Status | Expected Response / Behavior | Preconditions | Postman Request Name | Status |
|---|---|---|---|---|---|---|---|---|---|---|
| SECURITY-001 | Access protected endpoint without token | GET | `/api/movies` | No | — | `401 Unauthorized` | Unauthorized error; no data leaked | None | Security - No Token | ✅ |
| SECURITY-002 | Access protected endpoint with expired token | GET | `/api/movies` | Yes (expired) | Header: `Authorization: Bearer <expired_token>` | `401 Unauthorized` | Token expired error | Have an expired JWT | Security - Expired Token | ⚙️ |
| SECURITY-003 | Access protected endpoint with malformed token | GET | `/api/movies` | Yes (bad) | Header: `Authorization: Bearer invalidtoken123` | `401 Unauthorized` | Invalid token error | None | Security - Malformed Token | ✅ |
| SECURITY-004 | SQL injection in path parameter | GET | `/api/movies/1 OR 1=1` | Yes | Path: `1 OR 1=1` | `400 Bad Request` | Parameter type mismatch or not found; no SQL leak | Authenticated | Security - SQL Injection Path | ✅ |
| SECURITY-005 | XSS in request body | POST | `/api/movies` | Yes | `{ "title": "<script>alert('xss')</script>", "durationMinutes": 90 }` | `201 Created` or `400` | If accepted, script tags stored as plain text, never executed; response properly escaped | Authenticated | Security - XSS Body | ✅ |
| SECURITY-006 | Access dev endpoint without dev profile | POST | `/api/dev/reservations` | Yes | Valid reservation body | `404 Not Found` | Endpoint not found (controller not loaded without dev profile) | Server running WITHOUT dev profile | Security - Dev Endpoint Prod | ⚙️ |

---

## 📊 Summary Statistics

### Total Test Cases: **79**

### By Category

| Category | Count | Percentage |
|---|---|---|
| ✅ Positive / Happy Path | 32 | 40.5% |
| ❌ Negative / Validation | 35 | 44.3% |
| 🔒 Security | 6 | 7.6% |
| 🔄 Edge Case / Business Rule | 6 | 7.6% |

### By Status

| Status | Count | Percentage |
|---|---|---|
| ✅ Implemented & Testable | 48 | 60.8% |
| ⚙️ Requires Manual Setup | 9 | 11.4% |
| 🔧 Requires Dev Profile | 13 | 16.4% |
| ❌ Not Implemented | 1 | 1.3% |
| 📋 Planned (see below) | 8+ | — |

### By API Group

| Group | Test Cases | ID Range |
|---|---|---|
| Authentication | 10 | AUTH-001 — AUTH-010 |
| Cinema Profile | 6 | PROFILE-001 — PROFILE-006 |
| Movie CRUD | 11 | MOVIE-001 — MOVIE-011 |
| Movie Poster | 9 | POSTER-001 — POSTER-009 |
| Showtime | 16 | SHOWTIME-001 — SHOWTIME-016 |
| Seat Availability | 3 | SEAT-001 — SEAT-003 |
| Reservation (Dev) | 11 | RESERVATION-001 — RESERVATION-011 |
| Status Toggle | 5 | STATUS-001 — STATUS-005 |
| Catalog (Dev) | 3 | CATALOG-001 — CATALOG-003 |
| Security / Negative | 6 | SECURITY-001 — SECURITY-006 |

---

## 🚧 Not Implemented Features

The following features are **not yet implemented** and therefore have no testable endpoints:

| Feature | Status | Notes |
|---|---|---|
| **Live Catalog Publish** (`POST /api/dev/catalog/publish-live/{showtimeId}`) | ❌ Not Implemented | Lesson 1.70 incomplete; no controller method exists for this route |
| **gRPC Catalog Sync** | 📋 Planned | No REST endpoint; will use gRPC protocol for inter-service catalog synchronization |
| **Reservation Listing** (`GET /api/reservations`) | 📋 Planned | No endpoint to list reservations; reservations are received via Kafka from booking service |
| **Reservation Detail** (`GET /api/reservations/{id}`) | 📋 Planned | No endpoint to view individual reservation details |
| **Payment Marking** (`PATCH /api/reservations/{id}/payment`) | 📋 Planned | No endpoint to mark a reservation as paid |
| **Public Customer Reservation REST API** | 📋 By Design | Customer-facing reservation flow uses Kafka events, not direct REST calls to cinema service |

---

## 📝 Notes

1. **Timezone Handling:** All date/time validations for showtimes use `Asia/Colombo` timezone. Ensure test dates are set far enough in the future to remain valid.
2. **Single Cinema Constraint:** The system supports only one cinema account. The `AUTH-001` registration test should run only once per clean database. `AUTH-002` verifies the duplicate-prevention logic.
3. **Dev Profile Endpoints:** Tests marked 🔧 require the Spring Boot application to be started with `--spring.profiles.active=dev`. These endpoints are **not loaded** in production mode.
4. **Idempotent Reservations:** `RESERVATION-002` tests that sending the same `reservationId` twice returns the same response without creating a duplicate — critical for Kafka retry safety.
5. **File Upload Tests:** Poster tests (`POSTER-001` to `POSTER-004`) require actual image files. Prepare small JPEG/PNG files and one oversized file (>2MB) in advance.
6. **Overlapping Showtime Detection:** `SHOWTIME-004` requires knowing the movie's duration to calculate the time window. Create two showtimes whose `showTime + durationMinutes` windows overlap.
7. **Postman Variables Used:**
   - `{{baseUrl}}` — Server base URL (default `http://localhost:8082`)
   - `{{token}}` — JWT auth token (set after login)
   - `{{movieId}}` — Dynamic movie ID (set after movie creation)
   - `{{showtimeId}}` — Dynamic showtime ID (set after showtime creation)
   - `{{$guid}}` — Postman built-in UUID generator for reservation IDs
