--liquibase formatted sql

--changeset zahra:002-seed-movies
INSERT INTO movie (title, duration_minutes)
VALUES ('Inception', 148),
       ('The Matrix', 136),
       ('Spirited Away', 125);

--changeset zahra:002-seed-screenings
-- Fixed dates far in the future, so the demo never runs out of bookable screenings.
-- Times are cinema times in Berlin; one auditorium, so screenings do not overlap.
INSERT INTO screening (movie_id, starts_at)
SELECT m.id, v.start_local AT TIME ZONE 'Europe/Berlin'
FROM (VALUES ('Inception', TIMESTAMP '2036-03-14 14:00'),
             ('The Matrix', TIMESTAMP '2036-03-14 17:00'),
             ('Spirited Away', TIMESTAMP '2036-03-14 20:00'),
             ('Spirited Away', TIMESTAMP '2036-03-15 14:00'),
             ('The Matrix', TIMESTAMP '2036-03-15 17:00'),
             ('Inception', TIMESTAMP '2036-03-15 20:00')) AS v(title, start_local)
         JOIN movie m ON m.title = v.title;