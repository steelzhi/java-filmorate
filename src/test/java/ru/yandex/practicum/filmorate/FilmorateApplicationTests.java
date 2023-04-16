package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NoSuitableUnitException;
import ru.yandex.practicum.filmorate.exception.NotUniqueEntityException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genres;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;
    private final UserService userService;
    private final FilmDbStorage filmStorage;
    private final FilmService filmService;
    private final GenreDbStorage genreStorage;
    private final MpaDbStorage mpaStorage;

    public void deleteAllUsersData() {
        userStorage.deleteAllUsers();
    }

    public void deleteAllFilmsData() {
        filmStorage.deleteAllFilms();
    }

    @Test
    void userCreateCorrectUser() {
        deleteAllUsersData();
        User user = new User(null, "ivanov@ya.ru", "Iv", "Ivan",
                LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user);
        Long userId = user.getId();
        assertTrue(userStorage.get().size() == 1, "Количество пользователей в БД не равно 1.");
        assertTrue(userStorage.get(userId).getEmail().equals("ivanov@ya.ru"),
                "Электронные адреса не совпадают!");
        assertTrue(userStorage.get(userId).getName().equals("Ivan"), "Имена не совпадают!");
        assertTrue(userStorage.get(userId).getLogin().equals("Iv"), "Логины не совпадают!");
        assertTrue(userStorage.get(userId).getBirthday().equals(LocalDate.of(2000, 01, 01)),
                "Даты рождения не совпадают!");
    }

    @Test
    void userCreateUserWithoutName() {
        deleteAllUsersData();
        User user = new User(null, "ivanov@ya.ru", "Iv", null,
                LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user);
        Long userId = user.getId();
        assertTrue(userStorage.get(userId).getName().equals("Iv"), "Имена не совпадают!");
    }

    @Test
    void userCreateUsersWithSameEmails() {
        deleteAllUsersData();
        User user1 = new User(null, "ivanov@ya.ru", "Iv1", null,
                LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        User user2 = new User(null, "ivanov@ya.ru", "Iv2", null,
                LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user1);

        NotUniqueEntityException notUniqueEntityException = assertThrows(NotUniqueEntityException.class,
                () -> userStorage.create(user2));
        assertEquals("Пользователь с email = " + user2.getEmail() + " уже есть в БД",
                notUniqueEntityException.getMessage(),
                "В список добавлен пользователь с email, который уже был добавлени другим пользователем");
    }

    @Test
    void userUpdateUserWithCorrectParams() {
        deleteAllUsersData();
        User user1 = new User(null, "ivanov@ya.ru", "Iv5", "Ivan",
                LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user1);
        String name = user1.getName();
        String login = user1.getLogin();
        LocalDate birthday = user1.getBirthday();
        int currentNumberOfUsersInDb = userStorage.get().size();

        User user2 = new User(user1.getId(), "ivanov@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.update(user2);
        int numberOfUsersInDbAfterUserUpdate = userStorage.get().size();

        assertTrue(currentNumberOfUsersInDb == numberOfUsersInDbAfterUserUpdate,
                "После изменения данных пользователя изменилось число пользователей!");
        assertTrue(userStorage.get(user2.getId()).getName().equals(name), "Имя пользователя изменилось!");
        assertFalse(userStorage.get(user2.getId()).getLogin().equals(login),
                "Логин пользователя не изменился!");
        assertFalse(userStorage.get(user2.getId()).getBirthday().equals(birthday),
                "День рождения пользователя не изменился!");
    }

    @Test
    void userUpdateUserWithInorrectLogin() {
        deleteAllUsersData();
        User user1 = new User(null, "ivanov@ya.ru", "Iv5", "Ivan",
                LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userService.create(user1);
        User user2 = new User(user1.getId(), "ivanov@ya.ru", "Iv an", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());

        ValidationException validationException = assertThrows(ValidationException.class,
                () -> userService.update(user2));

        assertEquals("Введен некорректный логин пользователя!", validationException.getMessage(),
                "В список добавлен пользователь с некорректным логином");
    }

    @Test
    void getAllUsers() {
        deleteAllUsersData();
        List<User> userList = userStorage.get();
        int currentListSize = userList.size();
        User user1 = new User(null, "ivanov@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user1);

        assertTrue(userStorage.get().size() == currentListSize + 1,
                "Количество пользователей в БД не соответсвует реальному количеству!");
        User user2 = new User(null, "ivanov2@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user2);
        assertTrue(userStorage.get().size() == currentListSize + 2,
                "Количество пользователей в БД не соответсвует реальному количеству!");
    }

    @Test
    void getAllUserWithId() {
        deleteAllUsersData();
        User user = new User(null, "ivanov@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user);
        Long lastAddedUserId = userStorage.get().get(userStorage.get().size() - 1).getId();
        assertTrue(userStorage.get(lastAddedUserId).equals(user),
                "Добавленный в БД пользователь с id = " + user.getId() +
                        " не соответствует полученному из БД пользователю!");
    }

    @Test
    void addFriend() {
        deleteAllUsersData();
        User user = new User(null, "ivanov@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User friend = new User(null, "ivanov2@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user);
        userStorage.create(friend);
        Long friendOfUserId = userStorage.get().get(userStorage.get().size() - 1).getId();
        user.addFriend(friendOfUserId);
        assertFalse(user.getFriendsIds().isEmpty(), "Список друзей пользователя содержит некорректные id!");
        assertTrue(user.getFriendsIds().contains(friendOfUserId),
                "Список друзей пользователя не содержит id добавленного друга!");
        assertTrue(friend.getFriendsIds().isEmpty(),
                "Список друзей друга содержит id пользователя, дружбу с которым он не подтвердил!");
    }

    @Test
    void deleteFriend() {
        deleteAllUsersData();
        User user = new User(null, "ivanov@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User friend = new User(null, "ivanov2@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user);
        userStorage.create(friend);
        Long friendOfUserId = userStorage.get().get(userStorage.get().size() - 1).getId();
        user.addFriend(friendOfUserId);
        user.deleteFriend(friendOfUserId);
        assertTrue(user.getFriendsIds().isEmpty(), "Список друзей пользователя не пуст!");
        assertFalse(user.getFriendsIds().contains(friendOfUserId),
                "Список друзей пользователя содержит id удаленного друга");
    }

    @Test
    void getFriendsIds() {
        deleteAllUsersData();
        User user = new User(null, "ivanov@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User friend1 = new User(null, "ivanov2@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User friend2 = new User(null, "ivanov3@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user);
        userStorage.create(friend1);
        userStorage.create(friend2);
        Long firstFriendOfUserId = userStorage.get().get(userStorage.get().size() - 2).getId();
        Long secondFriendOfUserId = userStorage.get().get(userStorage.get().size() - 1).getId();
        user.addFriend(firstFriendOfUserId);
        user.addFriend(secondFriendOfUserId);
        assertTrue(user.getFriendsIds().size() == 2,
                "Размер списка друзей пользователя не соответствует реальному количеству друзей!");
        assertTrue(user.getFriendsIds().contains(firstFriendOfUserId),
                "Список друзей пользователя несодержит id первого друга");
        assertTrue(user.getFriendsIds().contains(secondFriendOfUserId),
                "Список друзей пользователя несодержит id второго друга");
    }

    @Test
    void getCommonFriendsIds() {
        deleteAllUsersData();
        User user1 = new User(null, "ivanov@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User friend1 = new User(null, "ivanov2@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User friend2 = new User(null, "ivanov3@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userService.create(user1);
        userService.create(friend1);
        userService.create(friend2);
        Long firstUserId = userService.get().get(userService.get().size() - 3).getId();
        Long firstFriendOfFirstUserId = userService.get().get(userService.get().size() - 2).getId();
        Long secondFriendOfFirstUserId = userService.get().get(userService.get().size() - 1).getId();
        userService.addFriend(firstUserId, firstFriendOfFirstUserId);
        userService.addFriend(firstUserId, firstFriendOfFirstUserId);
        userService.addFriend(firstUserId, secondFriendOfFirstUserId);
        user1 = userService.get().get(userService.get().size() - 3);
        User user2 = new User(null, "ivanov4@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User friend3 = new User(null, "ivanov5@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userService.create(user2);
        userService.create(friend3);
        Long secondUserId = userService.get().get(userService.get().size() - 2).getId();
        Long firstFriendOfSecondUserId = userService.get().get(userService.get().size() - 1).getId();
        userService.addFriend(secondUserId, firstFriendOfSecondUserId);
        userService.addFriend(secondUserId, secondFriendOfFirstUserId);
        user2 = userService.get().get(userService.get().size() - 2);
        List<User> commonFriends = userService.getCommonFriends(user1.getId(), user2.getId());

        assertFalse(commonFriends.isEmpty(), "Список общих друзей пуст!");
        assertTrue(commonFriends.size() == 1,
                "Размер списка общих друзей не совпадает с реальным количество общих друзей!");
        assertTrue(commonFriends.get(0).equals(friend2),
                "Список общих друзей содержит неверных пользователей!");
    }

    @Test
    void createFilmWithCorrectParams() {
        deleteAllFilmsData();
        Film film = new Film(null, "Scary Movie", "Amecican comedy movie from 2000",
                null, new Mpa(3, "PG-13"),
                LocalDate.of(2000, 01, 01), 100, null);
        filmStorage.create(film);

        assertTrue(filmStorage.get().size() == 1,
                "Список фильмов в БД содержит неверное количество фильмов!");
        assertTrue(filmStorage.get((long) (filmStorage.get().size())).getName().equals("Scary Movie"),
                "Название добавленного фильма не соответствует названию в БД!");
        assertEquals(filmStorage.get((long) (filmStorage.get().size())).getDescription(),
                "Amecican comedy movie from 2000",
                "Описание добавленного фильма не соответствует описанию в БД!");
        assertEquals(filmStorage.get((long) (filmStorage.get().size())).getMpa().getName(),
                "PG-13",
                "Рейтинг добавленного фильма не соответствует рейтингу в БД!");
        assertEquals(filmStorage.get((long) (filmStorage.get().size())).getReleaseDate(),
                LocalDate.of(2000, 01, 01),
                "Дата добавленного фильма не соответствует дате в БД!");
        assertEquals(filmStorage.get((long) (filmStorage.get().size())).getDuration(), 100,
                "Длительность добавленного фильма не соответствует длительности в БД!");
    }

    @Test
    void createFilmWithIncorrectDescription() {
        deleteAllFilmsData();
        Film film = new Film(null, "Scary Movie",
                "Amecican comedy movie from 2000 Amecican comedy movie from 2000 Amecican comedy movie " +
                        "from 2000 Amecican comedy movie from 2000 Amecican comedy movie from 2000 Amecican comedy " +
                        "movie from 2000 Amecican comedy movie from 2000",
                null, new Mpa(3, "PG-13"),
                LocalDate.of(2000, 01, 01), 100, null);
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> filmService.create(film));

        assertEquals("Введены некорректные параметры фильма!", validationException.getMessage(),
                "В БД добавлен фильм со слишком длинным описанием");
    }

    @Test
    void createFilmWithIncorrectDate() {
        deleteAllFilmsData();
        Film film = new Film(null, "Scary Movie", "Amecican comedy movie from 2000",
                null, new Mpa(3, "PG-13"),
                LocalDate.of(1800, 01, 01), 100, null);
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> filmService.create(film));

        assertEquals("Введены некорректные параметры фильма!", validationException.getMessage(),
                "В БД добавлен фильм со некорректной датой");
    }

    @Test
    void updateFilmWithCorrectParams() {
        deleteAllFilmsData();
        Film film = new Film(null, "Scary Movie", "Amecican comedy movie from 2000",
                null, new Mpa(3, "PG-13"),
                LocalDate.of(2000, 01, 01), 100, null);
        filmStorage.create(film);
        Long filmId = filmStorage.get().get(0).getId();
        Film updatedFilm = new Film(filmId, "Scary Movie", "Amecican comedy movie from 2003",
                null, new Mpa(3, "PG-13"),
                LocalDate.of(2003, 02, 01), 110, null);
        filmStorage.update(updatedFilm);
        assertTrue(filmStorage.get().size() == 1,
                "При изменении уже добавленного фильма изменился размер списка фильмов в БД!");
        assertTrue(filmStorage.get(filmId).getName().equals("Scary Movie"),
                "Название фильма не соответствует названию в БД!");
        assertTrue(filmStorage.get(filmId).getDescription().equals("Amecican comedy movie from 2003"),
                "Описание измененного фильма не соответствует описанию в БД!");
        assertTrue(filmStorage.get(filmId).getMpa().getName().equals("PG-13"),
                "Рейтинг фильма не соответствует рейтингу в БД!");
        assertTrue(filmStorage.get(filmId).getReleaseDate().equals(LocalDate.of(2003, 02, 01)),
                "Дата измененного фильма не соответствует дате в БД!");
        assertTrue(filmStorage.get(filmId).getDuration() == 110,
                "Длительность измененного фильма не соответствует длительности в БД!");
    }

    @Test
    void updateAbsentFilm() {
        deleteAllFilmsData();
        Film film = new Film(null, "Scary Movie", "Amecican comedy movie from 2000",
                null, new Mpa(3, "PG-13"),
                LocalDate.of(2000, 01, 01), 100, null);
        filmService.create(film);
        Long filmId = filmService.get().get(0).getId();
        Long wrongFilmId = filmId + 1;
        Film updatedFilm = new Film(wrongFilmId, "Scary Movie", "Amecican comedy movie from 2003",
                null, new Mpa(3, "PG-13"),
                LocalDate.of(2003, 02, 01), 110, null);

        NoSuitableUnitException noSuitableUnitException = assertThrows(NoSuitableUnitException.class,
                () -> filmService.update(updatedFilm));

        assertEquals("Фильм с указанным id не существует!", noSuitableUnitException.getMessage(),
                "В БД изменен фильм с несуществующим id");
    }

    @Test
    void updateFilmWithIncorrectGenre() {
        deleteAllFilmsData();
        Film film = new Film(null, "Scary Movie", "Amecican comedy movie from 2000",
                null, new Mpa(3, "PG-13"),
                LocalDate.of(2000, 01, 01), 100, null);
        filmStorage.create(film);
        Long filmId = filmStorage.get().get(0).getId();
        Film updatedFilm = new Film(filmId, "Scary Movie", "Amecican comedy movie from 2003",
                List.of(new Genres(10, "Adventure")), new Mpa(3, "PG-13"),
                LocalDate.of(2003, 02, 01), 110, null);
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> filmStorage.update(updatedFilm));

        assertEquals("Неверно введен жанр фильма", validationException.getMessage(),
                "В БД изменен фильм с несуществующим id");
    }

    @Test
    void getAllFilms() {
        deleteAllFilmsData();
        Film film1 = new Film(null, "Scary Movie", "Amecican comedy movie from 2000",
                null, new Mpa(3, "PG-13"),
                LocalDate.of(2000, 01, 01), 100, null);
        Film film2 = new Film(null, "Scary Movie 2", "Amecican comedy movie from 2005",
                null, new Mpa(3, "PG-13"),
                LocalDate.of(2005, 03, 04), 99, null);
        filmStorage.create(film1);
        filmStorage.create(film2);

        assertTrue(filmStorage.get().size() == 2,
                "Размер списка фильмов в БД не соответствует числу добавленных фильмов!");
    }

    @Test
    void getFilmWithCorrectId() {
        deleteAllFilmsData();
        Film film = new Film(null, "Scary Movie", "Amecican comedy movie from 2000",
                null, new Mpa(3, "PG-13"),
                LocalDate.of(2000, 01, 01), 100, null);
        filmStorage.create(film);
        assertTrue(filmStorage.get(film.getId()).getName().equals("Scary Movie"),
                "Название добавленного фильма не соответствует названию в БД!");
        assertTrue(filmStorage.get(film.getId()).getDescription().equals("Amecican comedy movie from 2000"),
                "Описание добавленного фильма не соответствует описанию в БД!");
        assertTrue(filmStorage.get(film.getId()).getMpa().getName().equals("PG-13"),
                "Рейтинг добавленного фильма не соответствует рейтингу в БД!");
        assertTrue(filmStorage.get(film.getId()).getReleaseDate().equals(
                LocalDate.of(2000, 01, 01)),
                "Дата добавленного фильма не соответствует дате в БД!");
        assertTrue(filmStorage.get(film.getId()).getDuration() == 100,
                "Длительность добавленного фильма не соответствует длительности в БД!");
    }

    @Test
    void getFilmWithInCorrectId() {
        deleteAllFilmsData();
        Film film = new Film(null, "Scary Movie", "Amecican comedy movie from 2000",
                null, new Mpa(3, "PG-13"),
                LocalDate.of(2000, 01, 01), 100, null);
        filmStorage.create(film);
        Long filmId = filmStorage.get().get(0).getId();
        Long wrongFilmId = filmId + 1;

        NoSuitableUnitException noSuitableUnitException = assertThrows(NoSuitableUnitException.class,
                () -> filmStorage.get(wrongFilmId));

        assertEquals("Фильма с таким id нет в БД", noSuitableUnitException.getMessage(),
                "В БД изменен фильм с несуществующим id");
    }

    @Test
    void putLike() {
        deleteAllFilmsData();
        deleteAllUsersData();
        Film film = new Film(null, "Scary Movie", "Amecican comedy movie from 2000",
                null, new Mpa(3, "PG-13"),
                LocalDate.of(2000, 01, 01), 100, null);
        filmStorage.create(film);
        User user = new User(null, "ivanov@ya.ru", "Iv", null,
                LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user);
        filmStorage.putLike(film.getId(), user.getId());

        assertTrue(filmStorage.get(film.getId()).getAllLikesCount() == 1,
                "Количество поставленных фильму лайков не соответствует количеству в БД");
        assertTrue(filmStorage.get(film.getId()).getUserLikes().contains(user.getId()),
                "id пользователя, поставившего лайк, не соответствует id пользователя в БД");
    }

    @Test
    void deleteLike() {
        deleteAllFilmsData();
        deleteAllUsersData();
        Film film = new Film(null, "Scary Movie", "Amecican comedy movie from 2000",
                null, new Mpa(3, "PG-13"),
                LocalDate.of(2000, 01, 01), 100, null);
        filmStorage.create(film);
        User user1 = new User(null, "ivanov@ya.ru", "Iv", null,
                LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user1);
        User user2 = new User(null, "ivanov2@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user2);
        filmStorage.putLike(film.getId(), user1.getId());
        filmStorage.putLike(film.getId(), user2.getId());
        filmStorage.deleteLike(film.getId(), user1.getId());

        assertTrue(filmStorage.get(film.getId()).getAllLikesCount() == 1,
                "После удаления лайка от 1-го пользователя количество лайков в БД неверное!");
        assertTrue(filmStorage.get(film.getId()).getUserLikes().contains(user2.getId()),
                "После удаления лайка от 1-го пользователя id оставшихся лайков пользователей, " +
                        "поставивших лайк, неверные!");
        filmStorage.deleteLike(film.getId(), user2.getId());
        assertTrue(filmStorage.get(film.getId()).getAllLikesCount() == 0,
                "После удаления лайка от 2-го пользователя количество лайков в БД неверное!");
    }

    @Test
    void getMostLikesFilms() {
        deleteAllFilmsData();
        deleteAllUsersData();
        Film film1 = new Film(null, "Scary Movie", "Amecican comedy movie from 2000",
                null, new Mpa(3, "PG-13"),
                LocalDate.of(2000, 01, 01), 100, null);
        Film film2 = new Film(null, "Scary Movie 2", "Amecican comedy movie from 2005",
                null, new Mpa(3, "PG-13"),
                LocalDate.of(2005, 03, 04), 99, null);
        Film film3 = new Film(null, "Scary Movie 3", "Amecican comedy movie from 2010",
                null, new Mpa(3, "PG-13"),
                LocalDate.of(2010, 07, 05), 111, null);
        filmService.create(film1);
        filmService.create(film2);
        filmService.create(film3);
        User user1 = new User(null, "ivanov@ya.ru", "Iv", null,
                LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        User user2 = new User(null, "ivanov2@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userService.create(user1);
        userService.create(user2);
        filmService.putLike(film1.getId(), user1.getId());
        filmService.putLike(film1.getId(), user2.getId());
        filmService.putLike(film3.getId(), user2.getId());

        System.out.println("Популярные фильмы: " + filmService.getMostLikedFilms(3));
        assertTrue(filmService.getMostLikedFilms(3).size() == 2,
                "Размер списка наиболее популярных фильмов не превышает установленный лимит либо содержит не все популярные фильмы");
        assertTrue(filmService.getMostLikedFilms(3).contains(filmService.get(film1.getId())),
                "Список популярных фильмов не содержит популярного фильма " + film1);
        assertTrue(filmService.getMostLikedFilms(3).contains(filmService.get(film3.getId())),
                "Список популярных фильмов не содержит популярного фильма " + film3);
        assertFalse(filmService.getMostLikedFilms(3).contains(filmService.get(film2.getId())),
                "Список популярных фильмов содержит непопулярный фильм " + film2);
    }

    @Test
    void getAllGenres() {
        List<Genres> allGenres = genreStorage.get();
        assertTrue(allGenres.size() == 6,
                "Размер заданного списка жанров не соответствует размеру, получаемому при помощи метода get()!");
    }

    @Test
    void getGenreById() {
        Genres comedy = genreStorage.get(1L);
        assertTrue(comedy.getName().equals("Комедия"),
                "Полученный из БД жанр не соответствует реальному жанру!");
    }


    @Test
    void getAllMpa() {
        List<Mpa> allMpa = mpaStorage.get();
        assertTrue(allMpa.size() == 5,
                "Размер заданного списка рейтингов не соответствует размеру, получаемому при помощи метода " +
                        "get()!");
    }

    @Test
    void getMpaById() {
        Mpa pG = mpaStorage.get(2L);
        assertTrue(pG.getName().equals("PG"),
                "Полученный из БД рейтинг не соответствует реальному рейтингу!");
    }
}