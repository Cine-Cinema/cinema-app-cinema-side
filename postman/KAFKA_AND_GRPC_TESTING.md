# 🎬 Cine Cinema — Kafka & gRPC Testing Guide

> Complete guide for testing Kafka messaging and gRPC services in the Cinema-Side backend.

---

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [Kafka CLI Testing from Docker](#2-kafka-cli-testing-from-docker)
3. [Testing cinema.catalog via REST Dev Endpoint](#3-testing-cinemacatalog-via-rest-dev-endpoint)
4. [Testing reservation.requests End-to-End](#4-testing-reservationrequests-end-to-end)
5. [Testing reservation.results](#5-testing-reservationresults)
6. [Which Tests Require Dev Profile](#6-which-tests-require-dev-profile)
7. [Which Tests Require the Reservation Consumer Enabled](#7-which-tests-require-the-reservation-consumer-enabled)
8. [How to Start the Backend in Different Modes](#8-how-to-start-the-backend-in-different-modes)
9. [gRPC — Planned / Not Yet Implemented](#9-grpc--planned--not-yet-implemented)
10. [සිංහල සාරාංශය (Sinhala Summary)](#10-සිංහල-සාරාංශය-sinhala-summary)

---

## 1. Prerequisites

Before running any Kafka tests, make sure the following are up and running:

| Component | Details |
|---|---|
| **Kafka Broker** | Running at `10.20.11.179:9092` |
| **Docker** | Kafka container named `cine-kafka` must be running |
| **Cinema Backend** | Spring Boot app started (see [Section 8](#8-how-to-start-the-backend-in-different-modes) for startup modes) |
| **Database** | MySQL/PostgreSQL (whichever the project uses) must be accessible |
| **JWT Token** | A valid JWT token for authenticated endpoints (obtain via login) |

### Verify Kafka Container is Running

```bash
docker ps | grep cine-kafka
```

If the container is not running:

```bash
docker start cine-kafka
```

---

## 2. Kafka CLI Testing from Docker

### 2.1 Access the Kafka Container Shell

```bash
docker exec -it cine-kafka bash
```

> **Note:** All Kafka CLI commands below should be run **inside** the container shell, unless otherwise noted.

---

### 2.2 List All Topics

```bash
kafka-topics --bootstrap-server localhost:9092 --list
```

You should see topics like:

```
cinema.catalog
reservation.requests
reservation.results
cine.connection-test
```

---

### 2.3 Create Topics (If They Don't Exist)

If any topics are missing, create them manually:

```bash
# cinema.catalog
kafka-topics --bootstrap-server localhost:9092 \
  --create --topic cinema.catalog \
  --partitions 1 --replication-factor 1

# reservation.requests
kafka-topics --bootstrap-server localhost:9092 \
  --create --topic reservation.requests \
  --partitions 1 --replication-factor 1

# reservation.results
kafka-topics --bootstrap-server localhost:9092 \
  --create --topic reservation.results \
  --partitions 1 --replication-factor 1

# cine.connection-test (dev only)
kafka-topics --bootstrap-server localhost:9092 \
  --create --topic cine.connection-test \
  --partitions 1 --replication-factor 1
```

### 2.4 Describe a Topic

```bash
kafka-topics --bootstrap-server localhost:9092 --describe --topic cinema.catalog
```

---

### 2.5 Produce Test Messages to `reservation.requests`

Open a producer console and paste a JSON reservation request:

```bash
kafka-console-producer --bootstrap-server localhost:9092 \
  --topic reservation.requests \
  --property "parse.key=true" \
  --property "key.separator=|"
```

Then type (key `|` value — the key is the reservationId):

```
550e8400-e29b-41d4-a716-446655440000|{"reservationId":"550e8400-e29b-41d4-a716-446655440000","showtimeId":1,"customerId":1,"customerName":"Test Customer","seatNumbers":[1,2,3]}
```

Press **Enter** to send, then **Ctrl+C** to exit the producer.

#### One-liner (from outside the container):

```bash
docker exec -it cine-kafka bash -c "echo '550e8400-e29b-41d4-a716-446655440000|{\"reservationId\":\"550e8400-e29b-41d4-a716-446655440000\",\"showtimeId\":1,\"customerId\":1,\"customerName\":\"Test Customer\",\"seatNumbers\":[1,2,3]}' | kafka-console-producer --bootstrap-server localhost:9092 --topic reservation.requests --property parse.key=true --property key.separator='|'"
```

---

### 2.6 Consume from `reservation.results`

```bash
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic reservation.results \
  --from-beginning \
  --property print.key=true \
  --property key.separator=" => "
```

Expected output (after a reservation is processed):

```
550e8400-e29b-41d4-a716-446655440000 => {"reservationId":"550e8400-e29b-41d4-a716-446655440000","status":"CONFIRMED","reason":null}
```

Press **Ctrl+C** to stop consuming.

---

### 2.7 Consume from `cinema.catalog`

```bash
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic cinema.catalog \
  --from-beginning \
  --property print.key=true \
  --property key.separator=" => "
```

---

### 2.8 Test Connection with `cine.connection-test` (Dev Profile Only)

**Produce a test message:**

```bash
kafka-console-producer --bootstrap-server localhost:9092 \
  --topic cine.connection-test
```

Type any message and press Enter:

```
Hello from Kafka CLI - connection test!
```

**What to expect:** If the backend is running with the **dev** profile, the `KafkaConnectionTestListener` will log the received message in the application console. Check the Spring Boot logs for output like:

```
[KafkaConnectionTestListener] Received: Hello from Kafka CLI - connection test!
```

---

## 3. Testing `cinema.catalog` via REST Dev Endpoint

> ⚠️ **Requires:** Backend running with **dev** profile.

### 3.1 Publish a Catalog Snapshot via Postman

**Request:**

| Field | Value |
|---|---|
| **Method** | `POST` |
| **URL** | `{{baseUrl}}/api/dev/catalog/publish` |
| **Auth** | Bearer Token: `{{token}}` |
| **Content-Type** | `application/json` |

**Body (raw JSON):**

```json
{
  "showtimeId": 1,
  "movieTitle": "Inception",
  "cinemaHall": "Hall A",
  "showDate": "2025-01-15",
  "showTime": "19:30",
  "availableSeats": 120,
  "ticketPrice": 1500.00
}
```

> **Note:** The body is a flexible `Map<String, Object>`. The only required field is a numeric `showtimeId`. All other fields are passed through as-is to the Kafka message.

### 3.2 Verify the Message Arrived in Kafka

Inside the Kafka container:

```bash
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic cinema.catalog \
  --from-beginning \
  --property print.key=true \
  --property key.separator=" => "
```

You should see the message with the key being the `showtimeId`:

```
1 => {"showtimeId":1,"movieTitle":"Inception","cinemaHall":"Hall A","showDate":"2025-01-15","showTime":"19:30","availableSeats":120,"ticketPrice":1500.0}
```

### 3.3 Postman Test Script for the Dev Endpoint

```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response indicates success", function () {
    var responseText = pm.response.text();
    // Adjust based on actual response format
    pm.expect(responseText).to.not.be.empty;
});
```

---

## 4. Testing `reservation.requests` End-to-End

This is the full flow: **Fan sends a reservation request → Cinema processes it → Cinema publishes the result**.

### 4.1 Step 1 — Start the Backend with the Reservation Consumer Enabled

```bash
./gradlew bootRun --args='--spring.profiles.active=dev --app.kafka.reservation.consumer-enabled=true'
```

> ⚠️ The reservation consumer is **DISABLED by default** (`app.kafka.reservation.consumer-enabled=false`). You **must** enable it explicitly.

### 4.2 Step 2 — Open a Consumer on `reservation.results` (Before Producing)

In a **separate terminal**, start consuming results so you don't miss the response:

```bash
docker exec -it cine-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic reservation.results \
  --property print.key=true \
  --property key.separator=" => "
```

> **Tip:** Don't use `--from-beginning` here if you only want to see new results.

### 4.3 Step 3 — Produce a Reservation Request via Kafka CLI

In another terminal:

```bash
docker exec -it cine-kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic reservation.requests \
  --property "parse.key=true" \
  --property "key.separator=|"
```

Paste the following and press Enter:

```
550e8400-e29b-41d4-a716-446655440000|{"reservationId":"550e8400-e29b-41d4-a716-446655440000","showtimeId":1,"customerId":1,"customerName":"Test Customer","seatNumbers":[1,2,3]}
```

### 4.4 Step 4 — Verify Processing in Application Logs

Check the Spring Boot console for log entries from `ReservationKafkaConsumer` indicating:

- Message received from `reservation.requests`
- Deserialization of `ReservationRequest`
- Processing via `ReservationService`
- Result published to `reservation.results`

### 4.5 Step 5 — Check the Result in `reservation.results`

In the consumer terminal from Step 2, you should see:

```
550e8400-e29b-41d4-a716-446655440000 => {"reservationId":"550e8400-e29b-41d4-a716-446655440000","status":"CONFIRMED","reason":null}
```

**Or**, if the reservation was rejected (e.g., seats already taken):

```
550e8400-e29b-41d4-a716-446655440000 => {"reservationId":"550e8400-e29b-41d4-a716-446655440000","status":"REJECTED","reason":"Seats [1, 2] are already reserved"}
```

---

## 5. Testing `reservation.results`

The `reservation.results` topic is a **producer-only** topic from the Cinema side. The Cinema app publishes results after processing reservation requests.

### 5.1 Consume Results

```bash
docker exec -it cine-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic reservation.results \
  --from-beginning \
  --property print.key=true \
  --property key.separator=" => "
```

### 5.2 Expected JSON Format

```json
{
  "reservationId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "CONFIRMED",
  "reason": null
}
```

| Field | Type | Description |
|---|---|---|
| `reservationId` | `String (UUID)` | The original reservation ID from the request |
| `status` | `String` | Either `CONFIRMED` or `REJECTED` |
| `reason` | `String \| null` | `null` if confirmed; rejection reason if rejected |

### 5.3 Verify Message Key

The message key is the `reservationId`. Confirm this by using `--property print.key=true` in the consumer command.

---

## 6. Which Tests Require Dev Profile

| Feature / Endpoint | Dev Profile Required? |
|---|---|
| `POST /api/dev/catalog/publish` | ✅ **Yes** — Controller is annotated with `@Profile("dev")` |
| `KafkaConnectionTestListener` (`cine.connection-test` topic) | ✅ **Yes** — Listener is active only in dev profile |
| `ReservationKafkaConsumer` (`reservation.requests`) | ❌ No — Works in any profile (but consumer must be enabled) |
| `ReservationResultProducer` (`reservation.results`) | ❌ No — Works in any profile |
| `CatalogKafkaProducer` (programmatic publishing) | ❌ No — Works in any profile |

> **Summary:** Only the dev REST endpoint and the connection-test listener require the dev profile. Core Kafka reservation flow works in any profile.

---

## 7. Which Tests Require the Reservation Consumer Enabled

| Component | Consumer Flag Required? |
|---|---|
| `ReservationKafkaConsumer` | ✅ **Yes** — Must set `app.kafka.reservation.consumer-enabled=true` |
| `CatalogKafkaProducer` | ❌ No |
| `ReservationResultProducer` | ❌ No (but it's triggered by the consumer processing) |
| `KafkaConnectionTestListener` | ❌ No (requires dev profile instead) |

> **Important:** The `ReservationResultProducer` only fires as a consequence of the `ReservationKafkaConsumer` processing a request. So to see results on `reservation.results`, you need the consumer enabled.

---

## 8. How to Start the Backend in Different Modes

### 8.1 Normal Mode (Default)

```bash
./gradlew bootRun
```

- Kafka producers work (catalog publishing via code)
- Reservation consumer is **disabled**
- Dev endpoints are **not available**

### 8.2 Dev Profile

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

- ✅ Dev REST endpoints available (`POST /api/dev/catalog/publish`)
- ✅ `KafkaConnectionTestListener` active
- ❌ Reservation consumer still **disabled**

### 8.3 Dev Profile + Kafka Reservation Consumer

```bash
./gradlew bootRun --args='--spring.profiles.active=dev --app.kafka.reservation.consumer-enabled=true'
```

- ✅ Dev REST endpoints available
- ✅ `KafkaConnectionTestListener` active
- ✅ Reservation consumer **enabled** — full end-to-end reservation flow works

### 8.4 Non-Dev with Reservation Consumer Only

```bash
./gradlew bootRun --args='--app.kafka.reservation.consumer-enabled=true'
```

- ❌ Dev endpoints **not** available
- ❌ Connection test listener **not** active
- ✅ Reservation consumer **enabled**

### Quick Reference Table

| Mode | Command | Dev Endpoints | Connection Test | Reservation Consumer |
|---|---|---|---|---|
| Normal | `./gradlew bootRun` | ❌ | ❌ | ❌ |
| Dev | `--args='--spring.profiles.active=dev'` | ✅ | ✅ | ❌ |
| Dev + Consumer | `--args='--spring.profiles.active=dev --app.kafka.reservation.consumer-enabled=true'` | ✅ | ✅ | ✅ |
| Prod + Consumer | `--args='--app.kafka.reservation.consumer-enabled=true'` | ❌ | ❌ | ✅ |

---

## 9. gRPC — Planned / Not Yet Implemented

> [!IMPORTANT]
> gRPC catalog synchronization is **planned** but has **NOT been implemented yet** in this project.

### Current Status

| Item | Status |
|---|---|
| `.proto` files | ❌ Not found |
| gRPC dependencies (e.g., `grpc-spring-boot-starter`) | ❌ Not in `build.gradle` |
| gRPC service implementations | ❌ None exist |
| gRPC server configuration | ❌ Not configured |

### Planned Use Case

gRPC is intended to provide **real-time catalog synchronization** between the Cinema-Side and Fan-Side applications, potentially replacing or supplementing the current Kafka-based catalog publishing approach.

### When gRPC Is Implemented — What to Test

Once gRPC is added, testing should cover:

1. **Proto compilation** — Verify `.proto` files compile correctly with `./gradlew generateProto`
2. **Server startup** — Confirm gRPC server starts on the configured port (usually `9090`)
3. **Service health** — Use `grpcurl` or Postman's gRPC support to call services
4. **Reflection** — If gRPC reflection is enabled, list services:
   ```bash
   grpcurl -plaintext localhost:9090 list
   ```
5. **Catalog sync RPC** — Call the catalog sync RPC and verify the response
6. **Error handling** — Test with invalid requests to verify proper gRPC status codes

### Tools for Future gRPC Testing

| Tool | Purpose |
|---|---|
| **Postman** (v10+) | Built-in gRPC support — import `.proto` files and invoke RPCs |
| **grpcurl** | CLI tool for gRPC, like curl for REST |
| **BloomRPC** | GUI client for gRPC services |
| **Evans** | Interactive gRPC client |

---

## 10. සිංහල සාරාංශය (Sinhala Summary)

### Kafka Messaging System එක ගැන

මෙම Cinema-Side backend application එකේ **Apache Kafka** message broker එක භාවිතා කරලා Fan-Side application එක සමග data exchange කරනවා. ප්‍රධාන topics තුනක් තියෙනවා: `cinema.catalog` topic එක හරහා cinema එකේ showtime details (movie title, hall, time, available seats) Fan-Side app එකට publish කරනවා. `reservation.requests` topic එක හරහා Fan-Side එකෙන් seat reservation requests එවනවා Cinema-Side app එකට. ඊට පස්සේ Cinema-Side app එක reservation request එක process කරලා result එක `reservation.results` topic එකට publish කරනවා — CONFIRMED නැත්නම් REJECTED status එකක් එක්ක.

### Reservation Flow එක ක්‍රියාත්මක වෙන හැටි

Fan කෙනෙක් seat reserve කරන්න request එකක් Kafka `reservation.requests` topic එකට එවනවා. ඒකේ `reservationId` (UUID), `showtimeId`, `customerId`, `customerName`, සහ `seatNumbers` array එකක් තියෙනවා. Cinema-Side backend එකේ `ReservationKafkaConsumer` component එක ඒ message එක consume කරලා `ReservationService` එකට process කරන්න දෙනවා. Service එක seats available ද බලලා reservation confirm කරනවා නැත්නම් reject කරනවා. Result එක `ReservationResultProducer` එක හරහා `reservation.results` topic එකට publish වෙනවා. මතක තියාගන්න ඕනේ, reservation consumer එක **default වලින් disable** කරලා තියෙනවා — ඒක enable කරන්න `app.kafka.reservation.consumer-enabled=true` property එක set කරන්න ඕනේ.

### Dev Profile එක සහ Testing

Backend එක **dev profile** එකේ run කරනකොට extra features දෙකක් activate වෙනවා: `POST /api/dev/catalog/publish` REST endpoint එක (Postman වලින් catalog snapshot එකක් Kafka ට publish කරන්න පුළුවන්) සහ `KafkaConnectionTestListener` එක (`cine.connection-test` topic එකට message එවලා Kafka connectivity test කරන්න). Testing කරනකොට `docker exec -it cine-kafka bash` command එකෙන් Kafka container එක ඇතුළට ගිහින් `kafka-console-producer` සහ `kafka-console-consumer` CLI tools භාවිතා කරන්න පුළුවන්.

### gRPC ගැන

gRPC catalog synchronization feature එක **plan කරලා තියෙනවා** නමුත් **තවම implement කරලා නැහැ**. Project එකේ `.proto` files, gRPC dependencies, හෝ gRPC service implementations කිසිවක් තවම නැහැ. ඉදිරියේදී gRPC implement කරනකොට real-time catalog sync සඳහා Kafka-based approach එකට අමතරව හෝ ඒක replace කරලා භාවිතා කරන්න planned කරලා තියෙනවා.

---

## Appendix: Quick Command Cheat Sheet

```bash
# ─── Container Access ───
docker exec -it cine-kafka bash

# ─── List Topics ───
kafka-topics --bootstrap-server localhost:9092 --list

# ─── Consume catalog (from beginning) ───
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic cinema.catalog --from-beginning \
  --property print.key=true --property key.separator=" => "

# ─── Consume reservation results (live) ───
kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic reservation.results \
  --property print.key=true --property key.separator=" => "

# ─── Produce reservation request ───
kafka-console-producer --bootstrap-server localhost:9092 \
  --topic reservation.requests \
  --property parse.key=true --property key.separator="|"
# Then paste:
# 550e8400-e29b-41d4-a716-446655440000|{"reservationId":"550e8400-e29b-41d4-a716-446655440000","showtimeId":1,"customerId":1,"customerName":"Test Customer","seatNumbers":[1,2,3]}

# ─── Connection test (dev profile) ───
kafka-console-producer --bootstrap-server localhost:9092 \
  --topic cine.connection-test
# Type: Hello Kafka!

# ─── Start backend (dev + consumer) ───
./gradlew bootRun --args='--spring.profiles.active=dev --app.kafka.reservation.consumer-enabled=true'
```

---

*Last updated: Auto-generated as part of the Cine Cinema API documentation suite.*
