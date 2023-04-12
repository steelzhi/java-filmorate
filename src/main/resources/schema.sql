CREATE TABLE IF NOT EXISTS PUBLIC.FILMS (
	FILM_ID INTEGER NOT NULL AUTO_INCREMENT,
	NAME VARCHAR_IGNORECASE(50) NOT NULL,
	DESCRIPTION VARCHAR_IGNORECASE NOT NULL,
	MPA_ID INTEGER NOT NULL,
	RELEASE_DATE DATE NOT NULL,
	DURATION INTEGER NOT NULL,
	CONSTRAINT FILMS_PK PRIMARY KEY (FILM_ID),
	CONSTRAINT FILMS_FK_1 FOREIGN KEY (MPA_ID) REFERENCES PUBLIC.MPA(MPA_ID) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS PUBLIC.GENRES (
	GENRE_ID INTEGER NOT NULL AUTO_INCREMENT,
	GENRE VARCHAR_IGNORECASE,
	CONSTRAINT GENRES_PK PRIMARY KEY (GENRE_ID)
);

CREATE TABLE IF NOT EXISTS PUBLIC.FILM_GENRES (
	FILM_GENRES_ID INTEGER NOT NULL AUTO_INCREMENT,
	FILM_ID INTEGER NOT NULL,
	GENRE_ID INTEGER NOT NULL,
	CONSTRAINT FILM_GENRES_PK PRIMARY KEY (FILM_GENRES_ID),
	CONSTRAINT FILM_GENRES_FK FOREIGN KEY (FILM_ID) REFERENCES PUBLIC.FILMS(FILM_ID) ON DELETE RESTRICT ON UPDATE RESTRICT,
	CONSTRAINT FILM_GENRES_FK_1 FOREIGN KEY (GENRE_ID) REFERENCES PUBLIC.GENRES(GENRE_ID) ON DELETE RESTRICT ON UPDATE RESTRICT
);

CREATE TABLE IF NOT EXISTS PUBLIC.MPA (
	MPA_ID INTEGER NOT NULL AUTO_INCREMENT,
	MPA VARCHAR_IGNORECASE NOT NULL,
	CONSTRAINT MPA_PK PRIMARY KEY (MPA_ID)
);

CREATE TABLE IF NOT EXISTS PUBLIC.USERS (
	USER_ID INTEGER NOT NULL AUTO_INCREMENT,
	EMAIL VARCHAR_IGNORECASE(50),
	LOGIN VARCHAR_IGNORECASE(30) NOT NULL,
	NAME VARCHAR_IGNORECASE,
	BIRTHDAY DATE NOT NULL,
	CONSTRAINT USERS_PK PRIMARY KEY (USER_ID)
);

CREATE TABLE IF NOT EXISTS PUBLIC.USER_LIKES (
	USER_LIKES_ID INTEGER NOT NULL AUTO_INCREMENT,
	FILM_ID INTEGER NOT NULL,
	USER_ID INTEGER NOT NULL,
	CONSTRAINT USER_LIKES_PK PRIMARY KEY (USER_LIKES_ID),
	CONSTRAINT USER_LIKES_FK FOREIGN KEY (FILM_ID) REFERENCES PUBLIC.FILMS(FILM_ID) ON DELETE RESTRICT ON UPDATE
	RESTRICT,
	CONSTRAINT USER_LIKES_FK_1 FOREIGN KEY (USER_ID) REFERENCES PUBLIC.USERS(USER_ID) ON DELETE RESTRICT ON UPDATE
	RESTRICT
);

CREATE TABLE IF NOT EXISTS PUBLIC.FRIENDSHIP (
	FRIENDSHIP_ID INTEGER NOT NULL AUTO_INCREMENT,
	FRIEND_ONE_ID INTEGER NOT NULL,
	FRIEND_TWO_ID INTEGER NOT NULL,
	FRIENDSHIP_STATUS BOOLEAN NOT NULL,
	CONSTRAINT FRIENDSHIP_PK PRIMARY KEY (FRIENDSHIP_ID),
	CONSTRAINT FRIENDSHIP_FK FOREIGN KEY (FRIEND_ONE_ID) REFERENCES PUBLIC.USERS(USER_ID) ON DELETE RESTRICT ON UPDATE
	RESTRICT,
	CONSTRAINT FRIENDSHIP_FK_1 FOREIGN KEY (FRIEND_TWO_ID) REFERENCES PUBLIC.USERS(USER_ID) ON DELETE RESTRICT ON UPDATE
	RESTRICT
);