# Cine Cinema Backend - Day 4 අධ්‍යයන මාර්ගෝපදේශය (Study Guide)
## Cinema Register & Login, Spring Security, JWT සහ Password Hashing

මෙම ලේඛනය (document) Cine Cinema ව්‍යාපෘතියේ (project) **Day 4** හි සිදු කරන ලද Authentication (පරිශීලකයා හඳුනා ගැනීම) සහ Security (ආරක්ෂණ) කටයුතු පිළිබඳව සිංහල මාධ්‍යයෙන් ඉදිරිපත් කර ඇති සම්පූර්ණ අධ්‍යයන සටහනකි. මෙහි සඳහන් සියලුම කේත උපුටා ගැනීම් සහ පැහැදිලි කිරීම් අපගේ කේත පදනමේ (codebase) සැබැවින්ම පවතින ගොනු මත පමණක් පදනම් වේ.

---

## 1. Day 4 හි අරමුණ සහ එය සමස්ත පද්ධතියට ගැලපෙන ආකාරය (Day 4 Purpose and Where It Fits)

### Day 4 හි මූලික අරමුණ
Day 3 හිදී අපි PostgreSQL දත්ත සමුදාය (database) සමඟ සම්බන්ධ වන JPA Entities (`Cinema`, `Movie`) සහ Repositories (`CinemaRepository`, `MovieRepository`) නිර්මාණය කළෙමු. නමුත් ඒ අවස්ථාවේදී ඕනෑම පිටස්තර පුද්ගලයෙකුට අවසරයකින් තොරව API endpoints වෙත පිවිසීමට හැකියාව තිබුණි.

**Day 4 හි ප්‍රධාන ඉලක්කය** වන්නේ සිනමා ශාලා ගිණුම සඳහා ආරක්ෂිත පිවිසුම් පද්ධතියක් ගොඩනැගීම සහ අනවසර ප්‍රවේශයන් (unauthorized access) අවහිර කිරීමයි:
1. **Cinema Register & Login (`/api/auth/register`, `/api/auth/login`):** සිනමා ශාලා පරිපාලක ගිණුම ලියාපදිංචි කිරීම සහ Login වීම.
2. **Password Hashing (`BCrypt`):** මුරපද (passwords) plain text ලෙස database එකේ ගබඩා නොකර, ආරක්ෂිත BCrypt hash එකක් බවට හරවා ගබඩා කිරීම.
3. **Stateless JWT Authentication:** Login වූ පසු සිනමා ශාලාවේ ID අංකය (`cinemaId`) ඇතුළත් අත්සන් කළ **JWT (JSON Web Token)** එකක් නිකුත් කිරීම සහ ඊළඟට එන සෑම protected request එකකදීම එම token එක පරීක්ෂා කිරීම.
4. **Spring Security Configuration:** පොදු (Public) endpoints සහ ආරක්ෂිත (Protected) endpoints වෙන් කර, session නොමැති (Stateless) API ආරක්ෂණ දාමයක් (`SecurityFilterChain`) සැකසීම.

### සමස්ත ව්‍යාපෘතිය තුළ Day 4 පිහිටීම
* **Day 3:** `Cinema` Entity සහ `CinemaRepository` (දත්ත ගබඩා කිරීමේ ස්ථරය).
* **Day 4 (මෙම අදියර):** `SecurityConfig`, `PasswordConfig`, `JwtAuthenticationFilter`, `JwtService`, `AuthController`, `AuthService`, සහ Auth DTOs (`RegisterRequest`, `LoginRequest`, `LoginResponse`, `CinemaResponse`).
* **පසු දිනවල කොටස් (Other days - කෙටි සඳහන):**
  - `CinemaProfileController` / `CinemaProfileService`: JWT token එකෙන් ලැබෙන `cinemaId` භාවිතයෙන් profile බැලීම සහ යාවත්කාලීන කිරීම (added in a later day).
  - `MovieController`, `MoviePosterController`, `ShowtimeController` සහ ඒවායේ Services/Entities: Day 4 security filter එකෙන් ආරක්ෂා වන movies, posters සහ showtimes CRUD APIs (added in other days).

### මේක මට පැහැදිලිව කියන්න පුළුවන්ද?
1. **Day 3 හි තිබූ `Cinema` entity එකට Day 4 මඟින් එකතු කරන ලද ප්‍රධාන ආරක්ෂණ විශේෂාංග මොනවාද?**
   - *පිළිතුර:* `Cinema` ගිණුම register කිරීමේදී මුරපදය BCrypt මඟින් hash කිරීම, login වීමේදී මුරපදය සසඳා JWT token එකක් නිකුත් කිරීම, සහ Spring Security filter එක මඟින් protected API endpoints වෙත එන requests වල token පරීක්ෂා කිරීමයි.
2. **Authentication සහ Authorization අතර වෙනස කුමක්ද?**
   - *පිළිතුර:* **Authentication** යනු "ඔබ කවුද?" යන්න තහවුරු කිරීමයි (උදා: Email + Password හෝ වලංගු JWT එකක් මඟින් `cinemaId` හඳුනා ගැනීම). **Authorization** යනු "ඔබට මෙම ක්‍රියාව කිරීමට අවසර තිබේද?" යන්න තීරණය කිරීමයි (උදා: `ROLE_CINEMA_MANAGER` හෝ `authenticated()` පරීක්ෂා කිරීම).
3. **REST API එකක් Stateless (තත්ත්ව රහිත) වීම යනු කුමක්ද? එහිදී JWT වැදගත් වන්නේ ඇයි?**
   - *පිළිතුර:* Server එකේ memory එක තුළ user login sessions (`HttpSession`) ගබඩා නොකිරීම Stateless බවයි. ඒ වෙනුවට, client විසින්ම සෑම request එකක් සමඟම තම අනන්‍යතාවය සහිත JWT token එක `Authorization` header එකෙන් එවන අතර server එක එය verify කරයි.

---

## 2. Security සහ JWT සඳහා නව Dependencies සහ Properties

### 2.1 `backend/build.gradle` හි Security සහ JWT Dependencies

`backend/build.gradle` ගොනුවෙන් උපුටා ගැනීම:
```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'

    testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'

    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.security:spring-security-crypto'
    implementation 'io.jsonwebtoken:jjwt-api:0.13.0'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'com.github.usefulness:webp-imageio:0.11.0'

    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.13.0'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.13.0'
    runtimeOnly 'org.postgresql:postgresql'
}
```

#### Day 4 සඳහා අදාළ Dependencies පැහැදිලි කිරීම:
| Dependency එක | Scope | අවශ්‍ය වීමට හේතුව |
|---|---|---|
| `org.springframework.boot:spring-boot-starter-security` | `implementation` | Spring Security හි මූලික filter chain (`SecurityFilterChain`), `@EnableWebSecurity`, `SecurityContextHolder`, සහ access control පද්ධතිය ලබා දෙයි. |
| `org.springframework.security:spring-security-crypto` | `implementation` | මුරපද hash කිරීම සඳහා අවශ්‍ය `BCryptPasswordEncoder` සහ `PasswordEncoder` classes ලබා දෙයි (මෙය `starter-security` තුළද අන්තර්ගත වුවත් පැහැදිලිවම එක් කර ඇත). |
| `org.springframework.boot:spring-boot-starter-validation` | `implementation` | `RegisterRequest` සහ `LoginRequest` DTOs වල ඇති `@NotBlank`, `@Email`, `@Size` වැනි input validation නීති ක්‍රියාත්මක කිරීමට උපකාරී වේ. |
| `io.jsonwebtoken:jjwt-api:0.13.0` | `implementation` | Java code එක තුළ JWT සෑදීමට (`Jwts.builder()`) සහ කියවීමට (`Jwts.parser()`) භාවිතා කරන API interfaces (`Claims`, `JwtException`, `Keys`) ලබා දෙයි. |
| `io.jsonwebtoken:jjwt-impl:0.13.0` | `runtimeOnly` | `jjwt-api` හි සැබෑ අභ්‍යන්තර ක්‍රියාත්මක කිරීම (Runtime implementation) සපයයි. අපේ කේතයෙන් කෙලින්ම import නොකරන නිසා `runtimeOnly` ලෙස යොදා ඇත. |
| `io.jsonwebtoken:jjwt-jackson:0.13.0` | `runtimeOnly` | JWT payload (Claims) JSON බවට පරිවර්තනය කිරීමට (Serialization/Deserialization) Jackson library එක සමඟ JJWT සම්බන්ධ කරයි. |

*(සටහන: `spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`, `postgresql` යනු Day 2/3 මූලික dependencies වන අතර `webp-imageio` යනු වෙනත් දිනයක poster upload සඳහා එක් කළ dependency එකකි).*

---

### 2.2 `application.properties` හි JWT සහ Environment වින්‍යාසය

`backend/src/main/resources/application.properties` ගොනුවෙන් උපුටා ගැනීම:
```properties
# Application name
spring.application.name=cinema-backend

spring.config.import=optional:file:../.env[.properties]
# Backend server port
server.port=8082

# PostgreSQL connection
spring.datasource.url=jdbc:postgresql://localhost:5432/${POSTGRES_DB}
spring.datasource.username=${POSTGRES_USER}
spring.datasource.password=${POSTGRES_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA configuration
spring.jpa.hibernate.ddl-auto=none

# Disable automatic SQL script execution
spring.sql.init.mode=never

# Movie poster upload configuration
spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=3MB

# JWT configuration
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration-ms=3600000
```

#### JWT Properties පැහැදිලි කිරීම (Hardcoded vs Environment Variables):
1. **`spring.config.import=optional:file:../.env[.properties]`:**
   - `backend/` ෆෝල්ඩරය තුළ සිට Spring Boot run කරන විට, ඉහළ root ෆෝල්ඩරයේ ඇති `.env` ගොනුව (`../.env`) properties මූලාශ්‍රයක් ලෙස ස්වයංක්‍රීයව load කර ගනී. `optional:` නිසා `.env` ගොනුව නොමැති වුවද (උදා: production හි OS environment variables භාවිතා කරන විට) error එකක් නොඑයි.
2. **`app.jwt.secret=${JWT_SECRET}` (Environment Variable / `.env` මඟින් ලබා ගනී):**
   - **Secret key එක කේතයේ Hardcode කර නැත!** එය `${JWT_SECRET}` ලෙස Environment Variable එකකින් (හෝ `.gitignore` කර ඇති `.env` ගොනුවෙන්) ලබා ගනී.
   - මෙය ඉතා වැදගත් ආරක්ෂණ පියවරකි (Security Best Practice); මන්ද Git repository එකට රහස් යතුර (Secret Key) push වීම මෙමඟින් වළකී.
   - `JwtService` තුළදී මෙම string එක Base64 decode කර අවම වශයෙන් **bytes 32 (bits 256)** ක් තිබේදැයි පරීක්ෂා කෙරේ.
3. **`app.jwt.expiration-ms=3600000` (Hardcoded අගයකි):**
   - **Token කල් ඉකුත් වීමේ කාලය (Expiry) Hardcode කර ඇත.** එය `${...}` environment variable එකකින් නොගෙන `application.properties` හි සෘජුවම `3600000` ලෙස ලියා ඇත.
   - මිලිතත්පර 3,600,000 යනු: $3,600,000 \div 1000 \div 60 = \text{මිනිත්තු } 60 = \text{පැය } 1$ කි.

### මේක මට පැහැදිලිව කියන්න පුළුවන්ද?
1. **`jjwt-impl` සහ `jjwt-jackson` dependencies `implementation` වෙනුවට `runtimeOnly` ලෙස දමා ඇත්තේ ඇයි?**
   - *පිළිතුර:* අපේ Java code එක තුළ අප කෙලින්ම භාවිතා කරන්නේ `jjwt-api` හි ඇති public classes (`Jwts`, `Claims`) පමණි. `impl` සහ `jackson` අවශ්‍ය වන්නේ කේතය compile වන විට නොව, application එක ධාවනය (runtime) වන විට token සෑදීමට සහ JSON parse කිරීමට පමණක් බැවින් `runtimeOnly` ලෙස යොදා ඇත.
2. **`app.jwt.secret` සහ `app.jwt.expiration-ms` වලින් environment variable එකකින් එන්නේ කුමක්ද? Hardcode කර ඇත්තේ කුමක්ද?**
   - *පිළිතුර:* `app.jwt.secret` එන්නේ `${JWT_SECRET}` යන environment variable එකෙන් (හෝ `../.env` ගොනුවෙන්) ය. නමුත් `app.jwt.expiration-ms` (`3600000` = පැය 1) `application.properties` හි hardcode කර ඇත.
3. **`spring.config.import=optional:file:../.env[.properties]` යෙදීමෙන් developer හට ලැබෙන පහසුව කුමක්ද?**
   - *පිළිතුර:* Local development වලදී සෑම අවස්ථාවකම terminal එකේ environment variables export නොකර, root ෆෝල්ඩරයේ ඇති `.env` ගොනුවෙන් `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` සහ `JWT_SECRET` අගයන් Spring Boot හට කෙලින්ම කියවා ගැනීමට හැකි වේ.

---

## 3. සමස්ත ක්‍රියාදාමයේ සිතියම් (Big Picture Diagrams)

### 3.1 Register ප්‍රවාහය (Cinema Register Sequence Diagram)

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client (Postman / Frontend)
    participant Filter as JwtAuthenticationFilter
    participant AuthCtrl as AuthController
    participant AuthSvc as AuthService
    participant Repo as CinemaRepository
    participant Encoder as BCryptPasswordEncoder
    participant DB as PostgreSQL (cinema)

    Client->>Filter: POST /api/auth/register + JSON Body
    Note over Filter: No Authorization Bearer header -> passes through
    Filter->>AuthCtrl: Forward request (/api/auth/register is permitAll)
    Note over AuthCtrl: @Valid validates RegisterRequest DTO
    AuthCtrl->>AuthSvc: register(RegisterRequest)
    AuthSvc->>AuthSvc: email = email.trim().toLowerCase()
    AuthSvc->>Repo: existsByEmail(email)
    Repo->>DB: SELECT count(...) > 0 FROM cinema WHERE email = ?
    DB-->>Repo: false
    AuthSvc->>Repo: count()
    Repo->>DB: SELECT count(*) FROM cinema
    DB-->>Repo: 0 (No cinema registered yet)
    AuthSvc->>Encoder: encode(rawPassword)
    Encoder-->>AuthSvc: "$2a$10$..." (BCrypt Hash)
    AuthSvc->>Repo: save(cinema)
    Repo->>DB: INSERT INTO cinema (name, address, email, password) VALUES (...)
    DB-->>Repo: Saved Cinema (id = 1)
    AuthSvc-->>AuthCtrl: CinemaResponse (id, name, address, email - NO password)
    AuthCtrl-->>Client: HTTP 201 Created + CinemaResponse JSON
