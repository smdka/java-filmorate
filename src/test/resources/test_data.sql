INSERT INTO MPA (NAME) VALUES ('G');
INSERT INTO MPA (NAME) VALUES ('PG');
INSERT INTO MPA (NAME) VALUES ('PG-13');
INSERT INTO MPA (NAME) VALUES ('R');
INSERT INTO MPA (NAME) VALUES ('NC-17');

INSERT INTO GENRES (NAME) VALUES ('Комедия');
INSERT INTO GENRES (NAME) VALUES ('Драма');
INSERT INTO GENRES (NAME) VALUES ('Мультфильм');
INSERT INTO GENRES (NAME) VALUES ('Триллер');
INSERT INTO GENRES (NAME) VALUES ('Документальный');
INSERT INTO GENRES (NAME) VALUES ('Боевик');

INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY)
VALUES ('user1@yandex.ru', 'Login1','Name1','19890501'),
       ('user2@yandex.ru', 'Login2','Name2','19890502'),
       ('user3@yandex.ru', 'Login3','Name3','19890503'),
       ('user4@yandex.ru', 'Login4','Name4','19890504'),
       ('user5@yandex.ru', 'Login5','Name5','19890505');

INSERT INTO FILMS (NAME, DESCRIPTION, RELEASE_DATE, DURATION, MPA_ID)
VALUES ('Terminator', 'Film about killer-machine','19830823',120,2),
       ('Snatch', 'Film about gypsies','19831123',120,3),
       ('Jaws', 'Film about killer shark','19850823',120,5);

INSERT INTO FILM_GENRE (FILM_ID, GENRE_ID)
VALUES (1,6),
       (2,1),
       (2,6),
       (3,4);

INSERT INTO FILM_LIKES (FILM_ID, LIKED_BY_USER_ID)
    VALUES ( SELECT ID FROM FILMS WHERE name = 'Snatch', SELECT ID FROM USERS WHERE name = 'Name2');
INSERT INTO FILM_LIKES (FILM_ID, LIKED_BY_USER_ID)
    VALUES ( SELECT ID FROM FILMS WHERE name = 'Snatch', SELECT ID FROM USERS WHERE name = 'Name1' );
INSERT INTO FILM_LIKES (FILM_ID, LIKED_BY_USER_ID)
    VALUES ( SELECT ID FROM FILMS WHERE name = 'Snatch', SELECT ID FROM USERS WHERE name = 'Name3' );
INSERT INTO FILM_LIKES (FILM_ID, LIKED_BY_USER_ID)
    VALUES ( SELECT ID FROM FILMS WHERE name = 'Jaws', SELECT ID FROM USERS WHERE name = 'Name1' );
INSERT INTO FILM_LIKES (FILM_ID, LIKED_BY_USER_ID)
    VALUES ( SELECT ID FROM FILMS WHERE name = 'Jaws', SELECT ID FROM USERS WHERE name = 'Name3' );
INSERT INTO FILM_LIKES (FILM_ID, LIKED_BY_USER_ID)
    VALUES ( SELECT ID FROM FILMS WHERE name = 'Terminator', SELECT ID FROM USERS WHERE name = 'Name3' );

INSERT INTO DIRECTORS (NAME)
VALUES ('Lucas'), ('Coppola');

INSERT INTO FILM_DIRECTOR (DIRECTOR_ID, FILM_ID)
    VALUES (SELECT ID FROM DIRECTORS WHERE name = 'Coppola', SELECT ID FROM FILMS WHERE name = 'Terminator');
INSERT INTO FILM_DIRECTOR (DIRECTOR_ID, FILM_ID)
    VALUES (SELECT ID FROM DIRECTORS WHERE name = 'Lucas', SELECT ID FROM FILMS WHERE name = 'Jaws');
INSERT INTO FILM_DIRECTOR (DIRECTOR_ID, FILM_ID)
VALUES (SELECT ID FROM DIRECTORS WHERE name = 'Coppola', SELECT ID FROM FILMS WHERE name = 'Snatch');
