create table if not exists PUBLIC.GENRES
(
    ID   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME CHARACTER VARYING(255) not null
    );

create table if not exists PUBLIC.MPA
(
    ID   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME CHARACTER VARYING(255) not null
    );

create table if not exists PUBLIC.FILMS
(
    ID           INTEGER                GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME         CHARACTER VARYING(255) not null,
    DESCRIPTION  CHARACTER VARYING(200) not null,
    RELEASE_DATE DATE                   not null,
    DURATION     INTEGER                not null,
    MPA_ID       INTEGER                references MPA on update cascade on delete cascade
    );

create table if not exists PUBLIC.USERS
(
    ID       INTEGER                GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    EMAIL    CHARACTER VARYING(255) not null,
    LOGIN    CHARACTER VARYING      not null,
    NAME     CHARACTER VARYING(255) not null,
    BIRTHDAY DATE                   not null
    );

create table if not exists PUBLIC.REVIEWS
(
    ID          INTEGER                GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    CONTENT     CHARACTER VARYING(255) not null,
    IS_POSITIVE BOOLEAN                not null,
    USEFUL      INTEGER                DEFAULT 0,
    USER_ID     INTEGER                references USERS on update cascade on delete cascade,
    FILM_ID     INTEGER                references FILMS on update cascade on delete cascade
    );

create table if not exists PUBLIC.FILM_GENRE
(
    FILM_ID  INTEGER references FILMS on update cascade on delete cascade,
    GENRE_ID INTEGER references GENRES on update cascade on delete cascade,
    PRIMARY KEY (FILM_ID, GENRE_ID)
    );

create table if not exists PUBLIC.FILM_LIKES
(
    FILM_ID          INTEGER references FILMS on update cascade on delete cascade,
    LIKED_BY_USER_ID INTEGER references USERS on update cascade on delete cascade,
    PRIMARY KEY (FILM_ID, LIKED_BY_USER_ID)
);

create table if not exists PUBLIC.USER_FRIENDS
(
    USER_ID   INTEGER references USERS on update cascade on delete cascade,
    FRIEND_ID INTEGER references USERS on update cascade on delete cascade,
    PRIMARY KEY (USER_ID, FRIEND_ID)
    );

create table if not exists PUBLIC.REVIEW_LIKES
(
    REVIEW_ID INTEGER references REVIEWS on update cascade on delete cascade,
    USER_ID   INTEGER references USERS on update cascade on delete cascade,
    PRIMARY KEY (REVIEW_ID, USER_ID)
    );

create table if not exists PUBLIC.REVIEW_DISLIKES
(
    REVIEW_ID INTEGER references REVIEWS on update cascade on delete cascade,
    USER_ID   INTEGER references USERS on update cascade on delete cascade,
    PRIMARY KEY (REVIEW_ID, USER_ID)
    );

CREATE TABLE  IF NOT EXISTS PUBLIC.DIRECTORS
(
    ID   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME CHARACTER VARYING(255) not null
    );

CREATE TABLE IF NOT EXISTS PUBLIC.FILM_DIRECTOR
(
    DIRECTOR_ID  INTEGER references DIRECTORS on update cascade on delete cascade,
    FILM_ID INTEGER references FILMS on update cascade on delete cascade,
    PRIMARY KEY (DIRECTOR_ID, FILM_ID)
    );