```

### 3.2 Login ප්‍රවාහය (Cinema Login Sequence Diagram)

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client (Postman / Frontend)
    participant Filter as JwtAuthenticationFilter
    participant AuthCtrl as AuthController
    participant AuthSvc as AuthService
    participant Repo as CinemaRepository
    participant Encoder as BCryptPasswordEncoder
    participant JwtSvc as JwtService
    participant DB as PostgreSQL (cinema)

    Client->>Filter: POST /api/auth/login + {email, password}
    Note over Filter: No Bearer token -> passes through
    Filter->>AuthCtrl: Forward request (/api/auth/login is permitAll)
    Note over AuthCtrl: @Valid validates LoginRequest DTO
    AuthCtrl->>AuthSvc: login(LoginRequest)
    AuthSvc->>AuthSvc: email = email.trim().toLowerCase(Locale.ROOT)
    AuthSvc->>Repo: findByEmail(email)
    Repo->>DB: SELECT * FROM cinema WHERE email = ?
    DB-->>Repo: Optional<Cinema> (with BCrypt password hash)
    AuthSvc->>Encoder: matches(rawPassword, cinema.getPassword())
    Encoder-->>AuthSvc: true (Password matches)
    AuthSvc->>JwtSvc: generateToken(cinema.getId())
    JwtSvc-->>AuthSvc: "eyJhbGciOiJIUzI1NiJ9...." (Signed JWT)
    AuthSvc-->>AuthCtrl: LoginResponse(token, "Bearer")
    AuthCtrl-->>Client: HTTP 200 OK + {accessToken, tokenType: "Bearer"}
```

### 3.3 Token එකක් සහිතව Protected Endpoint එකක් ඇමතීම (Calling a Protected Endpoint with JWT)

```mermaid
sequenceDiagram
    autonumber
    actor Client as Client (Postman / Frontend)
    participant Filter as JwtAuthenticationFilter
    participant JwtSvc as JwtService
    participant SecCtx as SecurityContextHolder
    participant ProfCtrl as CinemaProfileController<br/>(later day)
    participant ProfSvc as CinemaProfileService<br/>(later day)
    participant DB as PostgreSQL (cinema)

    Client->>Filter: GET /api/cinema/profile<br/>Header: Authorization: Bearer <token>
    Filter->>Filter: Check header starts with "Bearer "
    Filter->>Filter: token = header.substring(7)
    Filter->>JwtSvc: extractCinemaId(token)
    JwtSvc->>JwtSvc: Verify signature with signingKey & check exp
    JwtSvc-->>Filter: cinemaId = 1L
    Filter->>SecCtx: Set UsernamePasswordAuthenticationToken<br/>(principal = 1L, role = ROLE_CINEMA_MANAGER)
    Filter->>ProfCtrl: filterChain.doFilter() -> Pass to Controller
    Note over ProfCtrl: @AuthenticationPrincipal Long cinemaId reads 1L from SecurityContext
    ProfCtrl->>ProfSvc: getProfile(1L)
    ProfSvc->>DB: SELECT * FROM cinema WHERE id = 1
    DB-->>ProfSvc: Cinema row
    ProfSvc-->>ProfCtrl: CinemaResponse DTO
    ProfCtrl-->>Client: HTTP 200 OK + CinemaResponse JSON
```

### 3.4 Spring Security Filter Chain හි අපගේ JWT Filter එකේ පිහිටීම

Spring Boot යෙදුම වෙත HTTP request එකක් පැමිණි විට Controller එක වෙත යාමට පෙර Spring Security හි Filters දාමයක් හරහා ගමන් කරයි. `SecurityConfig` හි `.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)` මඟින් අපගේ filter එක ස්ථානගත කර ඇති ආකාරය පහත පරිදි වේ:

```mermaid
flowchart LR
    Req["Incoming HTTP Request"] --> SecCtxFilter["SecurityContextHolderFilter\n(Initializes empty SecurityContext)"]
    SecCtxFilter --> JwtFilter["JwtAuthenticationFilter\n(OUR CUSTOM FILTER)\nExtracts & validates Bearer JWT"]
    JwtFilter -->|Invalid/Expired JWT| Err401["ApiErrorResponseWriter\n401 Unauthorized (Stops Chain)"]
    JwtFilter -->|Valid JWT or No Token| UserPassFilter["UsernamePasswordAuthenticationFilter\n(Skipped in stateless API)"]
    UserPassFilter --> ExFilter["ExceptionTranslationFilter\n(Catches Auth/Access Exceptions)"]
    ExFilter --> AuthzFilter["AuthorizationFilter\n(Checks permitAll vs authenticated)"]
    AuthzFilter -->|No Auth on Protected URL| EntryPoint["AuthenticationEntryPoint\n401 Authentication required"]
    AuthzFilter -->|Authenticated or permitAll| Dispatcher["DispatcherServlet -> Controllers\n(AuthController / MovieController / Profile)"]
```

### මේක මට පැහැදිලිව කියන්න පුළුවන්ද?
1. **Register සහ Login requests දෙකත් `JwtAuthenticationFilter` එක හරහා යනවාද? එසේ නම් ඒවා block නොවන්නේ ඇයි?**
   - *පිළිතුර:* ඔව්, සෑම HTTP request එකක්ම `JwtAuthenticationFilter` එක හරහා ගමන් කරයි. නමුත් එම requests වල `Authorization: Bearer ` header එක නොමැති නිසා `JwtAuthenticationFilter` එයට බාධා නොකර `filterChain.doFilter()` මඟින් ඉදිරියට යවයි. පසුව `AuthorizationFilter` එකේදී `/api/auth/register` සහ `/api/auth/login` සඳහා `permitAll()` දී ඇති බැවින් Controller වෙත යාමට ඉඩ ලැබේ.
2. **`JwtAuthenticationFilter` එක `UsernamePasswordAuthenticationFilter` එකට පෙර (Before) දමා ඇත්තේ ඇයි?**
   - *පිළිතුර:* Request එකේ Bearer token එකක් ඇත්නම්, Spring Security හි සම්මත authentication සහ authorization පරීක්ෂා කිරීම් සිදුවීමට පෙරම JWT එක validate කර `SecurityContextHolder` එක තුළ පරිශීලකයා (authenticated principal) සකසා තැබිය යුතු බැවිනි.
3. **වැරදි JWT token එකක් එවූ විට Request එක Controller එක දක්වා ගමන් කරනවාද?**
   - *පිළිතුර:* නැත. `JwtAuthenticationFilter` තුළදීම `JwtException` අල්ලා ගෙන `ApiErrorResponseWriter` මඟින් `401 Unauthorized` JSON ප්‍රතිචාරය ලියා `return;` කරන බැවින් `filterChain.doFilter()` කැඳවෙන්නේ නැත. එනිසා Controller එක කිසිසේත් ක්‍රියාත්මක නොවේ.


---

## 4. සෑම Security සහ Auth Class එකක්ම පේළියෙන් පේළිය විග්‍රහය (Every Security Class, One by One)

### 4.1 `SecurityConfig.java`

`backend/src/main/java/com/cinemaapp/cinema/config/SecurityConfig.java` ගොනුවෙන් සම්පූර්ණ උපුටා ගැනීම:
```java
package com.cinemaapp.cinema.config;

import com.cinemaapp.cinema.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.cinemaapp.cinema.exception.ApiErrorResponseWriter;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            ApiErrorResponseWriter errorResponseWriter
    ) throws Exception {

        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(
                        jwtService,
                        errorResponseWriter
                );

        http
                // Disable CSRF for our stateless Bearer-token API
                .csrf(csrf -> csrf.disable())

                // Do not create HTTP sessions
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                // Configure which endpoints are public
                .authorizeHttpRequests(auth -> auth

                        // Public authentication endpoints
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // Public movie poster retrieval
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/movies/*/poster"
                        ).permitAll()

                        // Development endpoint
                        .requestMatchers("/api/hello").permitAll()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // Return 401 when authentication is required but missing
                .exceptionHandling(exception -> exception

                        // Handle missing authentication
                        .authenticationEntryPoint(
                                (request, response, authException) ->
                                        errorResponseWriter.writeError(
                                                request,
                                                response,
                                                HttpStatus.UNAUTHORIZED,
                                                "Authentication required"
                                        )
                        )

                        // Handle authenticated users without permission
                        .accessDeniedHandler(
                                (request, response, accessDeniedException) ->
                                        errorResponseWriter.writeError(
                                                request,
                                                response,
                                                HttpStatus.FORBIDDEN,
                                                "You do not have permission to perform this operation."
                                        )
                        )
                )

                // We use JWT, not form login or HTTP Basic
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // Execute our JWT filter before username/password authentication
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
```

#### පේළියෙන් පේළිය විග්‍රහය:
* `@Configuration` සහ `@EnableWebSecurity`: මෙය Spring configuration class එකක් බවත්, Spring Security web ආරක්ෂණ පද්ධතිය සක්‍රීය කරන බවත් දක්වයි.
* `@Bean public SecurityFilterChain securityFilterChain(...)`: Spring Security 6+/Spring Boot 4 හි HTTP ආරක්ෂණ නීති සකසන ප්‍රධාන Bean එක මෙයයි. Spring විසින් `HttpSecurity`, `JwtService`, සහ `ApiErrorResponseWriter` මෙයට inject කරයි.
* `JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, errorResponseWriter);`:
  - මෙහිදී filter එක `@Component` ලෙස declare නොකර `new` මඟින් සාදා ඇත.
  - **හේතුව:** `OncePerRequestFilter` එකක් `@Component` කළහොත්, Spring Boot එය Spring Security filter chain එකට අමතරව සාමාන්‍ය Servlet Container filter එකක් ලෙසත් දෙවරක් ලියාපදිංචි (double registration) කරයි. `new` මඟින් සෑදීමෙන් එය Spring Security filter chain එක තුළ පමණක් එක් වරක් ක්‍රියාත්මක වේ.
* `.csrf(csrf -> csrf.disable())` — **Stateless API එකක් සඳහා CSRF අක්‍රීය කිරීම:**
  - **CSRF (Cross-Site Request Forgery)** ප්‍රහාර සිදුවන්නේ browser එක විසින් වෙබ් අඩවියකට සෑම request එකකදීම ස්වයංක්‍රීයව යවන **Cookies** අවභාවිතා කිරීමෙනි.
  - අපගේ REST API එක cookies භාවිතා නොකර `Authorization: Bearer <token>` header එක මත ක්‍රියාත්මක වන stateless API එකකි. Browser එකක් විසින් `Authorization` header එක ස්වයංක්‍රීයව නොයවන බැවින් CSRF අවදානමක් නැත; එම නිසා POST/PUT/DELETE requests වලට බාධා නොවීමට CSRF disable කර ඇත.
* `.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))`:
  - Spring Security හට කිසිවිටෙක `HttpSession` එකක් සෑදීමට හෝ session cookies (`JSESSIONID`) භාවිතා කිරීමට ඉඩ නොදේ. සෑම request එකක්ම ස්වාධීන වන අතර JWT මඟින්ම පරිශීලකයා හඳුනා ගනී.
* `.authorizeHttpRequests(auth -> auth ...)` — **Public vs Protected URLs:**
  - **Public (අවසරයකින් තොරව පිවිසිය හැකි):**
    1. `POST /api/auth/register` සහ `POST /api/auth/login` (`permitAll()`) — නව ගිණුමක් සෑදීමට සහ token ලබා ගැනීමට login වී සිටිය නොහැකි නිසා public විය යුතුය.
    2. `GET /api/movies/*/poster` (`permitAll()`) — චිත්‍රපට පෝස්ටර් රූප බැලීම පොදු වේ (later day feature).
    3. `/api/hello` (`permitAll()`) — සංවර්ධන පරීක්ෂාවන් සඳහා තබා ඇති rule එකකි.
  - **Protected (Token අනිවාර්ය වන):**
    - `.anyRequest().authenticated()` — ඉහත URLs හැර අනෙකුත් **සියලුම** endpoints (උදා: `/api/cinema/profile`, `/api/movies`, `/api/showtimes`) සඳහා වලංගු authentication එකක් අනිවාර්ය වේ.
* `.exceptionHandling(...)`:
  - `.authenticationEntryPoint(...)`: Authentication නොමැතිව protected URL එකකට පැමිණි විට, default HTML login page එක වෙනුවට `ApiErrorResponseWriter` මඟින් **401 Unauthorized** JSON (`"Authentication required"`) ප්‍රතිචාරය යවයි.
  - `.accessDeniedHandler(...)`: Authenticate වී සිටින පරිශීලකයෙකුට අවසර නැති ක්‍රියාවක් කිරීමට උත්සාහ කළහොත් **403 Forbidden** JSON (`"You do not have permission to perform this operation."`) යවයි.
* `.formLogin(form -> form.disable()).httpBasic(basic -> basic.disable())`: Spring Security හි default HTML login form එක සහ HTTP Basic Auth popup එක අක්‍රීය කරයි.
* `.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)`: අපගේ `JwtAuthenticationFilter` එක සම්මත `UsernamePasswordAuthenticationFilter` එකට පෙර filter chain එකට එක් කරයි.
* `return http.build();`: සකසන ලද `SecurityFilterChain` object එක Spring වෙත ආපසු ලබා දෙයි.

---

### 4.2 `PasswordConfig.java`

`backend/src/main/java/com/cinemaapp/cinema/config/PasswordConfig.java` ගොනුවෙන් සම්පූර්ණ උපුටා ගැනීම:
```java
package com.cinemaapp.cinema.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

#### පේළියෙන් පේළිය විග්‍රහය:
* `@Configuration`: මෙය Spring Bean අර්ථ දක්වන configuration class එකක් බව දක්වයි.
* `@Bean public PasswordEncoder passwordEncoder()`: `PasswordEncoder` interface එක සඳහා `BCryptPasswordEncoder` instance එකක් Spring IoC container එක තුළ Bean එකක් ලෙස ලියාපදිංචි කරයි.
* Default strength (cost factor) එක `10` වේ (`new BCryptPasswordEncoder()` හි පෙරනිමි අගය 10 කි). `AuthService` තුළ Constructor Injection මඟින් මෙම Bean එක ලබා ගනී.
* මෙය `SecurityConfig` වෙතින් වෙන්ව තැබීමෙන් circular dependency ගැටලු මඟහැරී කේතය පිරිසිදු වේ.

---

### 4.3 `JwtAuthenticationFilter.java`

`backend/src/main/java/com/cinemaapp/cinema/config/JwtAuthenticationFilter.java` ගොනුවෙන් සම්පූර්ණ උපුටා ගැනීම:
```java
package com.cinemaapp.cinema.config;

import com.cinemaapp.cinema.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import com.cinemaapp.cinema.exception.ApiErrorResponseWriter;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ApiErrorResponseWriter errorResponseWriter;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            ApiErrorResponseWriter errorResponseWriter
    ) {
        this.jwtService = jwtService;
        this.errorResponseWriter = errorResponseWriter;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        // Continue without authentication if there is no Bearer token
        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        try {

            // Verify the token and extract the cinema account ID
            Long cinemaId = jwtService.extractCinemaId(token);

            // Create an authenticated security object
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            cinemaId,
                            null,
                            List.of(
                                    new SimpleGrantedAuthority("ROLE_CINEMA_MANAGER")
                            )
                    );

            // Store authentication in Spring Security's context
            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            context.setAuthentication(authentication);

            SecurityContextHolder.setContext(context);

        } catch (JwtException | IllegalArgumentException exception) {

            SecurityContextHolder.clearContext();

            errorResponseWriter.writeError(
                    request,
                    response,
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired JWT"
            );

            return;
        }

        // Continue to the next security filter
        filterChain.doFilter(request, response);
    }
}
```

#### පේළියෙන් පේළිය විග්‍රහය:
* `extends OncePerRequestFilter`: සෑම HTTP request එකක් සඳහාම මෙම filter එක හරියටම **එක් වරක් පමණක්** ක්‍රියාත්මක වන බව Spring මඟින් සහතික කරයි.
* `String authorizationHeader = request.getHeader("Authorization");`: Request එකෙහි `Authorization` HTTP header එක කියවයි.
* `if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer "))`: Header එක නොමැති නම් හෝ `"Bearer "` (අවසානයේ හිස්තැනක් සහිතව) ආරම්භ නොවේ නම්, authentication එකක් set නොකර `filterChain.doFilter(request, response)` මඟින් ඊළඟ filter එකට යවයි. (එවිට public endpoint එකක් නම් ඉඩ ලැබෙන අතර, protected එකක් නම් `AuthenticationEntryPoint` මඟින් 401 දෙනු ලැබේ).
* `String token = authorizationHeader.substring(7);`: `"Bearer "` යනු අක්ෂර 7 කි (`B-e-a-r-e-r- `). එම මුල් අක්ෂර 7 ඉවත් කර සැබෑ JWT string එක පමණක් වෙන් කර ගනී.
* `Long cinemaId = jwtService.extractCinemaId(token);`: Token එකේ signature සහ expiry date පරීක්ෂා කර, එහි `sub` (subject) claim එකෙන් `cinemaId` ලබා ගනී.
* `new UsernamePasswordAuthenticationToken(cinemaId, null, List.of(new SimpleGrantedAuthority("ROLE_CINEMA_MANAGER")))`:
  - 1st argument (`principal`): `cinemaId` (`Long`) — පසුව Controller එකේ `@AuthenticationPrincipal Long cinemaId` මඟින් ලබා ගන්නේ මෙම අගයයි.
  - 2nd argument (`credentials`): `null` — Token එක දැනටමත් verify කර ඇති නිසා password අවශ්‍ය නැත.
  - 3rd argument (`authorities`): `ROLE_CINEMA_MANAGER` නමැති authority එක ලබා දෙයි.
