create table if not exists GENRES
(
    ID   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME CHARACTER VARYING(255) not null
);

create table if not exists MPA
(
    ID   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME CHARACTER VARYING(255) not null
);

create table if not exists FILMS
(
    ID           INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME         CHARACTER VARYING(255) not null,
    DESCRIPTION  CHARACTER VARYING(200) not null,
    RELEASE_DATE DATE                   not null,
    DURATION     INTEGER                not null,
    MPA_ID       INTEGER                references MPA on update cascade on delete cascade
);

create table if not exists FILM_GENRE
(
    FILM_ID  INTEGER references FILMS on update cascade on delete cascade,
    GENRE_ID INTEGER references GENRES on update cascade on delete cascade
);

create table if not exists USERS
(
    ID       INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    EMAIL    CHARACTER VARYING(255) not null,
    LOGIN    CHARACTER VARYING      not null,
    NAME     CHARACTER VARYING(255) not null,
    BIRTHDAY DATE                   not null
);

create table if not exists FILM_LIKES
(
    FILM_ID          INTEGER references FILMS on update cascade on delete cascade,
    LIKED_BY_USER_ID INTEGER references USERS on update cascade on delete cascade
);

create table if not exists USER_FRIENDS
(
    USER_ID   INTEGER references USERS on update cascade on delete cascade,
    FRIEND_ID INTEGER references USERS on update cascade on delete cascade
);