create table if not exists PUBLIC.GENRE
(
    ID   INTEGER not null,
    NAME CHARACTER VARYING,
    constraint GENRE_PK
        primary key (ID)
);

create table if not exists PUBLIC.MPA_RATING
(
    ID   INTEGER not null,
    NAME CHARACTER VARYING,
    constraint "MPA_RATING_pk"
        primary key (ID)
);

create table if not exists PUBLIC.FILMS
(
    ID           INTEGER           not null,
    TITLE        CHARACTER VARYING not null,
    DESCRIPTION  CHARACTER VARYING not null,
    RELEASE_DATE DATE              not null,
    DURATION     INTEGER           not null,
    MPA_RATING   INTEGER           not null,
    constraint FILMS_PK
        primary key (ID),
    constraint "FILMS_MPA_RATING_ID_fk"
        foreign key (MPA_RATING) references PUBLIC.MPA_RATING
);

create table if not exists PUBLIC.FILM_GENRE
(
    FILM_ID  INTEGER,
    GENRE_ID INTEGER,
    constraint "film_genre_FILMS__fk"
        foreign key (FILM_ID) references PUBLIC.FILMS,
    constraint "film_genre_GENRE__fk"
        foreign key (GENRE_ID) references PUBLIC.GENRE
);

create table if not exists PUBLIC.USERS
(
    ID       INTEGER           not null,
    EMAIL    CHARACTER VARYING not null,
    LOGIN    CHARACTER VARYING not null,
    NAME     CHARACTER VARYING not null,
    BIRTHDAY DATE              not null,
    constraint USERS_PK
        primary key (ID)
);

create table if not exists PUBLIC.FILM_LIKES
(
    FILM_ID          INTEGER,
    LIKED_BY_USER_ID INTEGER,
    constraint "film_likes_FILMS_ID_fk"
        foreign key (FILM_ID) references PUBLIC.FILMS,
    constraint "film_likes_USERS__fk"
        foreign key (LIKED_BY_USER_ID) references PUBLIC.USERS
);

create table if not exists PUBLIC.USER_FRIENDS
(
    USER_ID   INTEGER,
    FRIEND_ID INTEGER,
    CONFIRMED BOOLEAN,
    constraint "USER_FRIENDS_USERS_ID_fk"
        foreign key (FRIEND_ID) references PUBLIC.USERS,
    constraint "user_friends_USERS__fk"
        foreign key (USER_ID) references PUBLIC.USERS
);


