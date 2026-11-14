--liquibase formatted sql

--changeset zahra:002-seed-movies
INSERT INTO movie (title, duration_minutes) VALUES
                                                ('Inception', 148),
                                                ('The Matrix', 136),
                                                ('Spirited Away', 125);

--changeset zahra:002-seed-screenings
INSERT INTO screening (movie_id, starts_at)
SELECT id, date_trunc('day', now()) + interval '1 day 18 hours'
FROM movie WHERE title = 'Inception';

INSERT INTO screening (movie_id, starts_at)
SELECT id, date_trunc('day', now()) + interval '1 day 21 hours'
FROM movie WHERE title = 'Inception';

INSERT INTO screening (movie_id, starts_at)
SELECT id, date_trunc('day', now()) + interval '1 day 19 hours'
FROM movie WHERE title = 'The Matrix';

INSERT INTO screening (movie_id, starts_at)
SELECT id, date_trunc('day', now()) + interval '2 days 17 hours'
FROM movie WHERE title = 'The Matrix';

INSERT INTO screening (movie_id, starts_at)
SELECT id, date_trunc('day', now()) + interval '2 days 15 hours'
FROM movie WHERE title = 'Spirited Away';