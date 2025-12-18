CREATE DATABASE IF NOT EXISTS ticket;
USE ticket;

-- 1. Users
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    points BIGINT DEFAULT 0
);

-- 2. Concert
CREATE TABLE concert (
    concert_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    concert_title VARCHAR(100) NOT NULL
);

-- 3. ConcertOption
CREATE TABLE concert_option (
    concert_option_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    start_time DATETIME NOT NULL,
    concert_id BIGINT NOT NULL
);

-- 4. Seat
CREATE TABLE seat (
    seat_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seat_number VARCHAR(20) NOT NULL,
    concert_option_id BIGINT NOT NULL,
    price BIGINT DEFAULT 0,
    seat_status VARCHAR(20) NOT NULL
);

-- 5. Reservation
CREATE TABLE reservation (
    reservation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    reservation_status VARCHAR(20) NOT NULL
);