* `SecurityContext context = SecurityContextHolder.createEmptyContext();` සහ `SecurityContextHolder.setContext(context);`: ThreadLocal හි race conditions වැළැක්වීමට අලුත් හිස් `SecurityContext` එකක් සාදා, එයට authentication object එක දමා Spring Security වෙත set කරයි.
* `catch (JwtException | IllegalArgumentException exception)`:
  - Token එක expire වී ඇත්නම් (`ExpiredJwtException`), වෙනස් කර ඇත්නම් (`SignatureException`), malformed නම් හෝ හිස් නම් මෙම block එක ක්‍රියාත්මක වේ.
  - `SecurityContextHolder.clearContext()` මඟින් context එක පිරිසිදු කර, `ApiErrorResponseWriter` මඟින් `401 Unauthorized` (`"Invalid or expired JWT"`) JSON response එක යවා `return;` කරයි (එවිට request එක controller එකට නොයයි).
* `filterChain.doFilter(request, response);`: Token එක වලංගු නම්, ඊළඟ security filter එක හරහා controller එක වෙත යොමු කරයි.

---

### 4.4 `JwtService.java`

`backend/src/main/java/com/cinemaapp/cinema/service/JwtService.java` ගොනුවෙන් සම්පූර්ණ උපුටා ගැනීම:
```java
package com.cinemaapp.cinema.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String base64Secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs
    ) {

        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain at least 32 bytes"
            );
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(Long cinemaId) {

        Date issuedAt = new Date();

        Date expiration = new Date(
                issuedAt.getTime() + expirationMs
        );

        return Jwts.builder()
                .subject(String.valueOf(cinemaId))
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    public Long extractCinemaId(String token) {

        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }
}
```

#### පේළියෙන් පේළිය විග්‍රහය:
* `@Service`: මෙය Spring Service Bean එකක් ලෙස ලියාපදිංචි කරයි.
* Constructor `@Value("${app.jwt.secret}")` සහ `@Value("${app.jwt.expiration-ms}")`: `application.properties` (සහ `.env`) වෙතින් secret key එක සහ කල් ඉකුත් වීමේ කාලය ලබා ගනී.
* `byte[] keyBytes = Decoders.BASE64.decode(base64Secret);`: Base64 string එකක් ලෙස ඇති `JWT_SECRET` අගය raw bytes බවට decode කරයි.
* `if (keyBytes.length < 32)`: HMAC-SHA256 (HS256) ඇල්ගොරිතමය සඳහා අවම වශයෙන් **bytes 32 (256 bits)** ක යතුරක් අවශ්‍ය වේ. ඊට අඩු නම් startup එකේදීම `IllegalArgumentException` එකක් විසි කර application එක නවත්වයි.
* `this.signingKey = Keys.hmacShaKeyFor(keyBytes);`: JJWT library එක මඟින් HMAC-SHA `SecretKey` object එකක් සාදයි.
* `generateToken(Long cinemaId)`:
  - `issuedAt`: වත්මන් දිනය හා වේලාව (`iat` claim).
  - `expiration`: වත්මන් වේලාව + `3600000ms` (පැය 1) (`exp` claim).
  - `.subject(String.valueOf(cinemaId))`: Token එකේ `sub` claim එක ලෙස cinema ID එක string එකක් ලෙස තැන්පත් කරයි.
  - `.signWith(signingKey).compact()`: Secret key එකෙන් අත්සන් කර `header.payload.signature` ආකෘතියේ compact JWT string එක නිපදවයි.
* `extractCinemaId(String token)`:
  - `Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token)`: Secret key එකෙන් signature එක නිවැරදිදැයි පරීක්ෂා කර, `exp` claim එක කල් ඉකුත් වී ඇත්දැයි බලයි.
  - `.getPayload()` මඟින් `Claims` ලබාගෙන `claims.getSubject()` (`String`) `Long.parseLong(...)` මඟින් `Long` බවට හරවා ආපසු දෙයි.

---

### 4.5 `AuthController.java`

`backend/src/main/java/com/cinemaapp/cinema/controller/AuthController.java` ගොනුවෙන් සම්පූර්ණ උපුටා ගැනීම:
```java
package com.cinemaapp.cinema.controller;

import com.cinemaapp.cinema.dto.CinemaResponse;
import com.cinemaapp.cinema.dto.RegisterRequest;
import com.cinemaapp.cinema.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cinemaapp.cinema.dto.LoginRequest;
import com.cinemaapp.cinema.dto.LoginResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<CinemaResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        CinemaResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }
}
```

#### පේළියෙන් පේළිය විග්‍රහය:
* `@RestController` සහ `@RequestMapping("/api/auth")`: මෙම controller එකේ සියලුම endpoints `/api/auth` යටතේ ඇති බවත්, ප්‍රතිචාර JSON ලෙස යවන බවත් දක්වයි.
* `@PostMapping("/register")`: `POST /api/auth/register` endpoint එකයි.
  - `@Valid @RequestBody RegisterRequest request`: HTTP JSON body එක `RegisterRequest` object එකක් බවට හරවා, එහි ඇති validation annotations (`@NotBlank`, `@Email`, `@Size`) පරීක්ෂා කරයි. දෝෂ ඇත්නම් `MethodArgumentNotValidException` විසි වේ.
  - සාර්ථක වූ විට `HttpStatus.CREATED` (201) සමඟ `CinemaResponse` JSON එක යවයි.
* `@PostMapping("/login")`: `POST /api/auth/login` endpoint එකයි.
  - `@Valid @RequestBody LoginRequest request`: Email සහ Password validation පරීක්ෂා කර `authService.login(request)` අමතයි.
  - සාර්ථක වූ විට `ResponseEntity.ok(response)` මඟින් HTTP 200 OK සමඟ `LoginResponse` (`accessToken`, `tokenType`) යවයි.


### 4.6 `AuthService.java`

`backend/src/main/java/com/cinemaapp/cinema/service/AuthService.java` ගොනුවෙන් සම්පූර්ණ උපුටා ගැනීම:
```java
package com.cinemaapp.cinema.service;

import com.cinemaapp.cinema.dto.CinemaResponse;
import com.cinemaapp.cinema.dto.RegisterRequest;
import com.cinemaapp.cinema.entity.Cinema;
import com.cinemaapp.cinema.repository.CinemaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.cinemaapp.cinema.dto.LoginRequest;
import com.cinemaapp.cinema.dto.LoginResponse;
import java.util.Locale;

@Service
public class AuthService {

    private final CinemaRepository cinemaRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    public AuthService(
            CinemaRepository cinemaRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.cinemaRepository = cinemaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public CinemaResponse register(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        // Check whether this email is already registered
        if (cinemaRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This email is already registered"
            );
        }

        // Only one cinema account is allowed
        if (cinemaRepository.count() > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A cinema account has already been registered"
            );
        }

        // Create the cinema entity
        Cinema cinema = new Cinema();

        cinema.setName(request.getName().trim());
        cinema.setAddress(request.getAddress());
        cinema.setEmail(email);

        // Hash the password before saving
        cinema.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save the cinema account in PostgreSQL
        Cinema savedCinema = cinemaRepository.save(cinema);

        // Return safe account details
        return new CinemaResponse(
                savedCinema.getId(),
                savedCinema.getName(),
                savedCinema.getAddress(),
                savedCinema.getEmail()
        );
    }
    public LoginResponse login(LoginRequest request) {

        // Normalize the email
        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        // Find the cinema account
        Cinema cinema = cinemaRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        // Verify the entered password against the stored BCrypt hash
        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                cinema.getPassword()
        );

        if (!passwordMatches) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        // Generate JWT after successful authentication
        String token = jwtService.generateToken(cinema.getId());

        // Return authentication token
        return new LoginResponse(token, "Bearer");
    }
}
```

#### පේළියෙන් පේළිය විග්‍රහය:
* Constructor Injection: `CinemaRepository` (Day 3), `PasswordEncoder` (`PasswordConfig`), සහ `JwtService` (`Day 4`) යන dependencies තුන inject කර ගනී.
* **`register(RegisterRequest request)`:**
  - `request.getEmail().trim().toLowerCase()`: Email එකෙහි දෙපස ඇති හිස්තැන් ඉවත් කර සියල්ල කුඩා අකුරු (lowercase) බවට සාමාන්‍යකරණය (normalize) කරයි.
  - `cinemaRepository.existsByEmail(email)`: Email එක දැනටමත් තිබේ නම් `409 Conflict` (`ResponseStatusException`) ලබා දෙයි.
  - `cinemaRepository.count() > 0`: අපගේ පද්ධතිය තුළ **එක් සිනමා ශාලා ගිණුමක් පමණක්** ලියාපදිංචි කිරීමේ නීතිය (One-cinema rule) ක්‍රියාත්මක කරයි. දැනටමත් ගිණුමක් ඇත්නම් `409 Conflict` ලබා දෙයි.
  - `cinema.setPassword(passwordEncoder.encode(request.getPassword()))`: පරිශීලකයා එවූ plain password එක `BCryptPasswordEncoder` මඟින් hash කර entity එකට ඇතුළත් කරයි.
  - `cinemaRepository.save(cinema)` මඟින් PostgreSQL හි `cinema` table එකේ සුරැකූ පසු, **password නොමැතිව** `CinemaResponse` DTO එක ආපසු දෙයි.
* **`login(LoginRequest request)`:**
  - `request.getEmail().trim().toLowerCase(Locale.ROOT)`: `Locale.ROOT` භාවිතයෙන් ඕනෑම පරිගණක භාෂා පරිසරයක (locale) එකම ආකාරයට lowercase කරයි.
  - `cinemaRepository.findByEmail(email).orElseThrow(...)`: Email එක නොමැති නම් `401 Unauthorized` (`"Invalid email or password"`) විසි කරයි.
  - `passwordEncoder.matches(request.getPassword(), cinema.getPassword())`: Login වීමේදී එවූ plain password එක, DB එකේ ඇති BCrypt hash එක සමඟ ගැළපේදැයි පරීක්ෂා කරයි.
  - නොගැළපේ නම් email වැරදි වූ විට මෙන්ම `401 Unauthorized` (`"Invalid email or password"`) ලබා දෙයි (ප්‍රහාරකයෙකුට email එක පවතීදැයි අනුමාන කිරීම වැළැක්වීමට).
  - සාර්ථක නම් `jwtService.generateToken(cinema.getId())` මඟින් token එක සාදා `new LoginResponse(token, "Bearer")` ආපසු දෙයි.

---

### 4.7 Authentication DTOs (`RegisterRequest`, `LoginRequest`, `LoginResponse`, `CinemaResponse`)

#### `RegisterRequest.java`
`backend/src/main/java/com/cinemaapp/cinema/dto/RegisterRequest.java` ගොනුවෙන් උපුටා ගැනීම:
```java
package com.cinemaapp.cinema.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Cinema name is required")
    @Size(max = 100, message = "Cinema name cannot exceed 100 characters")
    private String name;

    @Size(max = 200, message = "Address cannot exceed 200 characters")
    private String address;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must contain between 8 and 100 characters")
    private String password;

    public RegisterRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
```
* **විග්‍රහය:** `name`, `email`, `password` අනිවාර්ය (`@NotBlank`) වන අතර `address` optional වේ (`@Size(max = 200)` පමණි). Password එක අවම අක්ෂර 8 ක් හා උපරිම 100 ක් විය යුතුය.

#### `LoginRequest.java`
`backend/src/main/java/com/cinemaapp/cinema/dto/LoginRequest.java` ගොනුවෙන් උපුටා ගැනීම:
```java
package com.cinemaapp.cinema.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
```
* **විග්‍රහය:** Login වීමේදී email එක හිස් නොවන වලංගු email එකක් විය යුතු අතර password එක හිස් නොවිය යුතුය (`@NotBlank`).

#### `LoginResponse.java`
`backend/src/main/java/com/cinemaapp/cinema/dto/LoginResponse.java` ගොනුවෙන් උපුටා ගැනීම:
```java
package com.cinemaapp.cinema.dto;

public class LoginResponse {

    private String accessToken;
    private String tokenType;

    public LoginResponse(String accessToken, String tokenType) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }
}
```
* **විග්‍රහය:** Login සාර්ථක වූ විට `accessToken` (JWT string) සහ `tokenType` (`"Bearer"`) යන JSON fields දෙක client වෙත යවයි.

#### `CinemaResponse.java`
`backend/src/main/java/com/cinemaapp/cinema/dto/CinemaResponse.java` ගොනුවෙන් උපුටා ගැනීම:
```java
package com.cinemaapp.cinema.dto;

public class CinemaResponse {

    private Long id;
    private String name;
    private String address;
    private String email;

    public CinemaResponse() {
    }

    public CinemaResponse(Long id, String name, String address, String email) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }
}
```
* **විග්‍රහය:** Register වූ පසු (සහ profile API වලදී) client වෙත ආපසු යවන DTO එකයි. මෙහි `password` field එක **කිසිසේත් ඇතුළත් නොකර** ආරක්ෂාව තහවුරු කර ඇත.

---

### 4.8 Auth හි භාවිතා වන Exception Handlers (`ApiErrorResponseWriter`, `ApiErrorResponse`, `GlobalExceptionHandler`)

#### `ApiErrorResponseWriter.java`
`backend/src/main/java/com/cinemaapp/cinema/exception/ApiErrorResponseWriter.java` ගොනුවෙන් උපුටා ගැනීම:
```java
package com.cinemaapp.cinema.exception;

import com.cinemaapp.cinema.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ApiErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public ApiErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void writeError(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {

        ApiErrorResponse error = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
```
* **විග්‍රහය:** `@RestControllerAdvice` (`GlobalExceptionHandler`) ක්‍රියාත්මක වන්නේ Controller තුළින් එන exceptions වලට පමණි. `JwtAuthenticationFilter` සහ `SecurityConfig` හි `authenticationEntryPoint` / `accessDeniedHandler` ක්‍රියාත්මක වන්නේ Controller එකට **පෙර** Servlet Filter මට්ටමේදී බැවින්, JSON error response එක කෙලින්ම `HttpServletResponse` එකට ලිවීමට `ApiErrorResponseWriter` නිර්මාණය කර ඇත.

