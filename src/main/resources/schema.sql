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
    ID           INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    NAME         CHARACTER VARYING(255) not null,
    DESCRIPTION  CHARACTER VARYING(200) not null,
    RELEASE_DATE DATE                   not null,
    DURATION     INTEGER                not null,
    MPA_ID       INTEGER                references MPA on update cascade on delete cascade
);

create table if not exists PUBLIC.FILM_GENRE
(
    FILM_ID  INTEGER references FILMS on update cascade on delete cascade,
    GENRE_ID INTEGER references GENRES on update cascade on delete cascade,
    PRIMARY KEY (FILM_ID, GENRE_ID)
);

create table if not exists PUBLIC.USERS
(
    ID       INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    EMAIL    CHARACTER VARYING(255) not null,
    LOGIN    CHARACTER VARYING      not null,
    NAME     CHARACTER VARYING(255) not null,
    BIRTHDAY DATE                   not null
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

create table if not exists PUBLIC.USER_FEEDS
(
    EVENT_ID    INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    USER_ID     INTEGER references USERS on update cascade on delete cascade not null,
    TIME_STAMP  LONG not null,
    EVENT_TYPE  ENUM ('LIKE', 'REVIEW', 'FRIEND') not null,
    OPERATION   ENUM ('REMOVE', 'ADD', 'UPDATE') not null,
    ENTITY_ID   INTEGER not null
);