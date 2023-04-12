-- заглушка; с пустым data.sql программа не запускается :(
MERGE INTO genres (genre) KEY (genre)
VALUES ('Comedy'),
       ('Action'),
       ('Horror');

MERGE INTO mpa (mpa) KEY (mpa)
VALUES ('PG-13'),
       ('R'),
       ('G'),
       ('PG'),
       ('NC-17');