#### `ApiErrorResponse.java`
`backend/src/main/java/com/cinemaapp/cinema/dto/ApiErrorResponse.java` ගොනුවෙන් උපුටා ගැනීම:
```java
package com.cinemaapp.cinema.dto;

import java.time.LocalDateTime;

public class ApiErrorResponse {

    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;

    public ApiErrorResponse(
            int status,
            String error,
            String message,
            String path
    ) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}
```
* **විග්‍රහය:** සියලුම API දෝෂ ප්‍රතිචාර (`timestamp`, `status`, `error`, `message`, `path`) සඳහා පොදු JSON ආකෘතිය ලබා දෙයි.

#### `GlobalExceptionHandler.java`
`backend/src/main/java/com/cinemaapp/cinema/exception/GlobalExceptionHandler.java` ගොනුවෙන් උපුටා ගැනීම:
```java
package com.cinemaapp.cinema.exception;

import com.cinemaapp.cinema.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException exception) {

        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(error -> {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(errors);
    }

    // Handle resources that do not exist
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Handle database constraint violations
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDatabaseConflict(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "This operation cannot be completed because the record is referenced by other records or violates a database constraint.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // Handle invalid or unreadable JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRequestBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        ApiErrorResponse error = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Request body is missing, contains invalid JSON, or has incompatible field values.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    // Handle custom HTTP errors from services
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(
            ResponseStatusException exception,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(
                exception.getStatusCode().value()
        );

        ApiErrorResponse error = new ApiErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                exception.getReason() != null
                        ? exception.getReason()
                        : "Request could not be completed.",
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(error);
    }
}
```
* **Auth සඳහා වැදගත් වන Handlers:**
  - `MethodArgumentNotValidException` -> `@Valid` අසමත් වූ විට `400 Bad Request` සමඟ `{"field": "message"}` Map එක යවයි.
  - `ResponseStatusException` -> `AuthService` විසින් විසි කරන `409 Conflict` (duplicate email / one cinema) සහ `401 Unauthorized` (wrong credentials) යන errors `ApiErrorResponse` JSON එකක් බවට හරවයි.
  - `DataIntegrityViolationException` -> DB constraint එකක් (උදා: unique email) කැඩුණහොත් `409 Conflict` යවයි.
  - `HttpMessageNotReadableException` -> වැරදි හෝ හිස් JSON body එකක් එවූ විට `400 Bad Request` යවයි.

---

### 4.9 Day 3 හි `Cinema` Entity සහ `CinemaRepository` සමඟ සම්බන්ධතාවය (1-Line Day 3 Recap)
* **`Cinema.java`** (`backend/src/main/java/com/cinemaapp/cinema/entity/Cinema.java`): Day 3 හි සෑදූ මෙම Entity එකෙහි `email` (unique, max 100) සහ `password` (not null, max 100 - BCrypt hash සඳහා) fields Day 4 auth ක්‍රියාවලියේදී භාවිතා වේ.
* **`CinemaRepository.java`** (`backend/src/main/java/com/cinemaapp/cinema/repository/CinemaRepository.java`): Day 3 හි සෑදූ මෙම Repository එකේ ඇති `findByEmail(String email)`, `existsByEmail(String email)` සහ built-in `count()`, `save()` methods `AuthService` හි register සහ login සඳහා කෙලින්ම භාවිතා කෙරේ.

### මේක මට පැහැදිලිව කියන්න පුළුවන්ද?
1. **`SecurityConfig` හි `JwtAuthenticationFilter` එක `@Component` ලෙස නොදා `new JwtAuthenticationFilter(...)` මඟින් සාදා ඇත්තේ ඇයි?**
   - *පිළිතුර:* `OncePerRequestFilter` එකක් `@Component` කළහොත් Spring Boot එය Servlet filter chain එකටත් Security filter chain එකටත් දෙවරක් ලියාපදිංචි කරයි. `SecurityConfig` ඇතුළත `new` කර `.addFilterBefore(...)` කිරීමෙන් එය Security filter chain එක තුළ පමණක් එක් වරක් run වේ.
2. **Spring Security Filter එකකදී හටගන්නා 401/403 දෝෂ හැසිරවීමට `GlobalExceptionHandler` වෙනුවට `ApiErrorResponseWriter` භාවිතා කරන්නේ ඇයි?**
   - *පිළිතුර:* `@RestControllerAdvice` (`GlobalExceptionHandler`) අල්ලා ගන්නේ Controller layer එක තුළින් මතු වන exceptions පමණි. `JwtAuthenticationFilter` සහ `SecurityConfig` ක්‍රියාත්මක වන්නේ Controller එකට ළඟා වීමටත් පෙර Filter මට්ටමේදී බැවින් `HttpServletResponse` වෙත කෙලින්ම JSON ලියන `ApiErrorResponseWriter` අවශ්‍ය වේ.
3. **Stateless API එකක CSRF protection disable කිරීම ආරක්ෂිත වන්නේ ඇයි?**
   - *පිළිතුර:* CSRF ප්‍රහාර සිදුවන්නේ browser එක ස්වයංක්‍රීයව cookies යවන විටය. අපගේ API එක session cookies භාවිතා නොකරන (`SessionCreationPolicy.STATELESS`) අතර JavaScript/client මඟින් පැහැදිලිව `Authorization: Bearer <token>` header එක යවන බැවින් CSRF අවදානමක් නොමැත.
4. **`JwtService` constructor එකෙහි `keyBytes.length < 32` පරීක්ෂා කරන්නේ ඇයි?**
   - *පිළිතුර:* HMAC-SHA256 (HS256) algorithm එකට ආරක්ෂිතව අත්සන් කිරීම සඳහා අවම වශයෙන් bits 256 (bytes 32) ක secret key එකක් අවශ්‍ය වන බැවිනි.


---

## 5. Password Hashing: BCrypt ක්‍රියාත්මක වන ආකාරය (Worked Example)

අපගේ `PasswordConfig.java` හි `new BCryptPasswordEncoder()` භාවිතා කරන අතර, `AuthService.java` හි register වන විට `passwordEncoder.encode(request.getPassword())` මඟින් මුරපදය hash කර database එකේ ගබඩා කරයි.

### BCrypt Hashing සහ Encryption අතර වෙනස
* **Encryption** යනු යතුරක් භාවිතයෙන් නැවත මුල් දත්තය බවට පත් කළ හැකි (Reversible) ක්‍රියාවලියකි.
* **Hashing (BCrypt)** යනු ආපසු හැරවිය නොහැකි (One-way / Irreversible) ක්‍රියාවලියකි. Database එක hacker කෙනෙකු අතට පත් වුවද, hash එකෙන් මුල් password එක ආපසු ලබා ගත නොහැක.

### BCrypt String එකක සැබෑ ව්‍යුහය (Worked Example)
උදාහරණයක් ලෙස පරිශීලකයා `"CinemaPass@2026"` යන password එක register කළ විට, `BCryptPasswordEncoder` මඟින් පහත ආකාරයේ අක්ෂර 60 ක string එකක් නිපදවයි:

```text
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

මෙම BCrypt hash string එක ප්‍රධාන කොටස් 4 කට බෙදේ:

| කොටස (Part) | උදාහරණයේ අගය | තේරුම (Meaning) |
|---|---|---|
| **1. Version** | `$2a$` | BCrypt ඇල්ගොරිතමයේ සංස්කරණය (`$2a$`, `$2b$` ආදිය). `BCryptPasswordEncoder` හි පෙරනිමි version එක `$2a$` වේ. |
| **2. Cost Factor (Rounds)** | `10$` | වැඩ කිරීමේ බරපතලකම (Work factor). `10` යනු hashing ක්‍රියාවලිය $2^{10} = 1,024$ වාරයක් නැවත නැවත (iterations) ධාවනය වන බවයි. මෙමඟින් Brute-force ප්‍රහාර ඉතා මන්දගාමී කරයි. |
| **3. Salt (ලුණු/අහඹු දත්ත)** | `N9qo8uLOickgx2ZMRZoMye` (මුල් අක්ෂර 22) | 128-bit අහඹු (random) අගයකි. සෑම password එකක් hash කිරීමේදීම අලුත් salt එකක් උත්පාදනය වී hash එක තුළම තැන්පත් වේ. |
| **4. Hash (සැබෑ Hash අගය)** | `IjZAgcfl7p92ldGxad68LJZdL17lhWy` (අවසන් අක්ෂර 31) | Password එක සහ ඉහත Salt එක එකතු කර $2^{10}$ වාරයක් hash කිරීමෙන් ලැබෙන අවසාන ප්‍රතිඵලයයි. |

### එකම Password එකට වෙනස් Hashes දෙකක් ලැබෙන්නේ ඇයි?
පරිශීලකයන් දෙදෙනෙකු එකම password එක (`"CinemaPass@2026"`) ලබා දුන්නද, `passwordEncoder.encode("CinemaPass@2026")` දෙවරක් කැඳවූ විට සම්පූර්ණයෙන්ම වෙනස් strings දෙකක් ලැබේ:
* පළමු වර: `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`
* දෙවන වර: `$2a$10$X7vR2kMPqrst89ZABcDeFgHjKlMnOpQrStUvWxYz0123456789AbC`

**හේතුව:** `BCryptPasswordEncoder` විසින් සෑම `encode()` ඇමතුමකදීම අලුත් අහඹු **Salt** අගයක් ජනනය කරයි. Salt එක වෙනස් වන නිසා අවසාන Hash එකද වෙනස් වේ. මෙමඟින් Rainbow Table ප්‍රහාර සම්පූර්ණයෙන්ම වළකී.

### Login වීමේදී `matches()` ක්‍රියාත්මක වන ආකාරය
`AuthService.java` (පේළි 89-92) හි Login වන විට පහත code එක ක්‍රියාත්මක වේ:
```java
boolean passwordMatches = passwordEncoder.matches(
        request.getPassword(),
        cinema.getPassword()
);
```
1. `cinema.getPassword()` මඟින් PostgreSQL වෙතින් `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy` ලබා ගනී.
2. `matches()` ක්‍රමවේදය එම hash string එකේ මුල ඇති Version (`$2a$`), Cost (`10`), සහ **22-character Salt (`N9qo8uLOickgx2ZMRZoMye`)** වෙන් කර කියවා ගනී.
3. ඉන්පසු Login වීමේදී user එවූ raw password එක (`request.getPassword()`) එම **පැරණි Salt එකම** භාවිතයෙන් නැවත $2^{10}$ වාරයක් hash කරයි.
4. අලුතින් සෑදුණු hash එක database එකේ ඇති hash එකට සමාන නම් `true` ලබා දෙයි; නැතිනම් `false` ලබා දෙයි.

### මේක මට පැහැදිලිව කියන්න පුළුවන්ද?
1. **Password එකක් database එකේ Encrypt නොකර BCrypt මඟින් Hash කරන්නේ ඇයි?**
   - *පිළිතුර:* Encryption ආපසු decrypt කළ හැකි බැවින් secret key එක leak වුවහොත් සියලු passwords හෙළි වේ. BCrypt hashing යනු one-way ක්‍රියාවලියක් නිසා කිසිවෙකුටත් මුල් password එක ආපසු ගත නොහැක.
2. **BCrypt string එකක් තුළ Salt එක ගබඩා වී තිබීමෙන් Login වීමේදී ඇති වන ප්‍රයෝජනය කුමක්ද?**
   - *පිළිතුර:* Salt එක database එකේ වෙනම column එකක store කිරීමට අවශ්‍ය නොවේ. `matches()` method එකට සම්පූර්ණ string එකෙන්ම version, cost සහ salt එක කියවා raw password එක සමඟ සැසඳිය හැක.
3. **Cost factor `10` වෙනුවට `12` හෝ `14` දැමුවහොත් කුමක් සිදුවේද?**
   - *පිළිතුර:* Hashing සඳහා ගත වන CPU කාලය 4x හෝ 16x ගුණයකින් වැඩි වේ. ආරක්ෂාව වැඩි වුවත් Login/Register requests වල ප්‍රතිචාර කාලය (latency) මන්දගාමී විය හැක.

---

## 6. JWT (JSON Web Token) ගැඹුරු විග්‍රහය

`JwtService.java` හි `generateToken(Long cinemaId)` මඟින් නිපදවන JWT එක තිත් (`.`) දෙකකින් වෙන් වූ කොටස් 3 කින් සමන්විත වේ:
`Header.Payload.Signature`

### 6.1 අපගේ කේතය මඟින් සාදන සැබෑ Token එකක Worked Example

`JwtService.java` (පේළි 44-49):
```java
return Jwts.builder()
        .subject(String.valueOf(cinemaId))
        .issuedAt(issuedAt)
        .expiration(expiration)
        .signWith(signingKey)
        .compact();
