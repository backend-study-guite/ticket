-- 1. Users
INSERT INTO users (name, points) VALUES
                                     ('Alice', 1000),
                                     ('Bob', 2000),
                                     ('Charlie', 1500),
                                     ('David', 500),
                                     ('Emma', 3000),
                                     ('Frank', 1200),
                                     ('Grace', 800),
                                     ('Henry', 2500),
                                     ('Irene', 1800),
                                     ('Jack', 400);

-- 2. Concert
INSERT INTO concert (concert_title) VALUES
                                        ('Coldplay Live'),
                                        ('IU Concert'),
                                        ('BTS World Tour'),
                                        ('Jazz Night'),
                                        ('Rock Festival'),
                                        ('Classic Evening'),
                                        ('EDM Party'),
                                        ('Indie Live'),
                                        ('HipHop Show'),
                                        ('Ballad Night');

-- 3. ConcertOption
INSERT INTO concert_option (start_time, concert_id) VALUES
                                                        ('2025-01-01 18:00:00', 1),
                                                        ('2025-01-02 19:00:00', 2),
                                                        ('2025-01-03 20:00:00', 3),
                                                        ('2025-01-04 18:00:00', 4),
                                                        ('2025-01-05 19:00:00', 5),
                                                        ('2025-01-06 20:00:00', 6),
                                                        ('2025-01-07 18:00:00', 7),
                                                        ('2025-01-08 19:00:00', 8),
                                                        ('2025-01-09 20:00:00', 9),
                                                        ('2025-01-10 18:00:00', 10);

-- 4. Seat
INSERT INTO seat (seat_number, concert_option_id, price, seat_status) VALUES
                                                                   ('A1', 1, 18000, 'AVAILABLE'),
                                                                   ('A2', 1, 20000, 'AVAILABLE'),
                                                                   ('B1', 2, 18000, 'AVAILABLE'),
                                                                   ('B2', 2, 20000, 'RESERVED'),
                                                                   ('C1', 3, 32000, 'PAID'),
                                                                   ('C2', 3, 34000, 'AVAILABLE'),
                                                                   ('D1', 4, 20000, 'AVAILABLE'),
                                                                   ('D2', 4, 20000, 'AVAILABLE'),
                                                                   ('E1', 5, 16000, 'RESERVED'),
                                                                   ('E2', 5, 16000, 'AVAILABLE');

-- 5. Reservation
INSERT INTO reservation (user_id, seat_id, reservation_status) VALUES
                                                                   (1, 2, 'NOT_PAID'),
                                                                   (2, 4, 'PAID'),
                                                                   (3, 5, 'PAID'),
                                                                   (4, 1, 'NOT_PAID'),
                                                                   (5, 3, 'PAID'),
                                                                   (6, 6, 'NOT_PAID'),
                                                                   (7, 7, 'NOT_PAID'),
                                                                   (8, 8, 'PAID'),
                                                                   (9, 9, 'PAID'),
                                                                   (10, 10, 'NOT_PAID');