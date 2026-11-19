-- Table 1: Cinema account

CREATE TABLE IF NOT EXISTS cinema (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(100) NOT NULL,

    address VARCHAR(200),

    email VARCHAR(100) NOT NULL UNIQUE,

    password VARCHAR(100) NOT NULL
);



-- Table 2: Movie information

CREATE TABLE IF NOT EXISTS movie (
    id BIGSERIAL PRIMARY KEY,

    title VARCHAR(150) NOT NULL,

    description VARCHAR(500),

    duration_minutes INT NOT NULL CHECK (duration_minutes > 0)
);



-- =========================================
-- TABLE 3: MOVIE_IMAGE
-- =========================================

CREATE TABLE IF NOT EXISTS movie_image (
    movie_id BIGINT PRIMARY KEY REFERENCES movie(id),

    content_type VARCHAR(50) NOT NULL,

    data BYTEA NOT NULL,

    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);






-- =========================================
-- TABLE 4: SHOWTIME
-- =========================================

CREATE TABLE IF NOT EXISTS showtime (
    id BIGSERIAL PRIMARY KEY,

    movie_id BIGINT NOT NULL REFERENCES movie(id),

    show_date DATE NOT NULL,

    show_time TIME NOT NULL,

    price NUMERIC(10,2) NOT NULL,

    total_seats INT NOT NULL CHECK (total_seats > 0),

    seats_per_row INT NOT NULL CHECK (seats_per_row > 0),

    active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT uq_showtime_date_time UNIQUE (show_date, show_time)
);





CREATE TABLE IF NOT EXISTS reservation (
    id VARCHAR(36) PRIMARY KEY,
    showtime_id BIGINT NOT NULL REFERENCES showtime(id),
    customer_id BIGINT NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL
        CHECK (status IN ('CONFIRMED', 'REJECTED', 'PAID')),
    reason VARCHAR(200),
    total_amount NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);


CREATE TABLE IF NOT EXISTS reservation_seat (
    reservation_id VARCHAR(36) NOT NULL REFERENCES reservation(id),
    showtime_id BIGINT NOT NULL REFERENCES showtime(id),
    seat_number INT NOT NULL CHECK (seat_number > 0),

    PRIMARY KEY (reservation_id, seat_number),

    CONSTRAINT uq_showtime_seat
        UNIQUE (showtime_id, seat_number)
);