```

`cinemaId = 1L` සඳහා token එකක් සෑදූ විට (උදා: Base64URL encoded):
```text
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzMyNTI2MDAwLCJleHAiOjE3MzI1Mjk2MDB9.K7x8L9mNpQrStUvWxYz0123456789AbCdEfGhIjKlMn
```

මෙම කොටස් 3 Base64URL decode කළ විට පෙනෙන සැබෑ JSON දත්ත පහත පරිදි වේ:

1. **Header (`eyJhbGciOiJIUzI1NiJ9`):**
   ```json
   {
     "alg": "HS256"
   }
   ```
   * `signingKey` එක 32 bytes (256 bits) වන නිසා JJWT විසින් ස්වයංක්‍රීයව `HS256` (HMAC using SHA-256) ඇල්ගොරිතමය තෝරා ගනී.

2. **Payload / Claims (`eyJzdWIiOiIxIiwiaWF0IjoxNzMyNTI2MDAwLCJleHAiOjE3MzI1Mjk2MDB9`):**
   ```json
   {
     "sub": "1",
     "iat": 1732526000,
     "exp": 1732529600
   }
   ```
   * **`sub` (Subject):** `String.valueOf(cinemaId)` — සිනමා ශාලාවේ ID අංකය (`"1"`).
   * **`iat` (Issued At):** Token එක නිකුත් කළ Unix epoch තත්පර වේලාව.
   * **`exp` (Expiration):** Token එක කල් ඉකුත් වන වේලාව (`iat + 3600 seconds`, එනම් `expirationMs = 3600000`).
   * **වැදගත් සටහන:** අපගේ කේතය JWT payload එක තුළ `email`, `name`, `role` හෝ `password` කිසිවක් **ඇතුළත් නොකරයි**. ඇතුළත් කරන්නේ `sub`, `iat`, `exp` පමණි! (JWT payload එක Base64URL encoded පමණක් වන අතර encrypted නැති නිසා කිසිවෙකුටත් එය decode කර කියවිය හැක; එනිසා සංවේදී දත්ත නොදැමීම හොඳ පුරුද්දකි).

3. **Signature (`K7x8L9mNpQrStUvWxYz0123456789AbCdEfGhIjKlMn`):**
   * මෙය සාදන්නේ Header සහ Payload කොටස් දෙක `.` මඟින් එකතු කර, අපගේ රහස් `signingKey` (`JWT_SECRET` Base64 decoded bytes) භාවිතයෙන් **HMAC-SHA256** ගණනය කිරීමෙනි:
     $$\text{Signature} = \text{HMAC-SHA256}\big(\text{base64Url}(Header) + "." + \text{base64Url}(Payload),\ \text{signingKey}\big)$$
   * **රහස් යතුර (`JWT_SECRET`) නොමැතිව කිසිවෙකුටත් valid signature එකක් හැදිය නොහැක.**

### 6.2 `JwtAuthenticationFilter` සහ `JwtService` මඟින් Token එක Validate කරන ආකාරය
1. `JwtAuthenticationFilter` මඟින් `Authorization` header එකෙන් `"Bearer "` ඉවත් කර token string එක ලබා ගනී.
2. `jwtService.extractCinemaId(token)` තුළ `Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token)` ක්‍රියාත්මක වේ.
3. JJWT parser එක විසින් පියවර 3 ක් පරීක්ෂා කරයි:
   - Token එකේ ආකෘතිය (`header.payload.signature`) නිවැරදිද?
   - `signingKey` භාවිතයෙන් නැවත ගණනය කරන signature එක token එකේ signature එකට සමානද?
   - `exp` timestamp එක වත්මන් වේලාවට වඩා ඉදිරියෙන් තිබේද (not expired)?
4. සියල්ල සාර්ථක නම් `sub` අගය `Long` බවට හරවා `cinemaId` ලබා දෙන අතර, filter එක `SecurityContextHolder` හි `UsernamePasswordAuthenticationToken` තැන්පත් කරයි.

### 6.3 Token එක Expired, Tampered හෝ Missing වූ විට සිදුවන දේ (401 vs 403)

| අවස්ථාව (Scenario) | කේතයේ හැසිරීම | HTTP Status Code | JSON Error Message |
|---|---|---|---|
| **Token Expired** (පැය 1 ඉක්මවූ token එකක්) | `parseSignedClaims()` මඟින් `ExpiredJwtException` (which extends `JwtException`) විසි කරයි. `JwtAuthenticationFilter` හි `catch` block එකට හසු වේ. | **401 Unauthorized** | `"Invalid or expired JWT"` |
| **Token Tampered** (Payload එකේ `"sub":"2"` ලෙස වෙනස් කළ token එකක්) | Signature mismatch නිසා `SignatureException` (`JwtException` subclass) විසි වේ. Filter එකේ `catch` block එකට හසු වේ. | **401 Unauthorized** | `"Invalid or expired JWT"` |
| **Token Malformed / Empty** (`Bearer abc` හෝ `Bearer `) | `MalformedJwtException` හෝ `IllegalArgumentException` විසි වේ. Filter එකේ `catch` block එකට හසු වේ. | **401 Unauthorized** | `"Invalid or expired JWT"` |
| **Token Missing / Wrong Prefix** (`Authorization` header නැතිව හෝ `Bearer ` නැතිව protected URL එකකට යාම) | `JwtAuthenticationFilter` authentication set නොකර ඉදිරියට යවයි. `AuthorizationFilter` අවහිර කර `SecurityConfig` හි `authenticationEntryPoint` කැඳවයි. | **401 Unauthorized** | `"Authentication required"` |
| **Access Denied (403 Forbidden)** | User authenticate වී ඇත, නමුත් අදාළ endpoint එකට අවශ්‍ය role/permission නැති විට `accessDeniedHandler` ක්‍රියාත්මක වේ. | **403 Forbidden** | `"You do not have permission to perform this operation."` |

> [!NOTE]
> **401 vs 403 වෙනස:**
> * **401 Unauthorized (Unauthenticated):** "ඔබ කවුදැයි හඳුනාගත නොහැක" (Token එක නැත, වැරදිය, නැතිනම් expire වී ඇත).
> * **403 Forbidden (Access Denied):** "ඔබ කවුදැයි හඳුනාගෙන ඇත (valid token), නමුත් මෙම ක්‍රියාව කිරීමට ඔබට අවසර නැත". (අපගේ වත්මන් `SecurityConfig` හි role-based rules නොමැති නිසා `accessDeniedHandler` සාමාන්‍ය API requests වලදී ක්‍රියාත්මක නොවේ).

### මේක මට පැහැදිලිව කියන්න පුළුවන්ද?
1. **JWT payload එකේ ඇති `sub` අගය කෙනෙකුට decode කර කියවිය හැකිද? එසේ නම් token එක ආරක්ෂිත වන්නේ කෙසේද?**
   - *පිළිතුර:* ඔව්, JWT header සහ payload කොටස් encrypt කර නැත, Base64URL encode කර පමණි. එනිසා ඕනෑම කෙනෙකුට `jwt.io` හරහා payload එක කියවිය හැක. නමුත් එහි ඇති `sub: "1"` කිසිවෙකු වෙනස් කළහොත් (`"2"` කළහොත්), server එක සතු `JWT_SECRET` නොමැතිව signature එක නිවැරදිව සෑදිය නොහැකි නිසා server එක එම tampered token එක `401 Unauthorized` ලෙස ප්‍රතික්ෂේප කරයි.
2. **Token එකක් `Authorization` header එකෙන් නොයවා protected endpoint එකකට request එකක් දැමුවහොත් 401 දෙන code එක කොතැනද ඇත්තේ?**
   - *පිළිතුර:* `JwtAuthenticationFilter` නොවේ! Header එක නැති විට filter එක ඉදිරියට යන අතර, `AuthorizationFilter` මඟින් access ප්‍රතික්ෂේප කළ විට `SecurityConfig.java` හි `.authenticationEntryPoint(...)` මඟින් `401 Unauthorized` (`"Authentication required"`) ප්‍රතිචාරය ලබා දෙයි.
3. **වැරදි signature එකක් ඇති token එකක් යැවූ විට 401 දෙන code එක කොතැනද ඇත්තේ?**
   - *පිළිතුර:* `JwtAuthenticationFilter.java` හි `catch (JwtException | IllegalArgumentException exception)` block එක තුළ `errorResponseWriter.writeError(..., HttpStatus.UNAUTHORIZED, "Invalid or expired JWT")` මඟින් ලබා දෙයි.

---

## 7. අපගේ කේතයේ Register සහ Login නීති (Register & Login Rules in Code)

### 7.1 Input Validation නීති (DTO Annotations)
`AuthController` හි `@Valid` annotation එක නිසා Controller method එක ක්‍රියාත්මක වීමට පෙර පහත නීති පරීක්ෂා කෙරේ. අසමත් වුවහොත් `GlobalExceptionHandler` මඟින් **400 Bad Request** ලැබේ:

* **`RegisterRequest.java` නීති:**
  - `name`: හිස් විය නොහැක (`@NotBlank`), උපරිම අක්ෂර 100 (`@Size(max = 100)`).
  - `address`: Optional (හිස් විය හැක), නමුත් එවන්නේ නම් උපරිම අක්ෂර 200 (`@Size(max = 200)`).
  - `email`: හිස් විය නොහැක (`@NotBlank`), වලංගු email ආකෘතිය (`@Email`), උපරිම අක්ෂර 100 (`@Size(max = 100)`).
  - `password`: හිස් විය නොහැක (`@NotBlank`), අවම අක්ෂර 8 සිට උපරිම 100 දක්වා (`@Size(min = 8, max = 100)`).
* **`LoginRequest.java` නීති:**
  - `email`: `@NotBlank`, `@Email`.
  - `password`: `@NotBlank` (මෙහි min length 8 rule එක නොමැත; `AuthService` හි `matches()` මඟින් පරීක්ෂා වේ).

### 7.2 Email Normalization (සාමාන්‍යකරණය)
* `AuthService.register()`: `String email = request.getEmail().trim().toLowerCase();`
* `AuthService.login()`: `String email = request.getEmail().trim().toLowerCase(Locale.ROOT);`
* **හේතුව:** පරිශීලකයා `" Admin@Cinema.lk "` ලෙස capital අකුරු හෝ spaces සහිතව register වී පසුව `"admin@cinema.lk"` ලෙස login වුවද, දෙකම එකම email එක ලෙස හඳුනා ගැනීමටයි.

### 7.3 Duplicate Email Handling (අනුපිටපත් Email පාලනය)
`AuthService.java` (පේළි 38-43):
```java
if (cinemaRepository.existsByEmail(email)) {
    throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "This email is already registered"
    );
}
```
* සමාන email එකක් ඇත්නම් **409 Conflict** (`"This email is already registered"`) ආපසු යවයි.
* තවද Database level එකේ `cinema` වගුවේ `email VARCHAR(100) NOT NULL UNIQUE` constraint එක ඇති බැවින් `GlobalExceptionHandler` හි `DataIntegrityViolationException` handler එක අතිරේක ආරක්ෂාවක් (fallback -> 409 Conflict) සපයයි.

### 7.4 One-Cinema Rule (එක් සිනමා ශාලා ගිණුමක් පමණක් තබා ගැනීමේ නීතිය)
`AuthService.java` (පේළි 45-51):
```java
// Only one cinema account is allowed
if (cinemaRepository.count() > 0) {
    throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "A cinema account has already been registered"
    );
}
```
* **මෙය ක්‍රියාත්මක වන ස්ථානය (Where it is enforced):** මෙම නීතිය ක්‍රියාත්මක වන්නේ **Java Service Layer එකේ (`AuthService.register()`)** පමණි; PostgreSQL database schema (`schema.sql`) එකේ මෙවැනි row-count constraint එකක් නොමැත.
* **හේතුව:** මෙම backend එක සකසා ඇත්තේ තනි සිනමා ශාලාවක් (Single-tenant cinema side) කළමනාකරණය කිරීමට බැවින්, පළමු ගිණුම ලියාපදිංචි වූ පසු වෙනත් email එකකින් වුවද දෙවන ගිණුමක් සෑදීම වළක්වා **409 Conflict** (`"A cinema account has already been registered"`) ලබා දෙයි.

### 7.5 Login අසාර්ථක වීමේ නීතිය (Account Enumeration Prevention)
`AuthService.java` (පේළි 82-99):
* Email එක database එකේ නොතිබුණද (`findByEmail` empty) හෝ Password එක වැරදි වුවද (`!passwordMatches`), දෙකේදීම ලබා දෙන්නේ එකම message එක සහ status code එකයි: **401 Unauthorized** — `"Invalid email or password"`.
* මෙමඟින් අනවසර පුද්ගලයෙකුට "මෙම email එක පද්ධතියේ තිබේද නැද්ද?" යන්න සොයා ගැනීම (User Enumeration Attack) වළක්වයි.

### මේක මට පැහැදිලිව කියන්න පුළුවන්ද?
1. **Register වීමේදී දැනටමත් ගිණුමක් තිබේ නම් වෙනත් email එකකින් දෙවන ගිණුමක් සෑදිය හැකිද? එය නවත්වන්නේ කොතැනින්ද?**
   - *පිළිතුර:* නොහැක. `AuthService.register()` තුළ ඇති `if (cinemaRepository.count() > 0)` පරීක්ෂාව මඟින් එය නවත්වා `409 Conflict` (`"A cinema account has already been registered"`) ලබා දෙයි. මෙය enforce කර ඇත්තේ Service layer එකේය.
2. **Login වන විට email එක වැරදි නම් "Email not found" කියා message එකක් නොදී "Invalid email or password" දෙන්නේ ඇයි?**
   - *පිළිතුර:* ආරක්ෂක හේතූන් මතය. "Email not found" යැයි කීවොත් ප්‍රහාරකයෙකුට email ලැයිස්තුවක් පරීක්ෂා කර සැබෑ admin email එක සොයා ගත හැක (Account Enumeration). එකම පොදු පණිවිඩයක් දීමෙන් එය වළකී.
3. **Register වන විට `password` එක අකුරු 5 ක් පමණක් යැවුවහොත් AuthService එකට request එක යනවාද?**
   - *පිළිතුර:* නැත. `AuthController` හි `@Valid` annotation එක මඟින් `RegisterRequest` හි `@Size(min = 8)` පරීක්ෂා කර `AuthService` වෙත යාමට පෙරම `MethodArgumentNotValidException` විසි කර `400 Bad Request` ලබා දෙයි.


---

## 8. Postman මඟින් Day 4 Endpoints පරීක්ෂා කිරීම (Step-by-Step Postman Testing)

අපගේ backend server එක ධාවනය වන්නේ `server.port=8082` මත බැවින් Base URL එක `http://localhost:8082` වේ.

### 8.1 Register Endpoint (`POST /api/auth/register`)

#### Case A: සාර්ථක ලියාපදිංචිය (`201 Created`)
* **Method & URL:** `POST http://localhost:8082/api/auth/register`
* **Headers:** `Content-Type: application/json` (Authorization header අවශ්‍ය නැත)
* **Request JSON Body:**
  ```json
  {
    "name": "Liberty Cinema",
    "address": "No. 35, Srimath Anagarika Dharmapala Mawatha, Colombo 03",
    "email": "admin@liberty.lk",
    "password": "CinemaPass@2026"
  }
  ```
* **Expected Status:** `201 Created`
* **Expected Response Body (`CinemaResponse`):**
  ```json
  {
    "id": 1,
    "name": "Liberty Cinema",
    "address": "No. 35, Srimath Anagarika Dharmapala Mawatha, Colombo 03",
    "email": "admin@liberty.lk"
  }
  ```

#### Case B: Validation දෝෂ (`400 Bad Request`)
* **Request JSON Body (වැරදි email සහ කෙටි password):**
  ```json
  {
    "name": "",
    "email": "not-an-email",
    "password": "123"
  }
  ```
* **Expected Status:** `400 Bad Request`
* **Expected Response Body (`GlobalExceptionHandler` Map):**
  ```json
  {
    "name": "Cinema name is required",
    "email": "Please provide a valid email address",
    "password": "Password must contain between 8 and 100 characters"
  }
  ```

#### Case C: Duplicate Email හෝ One-Cinema Rule උල්ලංඝනය (`409 Conflict`)
* **Request JSON Body:** දැනටමත් register වී ඇති email එකක් හෝ ගිණුමක් තිබියදී නව email එකකින් යැවීම.
* **Expected Status:** `409 Conflict`
* **Expected Response Body (`ApiErrorResponse`):**
  ```json
  {
    "timestamp": "2026-11-25T09:10:00.123456",
    "status": 409,
    "error": "Conflict",
    "message": "This email is already registered",
    "path": "/api/auth/register"
  }
  ```
  *(වෙනත් email එකකින් දෙවන ගිණුමක් සෑදීමට උත්සාහ කළහොත් `"message": "A cinema account has already been registered"` ලැබේ).*

---

### 8.2 Login Endpoint (`POST /api/auth/login`)

#### Case A: සාර්ථක Login (`200 OK`)
* **Method & URL:** `POST http://localhost:8082/api/auth/login`
* **Headers:** `Content-Type: application/json`
* **Request JSON Body:**
  ```json
  {
    "email": "admin@liberty.lk",
    "password": "CinemaPass@2026"
  }
  ```
* **Expected Status:** `200 OK`
* **Expected Response Body (`LoginResponse`):**
  ```json
  {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzMyNTI2MDAwLCJleHAiOjE3MzI1Mjk2MDB9.K7x8L9mNpQrStUvWxYz...",
    "tokenType": "Bearer"
  }
  ```

#### Case B: වැරදි Email හෝ Password (`401 Unauthorized`)
* **Request JSON Body:** `"password": "WrongPassword"`
* **Expected Status:** `401 Unauthorized`
* **Expected Response Body (`ApiErrorResponse`):**
  ```json
  {
    "timestamp": "2026-11-25T09:12:00.123456",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid email or password",
    "path": "/api/auth/login"
  }
  ```

---

### 8.3 Protected Endpoint පරීක්ෂාව (With and Without JWT Token)

Day 4 හි ආරක්ෂාව පරීක්ෂා කිරීමට protected endpoint එකක් ලෙස `GET /api/cinema/profile` (හෝ `GET /api/movies`) භාවිතා කළ හැක:

#### Case A: Token නොමැතිව ඇමතීම (`401 Unauthorized`)
* **Method & URL:** `GET http://localhost:8082/api/cinema/profile`
* **Headers:** `Authorization` header එක නොමැත.
* **Expected Status:** `401 Unauthorized`
* **Expected Response Body (`SecurityConfig` `authenticationEntryPoint`):**
  ```json
  {
    "timestamp": "2026-11-25T09:15:00.123456",
    "status": 401,
    "error": "Unauthorized",
    "message": "Authentication required",
    "path": "/api/cinema/profile"
  }
  ```

#### Case B: වැරදි හෝ Expired Token එකක් සමඟ ඇමතීම (`401 Unauthorized`)
* **Method & URL:** `GET http://localhost:8082/api/cinema/profile`
* **Headers:** `Authorization: Bearer wrong.token.value`
* **Expected Status:** `401 Unauthorized`
* **Expected Response Body (`JwtAuthenticationFilter` catch block):**
  ```json
  {
    "timestamp": "2026-11-25T09:16:00.123456",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid or expired JWT",
    "path": "/api/cinema/profile"
  }
  ```

#### Case C: වලංගු Bearer Token එකක් සමඟ ඇමතීම (`200 OK`)
* **Method & URL:** `GET http://localhost:8082/api/cinema/profile`
* **Headers:** `Authorization: Bearer <Login මඟින් ලැබුණු accessToken>`
  *(Postman හි **Authorization** tab එකේ Type = `Bearer Token` තෝරා token එක paste කළ හැක).*
* **Expected Status:** `200 OK`
* **Expected Response Body:**
  ```json
  {
    "id": 1,
    "name": "Liberty Cinema",
    "address": "No. 35, Srimath Anagarika Dharmapala Mawatha, Colombo 03",
    "email": "admin@liberty.lk"
  }
  ```

#### `403 Forbidden` ලැබෙන්නේ කවදාද?
* `SecurityConfig.java` හි `accessDeniedHandler` මඟින් `403 Forbidden` (`"You do not have permission to perform this operation."`) සකසා ඇත.
* කෙසේ වෙතත්, වත්මන් `SecurityConfig` හි ඇත්තේ `.anyRequest().authenticated()` පමණක් වන අතර `.hasRole(...)` වැනි role restrictions නොමැති නිසා, valid JWT එකක් ඇති ඕනෑම cinema ගිණුමකට protected endpoints සඳහා 403 ලැබෙන්නේ නැත (අනාගතයේ role-based endpoints එක් කළහොත් පමණක් 403 ක්‍රියාත්මක වේ).

### මේක මට පැහැදිලිව කියන්න පුළුවන්ද?
1. **Register endpoint එකට `400 Bad Request` සහ `409 Conflict` ලැබෙන JSON responses වල ව්‍යුහය සමානද?**
   - *පිළිතුර:* නැත! `GlobalExceptionHandler` හි `MethodArgumentNotValidException` (400) මඟින් `{"field": "message"}` ආකාරයේ `Map` එකක් ලබා දෙන අතර, `ResponseStatusException` (409) මඟින් `timestamp, status, error, message, path` සහිත `ApiErrorResponse` object එකක් ලබා දෙයි.
2. **Postman හි protected endpoint එකකට Token එක යැවිය යුතු නිවැරදි Header නම සහ ආකෘතිය කුමක්ද?**
   - *පිළිතුර:* Header නම `Authorization` වන අතර අගය `Bearer <accessToken>` ලෙස තිබිය යුතුය (`Bearer` සහ token අතර හරියටම එක් space එකක් තිබිය යුතුය).
3. **Protected endpoint එකකට `Authorization` header නැතිව යැවූ විට සහ වැරදි token එකක් යැවූ විට ලැබෙන error messages වෙනස් වන්නේ ඇයි?**
   - *පිළිතුර:* Header නැති විට `SecurityConfig` හි `authenticationEntryPoint` මඟින් `"Authentication required"` ලබා දෙයි. වැරදි token එකක් යැවූ විට `JwtAuthenticationFilter` තුළදීම catch වී `"Invalid or expired JWT"` ලබා දෙයි.

---

## 9. පසු දිනවලදී (Later Days) JWT Token එක භාවිතා කරන ආකාරය

අපගේ කේත පදනමේ දැනට පවතින පසු දිනවල controllers පරීක්ෂා කළ විට, Day 4 හි නිර්මාණය කළ JWT පද්ධතිය භාවිතා වන ආකාර දෙකක් දැකගත හැක:

### 9.1 Profile API (`CinemaProfileController`) — `@AuthenticationPrincipal` මඟින් `cinemaId` කියවීම
`JwtAuthenticationFilter` (පේළි 58-65) මඟින් token එකේ ඇති `cinemaId` (`Long`) අගය `UsernamePasswordAuthenticationToken` හි **Principal** ලෙස `SecurityContextHolder` එකේ තැන්පත් කරයි.

`backend/src/main/java/com/cinemaapp/cinema/controller/CinemaProfileController.java` (පේළි 27-50) ගොනුවෙන් උපුටා ගැනීම:
```java
    // Get cinema profile
    @GetMapping
    public ResponseEntity<CinemaResponse> getProfile(
            @AuthenticationPrincipal Long cinemaId
    ) {

        CinemaResponse response =
                cinemaProfileService.getProfile(cinemaId);

        return ResponseEntity.ok(response);
    }

    // Update cinema profile
    @PutMapping
    public ResponseEntity<CinemaResponse> updateProfile(
            @AuthenticationPrincipal Long cinemaId,
            @Valid @RequestBody UpdateCinemaProfileRequest request
    ) {

        CinemaResponse response =
                cinemaProfileService.updateProfile(cinemaId, request);

        return ResponseEntity.ok(response);
    }
```
* **ක්‍රියාත්මක වන ආකාරය:** URL එකේ `/api/cinema/profile/1` ලෙස ID එක යැවීමට අවශ්‍ය නොවේ! `@AuthenticationPrincipal Long cinemaId` මඟින් Spring Security Context එකෙන් කෙලින්ම logged-in cinema ID එක ලබා ගෙන `CinemaProfileService` වෙත යවයි. එම නිසා වෙනත් අයෙකුගේ profile ID එකක් URL එකෙන් මාරු කර බැලීමට (IDOR vulnerability) නොහැක.

### 9.2 Movies, Posters සහ Showtimes APIs (`MovieController`, `MoviePosterController`, `ShowtimeController`)
අපගේ සැබෑ කේතය පරීක්ෂා කළ විට පෙනී යන වැදගත් කරුණක් ඇත:
* **Security Filter ආරක්ෂාව:** `SecurityConfig` හි `.anyRequest().authenticated()` නිසා `/api/movies/**`, `/api/showtimes/**`, සහ `/api/movies/{movieId}/poster` (POST, PUT, DELETE) යන සියලුම endpoints වෙත පිවිසීමට **වලංගු JWT Bearer token එකක් අනිවාර්ය වේ**. (`GET /api/movies/*/poster` පමණක් `permitAll()` මඟින් පොදු කර ඇත).
* **`cinemaId` කියවීම සිදු නොවීම (Visible in Code):** `CinemaProfileController` මෙන් නොව, `MovieController`, `MoviePosterController`, සහ `ShowtimeController` යන classes වල කිසිදු method එකක් `@AuthenticationPrincipal Long cinemaId` භාවිතා නොකරයි.
* **හේතුව:** අපගේ පද්ධතිය Single-cinema model එකක් (`AuthService` හි `count() > 0` මඟින් එක් සිනමා ශාලාවක් පමණක් ලියාපදිංචි කිරීම) අනුගමනය කරන බැවින් `movie` සහ `showtime` වගු වල `cinema_id` column එකක් නොමැත. එනිසා මෙම endpoints token එක භාවිතා කරන්නේ **Access Control (Authentication Gate)** එකක් ලෙස පමණි.

### මේක මට පැහැදිලිව කියන්න පුළුවන්ද?
1. **`CinemaProfileController` හි `GET /api/cinema/profile` වෙත ID එක URL එකෙන් නොයවා profile එක සොයා ගන්නේ කෙසේද?**
   - *පිළිතුර:* `JwtAuthenticationFilter` මඟින් JWT එකෙන් `cinemaId` කියවා `SecurityContext` එකේ principal ලෙස තබයි. `CinemaProfileController` හි `@AuthenticationPrincipal Long cinemaId` මඟින් එම ID එක ස්වයංක්‍රීයව ලබා ගනී.
2. **`MovieController` සහ `ShowtimeController` JWT token එක භාවිතා කරන ආකාරය `CinemaProfileController` ට වඩා වෙනස් වන්නේ කෙසේද?**
   - *පිළිතුර:* `CinemaProfileController` token එකෙන් `cinemaId` කියවා දත්ත query කරයි. නමුත් `MovieController` සහ `ShowtimeController` `cinemaId` කියවන්නේ නැත; ඒවා token එක භාවිතා කරන්නේ logged-in cinema manager කෙනෙකු බව තහවුරු කිරීමේ ආරක්ෂක දොරටුවක් ලෙස පමණි.
3. **චිත්‍රපට පෝස්ටරයක් බැලීමට (`GET /api/movies/1/poster`) JWT token එකක් අවශ්‍යද?**
   - *පිළිතුර:* නැත. `SecurityConfig.java` (පේළි 56-59) හි `HttpMethod.GET, "/api/movies/*/poster"` සඳහා `.permitAll()` ලබා දී ඇති බැවින් එය පොදු (public) වේ.

---

## 10. පොදු දෝෂ සහ විසඳුම් (Common Errors and Fixes)

### 10.1 Register වීමේදී 401 / 403 ලැබීම (Security Blocking Register)
* **ගැටලුව:** `POST /api/auth/register` වෙත request එකක් යැවූ විට Controller එකට නොගොස් `401 Unauthorized` හෝ `403 Forbidden` ලැබීම.
* **හේතුව:** `SecurityConfig.java` හි `.requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()` පේළිය මඟ හැරීම, URL path එක වැරදි වීම (උදා: `/auth/register` ලෙස ලිවීම), හෝ `.csrf(csrf -> csrf.disable())` අමතක වීම (CSRF active නම් POST requests 403 ලෙස block වේ).
* **විසඳුම:** `SecurityConfig` හි CSRF disable කර ඇත්දැයි සහ `AuthController` හි `@RequestMapping("/api/auth")` path එක `permitAll()` rule එකට හරියටම ගැළපේදැයි පරීක්ෂා කරන්න.

### 10.2 401 Unauthorized vs 403 Forbidden පැටලැවීම
* **ගැටලුව:** Token එකක් නැතිව හෝ expired token එකක් යැවූ විට 403 බලාපොරොත්තු වීම (හෝ Spring default behavior එක ලෙස 403 ලැබීම).
* **හේතුව:** Spring Security හි `authenticationEntryPoint` (401 සඳහා) configure නොකළ විට, සමහර අවස්ථාවලදී `Http403ForbiddenEntryPoint` මඟින් 403 ආපසු යවයි.
* **විසඳුම:** අපගේ `SecurityConfig` හි `.authenticationEntryPoint(...)` මඟින් `HttpStatus.UNAUTHORIZED` (401) ද, `.accessDeniedHandler(...)` මඟින් `HttpStatus.FORBIDDEN` (403) ද පැහැදිලිව වෙන් කර ඇත. 401 යනු login/token ගැටලුවක් බවත් 403 යනු අවසර (permission) ගැටලුවක් බවත් මතක තබා ගන්න.

### 10.3 JWT Secret Key එක කෙටි වීම හෝ Load නොවීම
* **දෝෂය:**
  ```text
  java.lang.IllegalArgumentException: JWT secret must contain at least 32 bytes
  ```
  හෝ `Could not resolve placeholder 'JWT_SECRET' in value "${JWT_SECRET}"`
* **හේතුව:**
  1. `.env` හි `JWT_SECRET` Base64 decode කළ විට bytes 32 ට වඩා අඩු වීම (`JwtService.java` පේළි 26-30).
  2. Spring Boot run කළ working directory එක `backend/` නොවීම නිසා `optional:file:../.env[.properties]` හට `.env` ගොනුව සොයා ගත නොහැකි වීම.
* **විසඳුම:** අවම වශයෙන් අක්ෂර 44 ක Base64-encoded 256-bit key එකක් `.env` හි `JWT_SECRET` ලෙස දමා `backend/` ෆෝල්ඩරය තුළ සිට application එක run කරන්න (හෝ OS environment variable එකක් ලෙස `JWT_SECRET` export කරන්න).

### 10.4 Token එක `"Bearer <token>"` ලෙස නොයැවීම
* **ගැටලුව:** `Authorization` header එකට `"Bearer "` නොදා කෙලින්ම `eyJhbG...` ලෙස token එක පමණක් යැවීම.
* **කේතයේ සිදුවන දේ:** `JwtAuthenticationFilter` හි `!authorizationHeader.startsWith("Bearer ")` සත්‍ය වන නිසා token එක නොසලකා හරියි. එවිට `401 Unauthorized` — `"Authentication required"` ලැබේ. (එමෙන්ම `"Bearer"` පමණක් හෝ token එකට පෙර අමතර spaces දැමුවහොත් `substring(7)` වැරදි token එකක් ගෙන `"Invalid or expired JWT"` ලබා දෙයි).
* **විසඳුම:** සෑම විටම `Authorization: Bearer <token>` ආකෘතිය භාවිතා කරන්න.

### 10.5 Token Expiry කාලය පිළිබඳ වැරදි වැටහීම්
* **ගැටලුව:** `app.jwt.expiration-ms=3600` ලෙස සිතා තැබීම.
* **හේතුව:** `JwtService` හි `new Date(issuedAt.getTime() + expirationMs)` ගණනය කරන්නේ **මිලිතත්පර (milliseconds)** වලිනි. `3600` දැමුවහොත් token එක තත්පර 3.6 කින් expire වේ!
* **විසඳුම:** පැය 1 ක් සඳහා $60 \times 60 \times 1000 = 3600000$ ලෙස තබන්න.

### 10.6 Double-Encoded Passwords (මුරපද දෙවරක් Hash වීම)
* **ගැටලුව:** Register වී නිවැරදි password එක ගැසුවත් login වීමේදී `401 Invalid email or password` ලැබීම.
* **හේතුව:**
  1. Service layer එකේදී `passwordEncoder.encode()` දෙවරක් කැඳවීම, නැතහොත්
  2. SQL script එකකින් හෝ අතින් database එකට plain text password එකක් ඇතුළත් කිරීම (BCrypt hash එකක් නොවන නිසා `passwordEncoder.matches()` සැමවිටම `false` දෙයි, තවද log වල `Encoded password does not look like BCrypt` warning එකක් පෙන්වයි).
* **විසඳුම:** `AuthService.register()` හි එක් වරක් පමණක් `passwordEncoder.encode(...)` භාවිතා වන බවට සහ database එකේ `$2a$10$...` ආකෘතියේ hash එකක් ගබඩා වී ඇති බවට තහවුරු කර ගන්න.

### මේක මට පැහැදිලිව කියන්න පුළුවන්ද?
1. **Database එකට අතින් (SQL INSERT මඟින්) plain password එකක් දමා Login වීමට උත්සාහ කළහොත් කුමක් සිදුවේද?**
   - *පිළිතුර:* `BCryptPasswordEncoder.matches()` මඟින් database එකේ ඇති password එක BCrypt format එකේ නොමැති බව හඳුනාගෙන warning එකක් සමඟ `false` ආපසු දෙයි. එවිට `AuthService.login()` මඟින් `401 Unauthorized` ලැබේ.
2. **Backend එක root directory එකේ සිට `java -jar backend/build/libs/...` ලෙස run කළහොත් `JWT_SECRET` error එකක් ඒමට ඉඩ ඇත්තේ ඇයි?**
   - *පිළිතුර:* `application.properties` හි ඇත්තේ `optional:file:../.env[.properties]` වන නිසා, root directory එකේ සිට run කරන විට `../.env` මඟින් root එකටත් ඉහළ directory එකේ `.env` සොයයි. එවිට `JWT_SECRET` environment variable එක OS එකේ set කර නොමැති නම් startup fail වේ.
3. **Register endpoint එකට 403 Forbidden ලැබීමට බලපාන ප්‍රධාන Spring Security හේතු 2 මොනවාද?**
   - *පිළිතුර:* (1) `SecurityConfig` හි CSRF protection disable නොකර තිබීම, (2) `/api/auth/register` සඳහා `.permitAll()` නිවැරදිව යොදා නොතිබීම.


---

## 11. Mentor හට ඉදිරිපත් කිරීම සඳහා කථන පිටපත සහ Live Demo පිරික්සුම් ලැයිස්තුව (Spoken Script & Demo Checklist)

### 11.1 විනාඩි 3 සිට 5 දක්වා සිංහල කථන පිටපත (Spoken Script for Mentor)

> "ආයුබෝවන් සර් / මෙන්ටර්!
> 
> අද මම ඉදිරිපත් කරන්නේ අපේ **Cine Cinema** backend ව්‍යාපෘතියේ **Day 4** හි සිදු කළ Authentication සහ Spring Security කටයුතු පිළිබඳවයි.
> 
> Day 3 වලදී අපි `Cinema` entity එක සහ `CinemaRepository` එක නිර්මාණය කළා. Day 4 හි මගේ ප්‍රධාන ඉලක්කය වුණේ සිනමා ශාලා ගිණුම සඳහා ආරක්ෂිත **Register, Login, Password Hashing සහ Stateless JWT Authentication** පද්ධතියක් ගොඩනැගීමයි.
> 
> මුලින්ම **Password ආරක්ෂාව** සඳහා මම `PasswordConfig` class එක හරහා `BCryptPasswordEncoder` Bean එකක් හැදුවා. `AuthService.register()` ක්‍රියාත්මක වන විට පරිශීලකයා එවන password එක plain text ලෙස save නොකර `passwordEncoder.encode()` මඟින් අහඹු salt එකක් සහිත BCrypt hash එකක් බවට හරවා PostgreSQL හි ගබඩා කරනවා. Register කිරීමේදී duplicate email පරීක්ෂාවට අමතරව, අපේ system එක තනි සිනමා ශාලාවකට සීමා කරන **One-cinema rule** එක `cinemaRepository.count() > 0` මඟින් `AuthService` තුළ enforce කර තිබෙනවා. Register සාර්ථක වූ පසු password නොමැතිව `CinemaResponse` DTO එක පමණක් ආපසු යවනවා.
> 
> දෙවනුව **Login සහ JWT** ක්‍රියාවලිය ගැන බැලුවොත්, `AuthService.login()` හිදී email එක normalize කර `findByEmail()` මඟින් සිනමා ගිණුම සොයා, `passwordEncoder.matches()` මඟින් password එක සසඳනවා. වැරදි email හෝ password සඳහා enumeration attacks වැළැක්වීමට එකම `401 Invalid email or password` පණිවිඩය ලබා දෙනවා. Login සාර්ථක නම් `JwtService.generateToken(cinema.getId())` මඟින් token එක සාදා `LoginResponse` හරහා `"Bearer"` token එකක් ලෙස නිකුත් කරනවා.
> 
> `JwtService` එකේදී අපි token payload එක තුළ `sub` ලෙස `cinemaId`, `iat` සහ `exp` පමණක් දමා, `.env` ගොනුවෙන් එන `${JWT_SECRET}` (අවම 32 bytes) භාවිතයෙන් HMAC-SHA256 signature එක ගසනවා. Token කල් ඉකුත් වීමේ කාලය `application.properties` හි `3600000ms` (පැය 1) ලෙස සකසා තිබෙනවා.
> 
> තෙවනුව **Spring Security Configuration** සඳහා මම `SecurityConfig` සහ `JwtAuthenticationFilter` නිර්මාණය කළා:
> * මෙය Stateless Bearer-token API එකක් නිසා `SessionCreationPolicy.STATELESS` යොදා `CSRF`, `formLogin` සහ `httpBasic` disable කර තිබෙනවා.
> * `POST /api/auth/register`, `POST /api/auth/login` සහ `GET /api/movies/*/poster` පොදු (`permitAll()`) කර ඇති අතර අනෙක් සියලුම endpoints `.authenticated()` මඟින් ආරක්ෂා කර තිබෙනවා.
> * අපේ custom `JwtAuthenticationFilter` එක `UsernamePasswordAuthenticationFilter` එකට පෙර chain එකට එක් කර තිබෙනවා. එය `Authorization: Bearer <token>` header එකෙන් token එක කියවා, `JwtService` මඟින් verify කර, `cinemaId` principal එක ලෙස `SecurityContextHolder` හි තැන්පත් කරනවා. Token එක expired හෝ tampered නම් `ApiErrorResponseWriter` මඟින් `401 Unauthorized` JSON ප්‍රතිචාරයක් කෙලින්ම යවනවා.
> 
> පසු දිනවලදී `CinemaProfileController` මඟින් `@AuthenticationPrincipal Long cinemaId` භාවිතයෙන් token එකෙන් කෙලින්ම logged-in cinema ගිණුම හඳුනා ගන්නා අතර, `MovieController` සහ `ShowtimeController` වල endpoints මෙම JWT ආරක්ෂණ පවුර මඟින් සුරක්ෂිත වී තිබෙනවා. දැන් මම Postman මඟින් Register, Login, සහ Protected endpoint පරීක්ෂාව සජීවීව පෙන්වන්නම්. ස්තූතියි!"

---

### 11.2 Live Demo පිරික්සුම් ලැයිස්තුව (5-Step Live Demo Checklist)

| පියවර | පරීක්ෂා කළ යුතු දේ (Action) | ක්‍රමය / විධානය (Command / Postman) | අපේක්ෂිත ප්‍රතිඵලය (Expected Result) |
|---|---|---|---|
| **1** | PostgreSQL සහ `.env` සූදානම | `docker compose up -d postgres` සහ `.env` හි `JWT_SECRET` ඇති බව තහවුරු කිරීම | DB container එක healthy වී පැවතීම; `cinema` table එක පැවතීම. |
| **2** | Backend server ධාවනය | `cd backend && ./gradlew bootRun` | `JwtService` 32-byte key check සමත් වී `port 8082` හි server start වීම. |
| **3** | Register සහ One-Cinema Rule පෙන්වීම | `POST /api/auth/register` (පළමු වරට, ඉන්පසු දෙවන වරට) | පළමු වර: `201 Created` (password නැති JSON), DB හි `$2a$10$...` hash save වීම. දෙවන වර: `409 Conflict`. |
| **4** | Login සහ JWT Decode පෙන්වීම | `POST /api/auth/login` -> ලැබෙන `accessToken` එක `jwt.io` හෝ Base64 decoder මඟින් පෙන්වීම | `200 OK` + `{accessToken, tokenType: "Bearer"}`. Payload හි `sub`, `iat`, `exp` පමණක් පෙන්වීම. |
| **5** | Protected Endpoint (With / Without / Invalid Token) | `GET /api/cinema/profile`: (a) token නැතිව, (b) invalid token එකකින්, (c) valid Bearer token එකකින් | (a) `401 "Authentication required"`, (b) `401 "Invalid or expired JWT"`, (c) `200 OK` + cinema profile JSON. |

### මේක මට පැහැදිලිව කියන්න පුළුවන්ද?
1. **Live demo එකේදී Register වූ පසු database එකේ password column එක පෙන්වුවහොත් mentor ට පැහැදිලි කළ යුතු ප්‍රධාන කරුණ කුමක්ද?**
   - *පිළිතුර:* Database එකේ store වී ඇත්තේ plain text password එක නොව `$2a$10$...` ආකෘතියේ BCrypt hash එක බවත්, API response (`CinemaResponse`) එකේදී password field එක සම්පූර්ණයෙන්ම ඉවත් කර ඇති බවත් පෙන්වීම.
2. **Demo එකේදී `jwt.io` හි token එක paste කළ විට signature verify කිරීමට අවශ්‍ය වන්නේ කුමක්ද?**
   - *පිළිතුර:* `.env` ගොනුවේ ඇති `JWT_SECRET` Base64 string එක (Base64 encoded box එක tick කර) ඇතුළත් කළහොත් `Signature Verified` ලෙස පෙන්වයි.
3. **Register දෙවන වරට demo කරන විට 409 Conflict ලැබෙන්නේ ඇයි?**
   - *පිළිතුර:* පද්ධතියේ One-cinema rule (`cinemaRepository.count() > 0`) හෝ duplicate email check (`existsByEmail`) ක්‍රියාත්මක වන නිසාය.

---

## 12. Viva ප්‍රශ්න සහ කෙටි සිංහල පිළිතුරු 20 (20 Viva Questions with Short Sinhala Answers)

1. **ප්‍රශ්නය: `SecurityConfig` හි `SessionCreationPolicy.STATELESS` දමා ඇත්තේ ඇයි?**
   - **පිළිතුර:** අපේ REST API එක server memory හි `HttpSession` ගබඩා නොකර, සෑම request එකකදීම JWT token එක මඟින් පමණක් පරිශීලකයා හඳුනා ගන්නා නිසාය.
2. **ප්‍රශ්නය: Stateless API එකක CSRF protection disable කරන්නේ ඇයි?**
   - **පිළිතුර:** CSRF ප්‍රහාර සිදුවන්නේ browser cookies ස්වයංක්‍රීයව යවන විටය. අපි cookies වෙනුවට `Authorization: Bearer` header භාවිතා කරන නිසා CSRF අවදානමක් නැති අතර, CSRF active තිබුණහොත් POST/PUT requests block වේ.
3. **ප්‍රශ්නය: `JwtAuthenticationFilter` extend කරන්නේ `OncePerRequestFilter` වලින් ඇයි?**
   - **පිළිතුර:** සෑම HTTP request එකක් සඳහාම filter එකේ JWT validation කේතය එක් වරක් පමණක් ක්‍රියාත්මක වන බව සහතික කිරීමටයි.
4. **ප්‍රශ්නය: `JwtAuthenticationFilter` මත `@Component` annotation එක නොදමා `SecurityConfig` හි `new` කරන්නේ ඇයි?**
   - **පිළිතුර:** `@Component` දැමුවහොත් Spring Boot එය Servlet filter ලෙසත් Security filter ලෙසත් දෙවරක් register කරයි. `new` කිරීමෙන් Security filter chain එක තුළ පමණක් එක් වරක් run වේ.
5. **ප්‍රශ්නය: `JwtAuthenticationFilter` එක `UsernamePasswordAuthenticationFilter` එකට පෙර (Before) දමන්නේ ඇයි?**
   - **පිළිතුර:** `AuthorizationFilter` එක මඟින් request එකට අවසර තිබේදැයි පරීක්ෂා කිරීමට පෙරම, Bearer JWT එක decode කර `SecurityContextHolder` එකේ authentication object එක සකසා තැබිය යුතු බැවිනි.
6. **ප්‍රශ්නය: `authorizationHeader.substring(7)` ලෙස 7 කපා හරින්නේ ඇයි?**
   - **පිළිතුර:** `"Bearer "` යන prefix එකෙහි අක්ෂර 6 ක් සහ හිස්තැනක් (මුළු අක්ෂර 7 ක්) ඇති බැවින් සැබෑ JWT string එක පමණක් වෙන් කර ගැනීමටයි.
7. **ප්‍රශ්නය: JWT එකක් කොටස් කීයකින් සමන්විතද? ඒවා මොනවාද?**
   - **පිළිතුර:** තිත් (`.`) මඟින් වෙන් වූ කොටස් 3 කි: `Header` (algorithm), `Payload` (claims), සහ `Signature` (HMAC අත්සන).
8. **ප්‍රශ්නය: අපේ `JwtService` එක token payload එක තුළ ගබඩා කරන claims මොනවාද?**
   - **පිළිතුර:** `sub` (`cinemaId`), `iat` (issued at timestamp), සහ `exp` (expiration timestamp) පමණි.
9. **ප්‍රශ්නය: JWT payload එක encrypted ද? නැතිනම් කෙනෙකුට එය කියවිය හැකිද?**
   - **පිළිතුර:** Encrypt කර නැත; Base64URL encode කර පමණි. එනිසා ඕනෑම කෙනෙකුට decode කර කියවිය හැක, නමුත් signature නිසා රහස් යතුර නැතිව වෙනස් කළ නොහැක.
10. **ප්‍රශ්නය: `JwtService` constructor එකේ `keyBytes.length < 32` පරීක්ෂා කරන්නේ ඇයි?**
    - **පිළිතුර:** HMAC-SHA256 (HS256) ඇල්ගොරිතමය සඳහා අවම වශයෙන් bits 256 (bytes 32) ක secret key එකක් අවශ්‍ය වන බැවිනි.
11. **ප්‍රශ්නය: `app.jwt.secret` සහ `app.jwt.expiration-ms` වලින් environment variable එකකින් එන්නේ කුමක්ද?**
    - **පිළිතුර:** `app.jwt.secret` `${JWT_SECRET}` (environment variable / `.env`) වෙතින් එන අතර, `app.jwt.expiration-ms` (`3600000`) `application.properties` හි hardcode කර ඇත.
12. **ප්‍රශ්නය: BCrypt string එකක ඇති කොටස් 4 මොනවාද?**
    - **පිළිතුර:** Version (`$2a$`), Cost factor (`10$`), 22-character Salt, සහ 31-character Hash අගයයි.
13. **ප්‍රශ්නය: එකම password එක දෙවරක් BCrypt encode කළ විට වෙනස් hashes ලැබෙන්නේ ඇයි?**
    - **පිළිතුර:** සෑම `encode()` ඇමතුමකදීම අලුත් අහඹු 128-bit **Salt** අගයක් ජනනය වන බැවිනි.
14. **ප්‍රශ්නය: Login වීමේදී `passwordEncoder.matches()` සැසඳීම සිදු කරන්නේ කෙසේද?**
    - **පිළිතුර:** Stored hash එකේ මුල ඇති salt එක කියවාගෙන, user දුන් raw password එක එම salt එකෙන්ම නැවත hash කර, hash දෙක සමානදැයි පරීක්ෂා කරයි.
15. **ප්‍රශ්නය: `AuthService.register()` හි One-cinema rule එක ක්‍රියාත්මක වන්නේ කෙසේද?**
    - **පිළිතුර:** `if (cinemaRepository.count() > 0)` මඟින් database එකේ දැනටමත් ගිණුමක් ඇත්දැයි බලා, ඇත්නම් `409 Conflict` (`"A cinema account has already been registered"`) විසි කරයි.
16. **ප්‍රශ්නය: Login අසාර්ථක වූ විට email වැරදි වුවත් password වැරදි වුවත් එකම message එක දෙන්නේ ඇයි?**
    - **පිළිතුර:** Account Enumeration ප්‍රහාර (වලංගු email ලිපින මොනවාදැයි අනුමාන කිරීම) වැළැක්වීම සඳහා දෙකේදීම `"Invalid email or password"` ලබා දෙයි.
17. **ප්‍රශ්නය: `401 Unauthorized` සහ `403 Forbidden` අතර වෙනස කුමක්ද?**
    - **පිළිතුර:** 401 යනු authentication නැති හෝ token වැරදි/expired බවයි. 403 යනු authenticate වී ඇතත් අදාළ ක්‍රියාවට permission නැති බවයි.
18. **ප්‍රශ්නය: `GlobalExceptionHandler` වෙනුවට `ApiErrorResponseWriter` ලියා ඇත්තේ ඇයි?**
    - **පිළිතුර:** `JwtAuthenticationFilter` සහ `SecurityConfig` handlers run වන්නේ Controller එකට පෙර Servlet Filter ස්ථරයේදී බැවින්, `@RestControllerAdvice` වලට ඒවා හසු නොවන නිසා response stream එකට කෙලින්ම JSON ලිවීමටයි.
19. **ප්‍රශ්නය: `CinemaProfileController` හි `@AuthenticationPrincipal Long cinemaId` අගය ලැබෙන්නේ කොහෙන්ද?**
    - **පිළිතුර:** `JwtAuthenticationFilter` විසින් `SecurityContextHolder` හි තැන්පත් කළ `UsernamePasswordAuthenticationToken` හි පළමු argument (Principal) වන `cinemaId` වෙතිනි.
20. **ප්‍රශ්නය: `MovieController` සහ `ShowtimeController` වල `@AuthenticationPrincipal` භාවිතා නොකරන්නේ ඇයි?**
    - **පිළිතුර:** මෙය තනි සිනමා ශාලා පද්ධතියක් (Single-cinema model) වන නිසා `movie` සහ `showtime` වගු වල `cinema_id` නොමැත. එනිසා ඒවා JWT භාවිතා කරන්නේ `.anyRequest().authenticated()` මඟින් අනවසර ප්‍රවේශ වැළැක්වීමට පමණි.

### මේක මට පැහැදිලිව කියන්න පුළුවන්ද?
1. **Viva එකේදී "JWT token එකක් logout කළ පසු invalidate කරන්නේ කෙසේද?" කියා ඇසුවහොත් දිය යුතු පිළිතුර කුමක්ද?**
   - *පිළිතුර:* අපේ වත්මන් stateless කේතයේ token blacklist/revocation table එකක් නොමැත; client පැත්තෙන් token එක මකා දමන අතර, server පැත්තෙන් token එක වලංගු කාලය (`3600000ms`) අවසන් වූ පසු ස්වයංක්‍රීයව expire වේ. (අවශ්‍ය නම් token blacklist/refresh token mechanism එකක් පසුව එක් කළ හැකි බව පැවසීම).
2. **`SecurityContextHolder.createEmptyContext()` භාවිතා කරන්නේ ඇයි?**
   - *පිළිතුර:* Multithreaded පරිසරයක `ThreadLocal` race conditions වළක්වා ගැනීමට Spring Security නිර්දේශ කරන ආරක්ෂිතම ක්‍රමය එය බැවිනි.
3. **`RegisterRequest` හි `email` validation සඳහා `@NotBlank` සහ `@Email` දෙකම දමා ඇත්තේ ඇයි?**
   - *පිළිතුර:* සමහර bean validation implementations වල `@Email` මඟින් `null` අගය valid ලෙස සලකන බැවින්, email එක අනිවාර්යයෙන්ම හිස් නොවිය යුතු බව තහවුරු කිරීමට `@NotBlank` ද එක් කර ඇත.

---

## 13. තාක්ෂණික වචන මාලාව (Glossary Table)

| ඉංග්‍රීසි පදය (English Term) | සිංහල තේරුම (Sinhala Meaning) | අපගේ කේතයෙන් සැබෑ උදාහරණයක් (Example from Code) |
|---|---|---|
| **Authentication** | පරිශීලකයාගේ අනන්‍යතාවය තහවුරු කිරීම | `AuthService.login()` මඟින් email/password පරීක්ෂා කිරීම |
| **Authorization** | තහවුරු කළ පරිශීලකයාට ප්‍රවේශ අවසරය තීරණය කිරීම | `SecurityConfig` හි `.anyRequest().authenticated()` |
| **Stateless** | Server memory හි session දත්ත රඳවා නොගැනීම | `SessionCreationPolicy.STATELESS` |
| **CSRF (Cross-Site Request Forgery)** | Browser cookies අවභාවිතා කර සිදු කරන ප්‍රහාර වර්ගයක් | `.csrf(csrf -> csrf.disable())` |
| **JWT (JSON Web Token)** | අත්සන් කළ, සංයුක්ත JSON ආකාරයේ අනන්‍යතා token එක | `JwtService.generateToken(cinemaId)` |
| **Claims** | JWT payload එක තුළ අඩංගු දත්ත ක්ෂේත්‍ර (key-value pairs) | `sub` (`cinemaId`), `iat`, `exp` (`Claims`) |
| **HMAC-SHA256 (HS256)** | රහස් යතුරක් භාවිතයෙන් token අත්සන් කරන hash algorithm එක | `Keys.hmacShaKeyFor(keyBytes)` |
| **Base64 / Base64URL** | Binary දත්ත ASCII text ලෙස encode කරන ක්‍රමය | `Decoders.BASE64.decode(base64Secret)` |
| **BCrypt** | Salt සහිතව මුරපද ආරක්ෂිතව hash කරන adaptive algorithm එක | `new BCryptPasswordEncoder()` (`PasswordConfig`) |
| **Salt** | Rainbow table ප්‍රහාර වැළැක්වීමට password එකට එක් කරන අහඹු දත්ත | BCrypt string හි මුල් අක්ෂර 22 |
| **Cost Factor** | BCrypt hashing නැවත නැවත ධාවනය වන වාර ගණන ($2^{10}$) | `$2a$10$...` හි `10` අගය |
| **SecurityFilterChain** | HTTP requests පෙරා බලන Spring Security filter දාමය | `SecurityConfig.securityFilterChain(...)` |
| **OncePerRequestFilter** | Request එකකට හරියටම එක් වරක් පමණක් run වන Spring filter class එක | `JwtAuthenticationFilter extends OncePerRequestFilter` |
| **SecurityContextHolder** | වත්මන් request එකේ authenticated user විස්තර තබා ගන්නා ගබඩාව | `SecurityContextHolder.setContext(context)` |
| **Principal** | දැනට login වී සිටින පරිශීලකයා හඳුනා ගන්නා ප්‍රධාන වස්තුව/ID එක | `cinemaId` (`@AuthenticationPrincipal Long cinemaId`) |
| **AuthenticationEntryPoint** | Authenticate නොවී protected URL එකකට එන විට ක්‍රියාත්මක වන handler එක | `SecurityConfig` හි 401 handler එක |
| **AccessDeniedHandler** | අවසර නැති resource එකකට එන විට 403 යවන handler එක | `SecurityConfig` හි 403 handler එක |
| **Bearer Token** | "මෙම token එක දරන්නාට අවසර දෙන්න" යන HTTP auth scheme එක | `Authorization: Bearer <token>` |
| **Email Normalization** | Email එකක spaces ඉවත් කර lowercase කිරීම | `request.getEmail().trim().toLowerCase(Locale.ROOT)` |
| **Account Enumeration** | Error messages මඟින් පද්ධතියේ email ඇත්දැයි සොයා ගැනීමේ ප්‍රහාරය | Login error දෙකේදීම `"Invalid email or password"` දීම |

### මේක මට පැහැදිලිව කියන්න පුළුවන්ද?
1. **Principal සහ Authority අතර වෙනස කුමක්ද?**
   - *පිළිතුර:* Principal යනු පරිශීලකයා කවුද යන්නයි (අපේ කේතයේ `cinemaId` = `1L`). Authority යනු ඔහුට ඇති role/permission එකයි (අපේ කේතයේ `"ROLE_CINEMA_MANAGER"`).
2. **`OncePerRequestFilter` එකක් භාවිතා නොකර සාමාන්‍ය `Filter` එකක් භාවිතා කළහොත් කුමක් විය හැකිද?**
   - *පිළිතුර:* Request එක අභ්‍යන්තරව forward වන අවස්ථාවලදී සාමාන්‍ය filter එකක් එක් request එකකට කිහිප වතාවක් run විය හැක. `OncePerRequestFilter` එය වළක්වයි.
3. **`Bearer` යන වචනයේ තේරුම කුමක්ද?**
   - *පිළිතුර:* "Bearer" යනු "දරන්නා" යන්නයි — එනම් මෙම token එක රැගෙන එන ඕනෑම client කෙනෙකුට token එකේ හිමිකරු ලෙස ප්‍රවේශය ලබා දෙන බවයි.

---

## 14. සමාලෝචන සටහන් - කේතයේ ඇති අඩුපාඩු සහ අවදානම් (Review Notes - Not Changed)

> [!IMPORTANT]
> **නිරීක්ෂණ ප්‍රතිපත්තිය (Observation Policy):**
> පහත දැක්වෙන්නේ Day 4 කේත පදනම සහ වින්‍යාසයන් පරීක්ෂා කිරීමේදී නිරීක්ෂණය වූ අඩුපාඩු, නොගැලපීම් සහ අවදානම් වේ. **නීති රීති වලට අනුකූලව මෙම කිසිදු ගොනුවක් වෙනස් කර නොමැත.**

1. **Auth සහ Security සඳහා Tests කිසිවක් නොමැති වීම (Missing Security/Auth Tests):**
   - `backend/src/test` තුළ ඇත්තේ `BackendApplicationTests.java` සහ `MovieControllerTest.java` පමණි.
   - `AuthController`, `AuthService`, `JwtService`, `JwtAuthenticationFilter`, `SecurityConfig`, සහ `CinemaProfileController` සඳහා කිසිදු unit හෝ integration test එකක් ලියා නැත.
2. **`SecurityConfig.java` හි නොපවතින `/api/hello` Endpoint Rule එක:**
   - `SecurityConfig.java` (පේළි 62) හි `.requestMatchers("/api/hello").permitAll()` ඇතත්, කේත පදනමේ `/api/hello` නමින් කිසිදු controller endpoint එකක් නොමැත.
3. **`SecurityConfig.java` හි භාවිතා නොකරන Imports:**
   - පේළි 4: `import jakarta.servlet.http.HttpServletResponse;` සහ පේළි 15: `import org.springframework.security.web.access.AccessDeniedHandler;` යන imports දෙකම class එක තුළ භාවිතා නොකරයි.
4. **`.env.example` ගොනුවේ `JWT_SECRET` නොමැති වීම:**
   - `application.properties` (පේළි 25) හි `app.jwt.secret=${JWT_SECRET}` අනිවාර්ය වුවත්, `.env.example` ගොනුවේ ඇත්තේ `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` පමණි. අලුත් developer කෙනෙකු `.env.example` copy කර `.env` හැදුවහොත් `JWT_SECRET` නොමැති නිසා startup අසාර්ථක වේ.
5. **`application.properties` හි Relative `.env` Path එක (`../.env`):**
   - `spring.config.import=optional:file:../.env[.properties]` නිවැරදිව වැඩ කරන්නේ command working directory එක `backend/` වන විට පමණි. Repository root එකේ සිට JAR එක run කළහොත් `.env` load නොවේ.
6. **`AuthService.register()` හි `address` Normalize නොකිරීම:**
   - `CinemaProfileService.updateProfile()` හි `address` trim කර හිස් නම් `null` බවට හරවයි. නමුත් `AuthService.register()` (පේළි 57) හි `cinema.setAddress(request.getAddress());` මඟින් trim කිරීමක් හෝ blank-to-null conversion එකක් සිදු නොකරයි.
7. **One-Cinema Rule (`count() > 0`) හි Concurrency / Race Condition අවදානම:**
   - `AuthService.register()` හි `@Transactional` නොමැති අතර `cinemaRepository.count() > 0` check එක database constraint එකකින් ආරක්ෂා වී නැත. එකම මොහොතේ වෙනස් emails දෙකකින් simultaneous register requests දෙකක් පැමිණියහොත් දෙකටම `count() == 0` පෙනී ගිණුම් 2 ක් save වීමට ඉඩ ඇත.
8. **BCrypt 72-Byte සීමාව සහ `RegisterRequest` 100-Character සීමාව:**
   - `RegisterRequest` හි `@Size(min = 8, max = 100)` ලෙස අක්ෂර 100 ක් දක්වා ඉඩ දී ඇතත්, සම්මත BCrypt ඇල්ගොරිතමය මුල් bytes 72 න් පසු අක්ෂර නොසලකා හරියි (හෝ `BCryptPasswordEncoder` implementation අනුව UTF-8 72 bytes සීමාවට යටත් වේ).
9. **CORS Configuration නොමැති වීම:**
   - `SecurityConfig.java` හි `.cors(...)` වින්‍යාස කර නැත. Frontend app එක වෙනත් port/domain එකක (උදා: `localhost:5173`) සිට browser හරහා requests එවන විට preflight `OPTIONS` requests `.anyRequest().authenticated()` මඟින් block වීමට ඉඩ ඇත.
10. **වත්මන් `SecurityConfig` හි `accessDeniedHandler` (403) ක්‍රියාත්මක නොවීම:**
    - `JwtAuthenticationFilter` සැමවිටම `ROLE_CINEMA_MANAGER` ලබා දෙන අතර `SecurityConfig` හි role-based rules (`.hasRole(...)`) නොමැති නිසා වත්මන් endpoints කිසිවකින් `403 Forbidden` trigger නොවේ.
11. **Validation Error සහ අනෙකුත් Error JSON ආකෘති අසමාන වීම:**
    - `GlobalExceptionHandler` හි `MethodArgumentNotValidException` (400) මඟින් `Map<String, String>` එකක් return කරන අතර, `ResponseStatusException` / Filter 401 errors මඟින් `ApiErrorResponse` object එකක් return කරයි. Frontend error parsing වලදී ආකෘති දෙකම හැසිරවීමට සිදු වේ.

### මේක මට පැහැදිලිව කියන්න පුළුවන්ද?
1. **`SecurityConfig` හි CORS configure නොකිරීමෙන් frontend සම්බන්ධ කරන විට ඇතිවන ගැටලුව කුමක්ද?**
   - *පිළිතුර:* Browser එක මඟින් `POST /api/auth/login` වැනි request එකකට පෙර යවන `OPTIONS` preflight request එක `Authorization` header එකක් නොමැතිව එන බැවින් `.anyRequest().authenticated()` මඟින් `401` ලෙස block වේ.
2. **`AuthService.register()` හි One-cinema rule එක race condition එකකින් බිඳෙන්නේ කෙසේද?**
   - *පිළිතුර:* Requests දෙකක් එකවර ආ විට, පළමු transaction එක commit වීමට පෙර දෙවැන්නද `cinemaRepository.count()` පරීක්ෂා කර `0` ලෙස දුටුවහොත් වෙනස් emails වලින් accounts 2 ක් insert විය හැක.
3. **`.env.example` ගොනුවේ `JWT_SECRET` තිබීම වැදගත් වන්නේ ඇයි?**
   - *පිළිතුර:* කණ්ඩායමේ අනෙක් සාමාජිකයන් project එක clone කර `.env.example` අනුව `.env` සකසන විට `JWT_SECRET` අවශ්‍ය බවත්, අවම වශයෙන් 32-byte Base64 string එකක් විය යුතු බවත් දැනගැනීමටයි.
