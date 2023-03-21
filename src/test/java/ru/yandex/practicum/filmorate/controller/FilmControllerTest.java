package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    FilmController filmController;
    UserController userController;

    boolean areFilmParamsValid(Film film) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.usingContext().getValidator();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        if (violations.isEmpty()) {
            return true;
        }
        return false;
    }

    @BeforeEach
    void createControllerWithEmptyData() {
        filmController = new FilmController(new FilmService(new InMemoryFilmStorage(),
                new InMemoryUserStorage()));
    }

    @Test
    void addCorrectFilm() {
        Film film = new Film(null, "Scary Movie", "AAA",
                LocalDate.of(2001, 02, 03), 100, new HashSet<>());
        filmController.create(film);
        assertTrue(filmController.get().size() == 1, "Фильм некорректно добавлен в список фильмов");
        assertTrue(filmController.get().contains(film),
                "Список фильмов не содержит добавленного фильма");
    }

    @Test
    void addFilmWithEmptyName() {
        Film film = new Film(null, "", "AAA",
                LocalDate.of(2001, 02, 03), 100, new HashSet<>());
        boolean areFilmParamsValid = areFilmParamsValid(film);
        assertTrue(areFilmParamsValid == false, "Введены недопустимое имя фильма.");
    }

    @Test
    void addFilmWithTooLongDescription() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 201; i++) {
            builder.append("A");
        }
        Film film = new Film(null, "Scary Movie", builder.toString(),
                LocalDate.of(2001, 02, 03), 100, new HashSet<>());
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertEquals("Введены некорректные параметры фильма!", validationException.getMessage(),
                "В список добавлен фильм с описанием длиннее 200 символов");
    }

    @Test
    void addFilmWithTooEarlyReleaseDate() {
        Film film = new Film(null, "Scary Movie", "AAA",
                LocalDate.of(1895, 12, 27), 100, new HashSet<>());
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertEquals("Введены некорректные параметры фильма!", validationException.getMessage(),
                "В список добавлен фильм с релизом до 28.12.1895");
    }

    @Test
    void addFilmWithNonPositiveDuration() {
        Film film = new Film(null, "Scary Movie", "AAA",
                LocalDate.of(2001, 02, 03), 0, new HashSet<>());
        boolean areFilmParamsValid = areFilmParamsValid(film);
        assertTrue(areFilmParamsValid == false, "Введена длительность фильма <= 0.");
    }

    @Test
    void addNullInsteadOfFilm() {
        Film film = null;
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertEquals("Введены некорректные параметры фильма!", validationException.getMessage(),
                "В список добавлен фильм без параметров");
    }

    @Test
    void updateFilm() {
        Film film1 = new Film(null, "Scary Movie", "AAA",
                LocalDate.of(2001, 02, 03), 100, new HashSet<>());
        filmController.create(film1);
        long film1Id = film1.getId();
        Film film2 = new Film(null, "Scary Movie 2", "BBB",
                LocalDate.of(2004, 06, 01), 112, new HashSet<>());
        film2.setId(film1Id);
        filmController.update(film2);

        assertTrue(filmController.get().size() == 1, "Фильм некорректно обновлен в списке фильмов");
        assertTrue(filmController.get().contains(film2),
                "Список фильмов не содержит обновленного фильма");
        assertFalse(filmController.get().contains(film1),
                "Список фильмов содержит необновленный фильм");
    }

    @Test
    void getFilms() {
        Film film1 = new Film(null, "Scary Movie", "AAA",
                LocalDate.of(2001, 02, 03), 100, new HashSet<>());
        filmController.create(film1);
        Film film2 = new Film(3L, "Scary Movie 2", "BBB",
                LocalDate.of(2004, 06, 01), 112, new HashSet<>());
        filmController.create(film2);

        assertTrue(filmController.get().size() == 2, "Фильмы некорректно добавлены в список фильмов");
        assertTrue(filmController.get().contains(film2), "Список фильмов не содержит фильма \""
                + film1.getName() + "\"");
        assertTrue(filmController.get().contains(film2), "Список фильмов не содержит фильма \""
                + film2.getName() + "\"");
    }

    @Test
    void getFilm() {
        Film film1 = new Film(null, "Scary Movie", "AAA",
                LocalDate.of(2001, 02, 03), 100, new HashSet<>());
        filmController.create(film1);
        Film film2 = new Film(3L, "Scary Movie 2", "BBB",
                LocalDate.of(2004, 06, 01), 112, new HashSet<>());
        filmController.create(film2);

        assertEquals(filmController.get(1L), film1, "Полученный по id фильм не совпадает с добавленным");
        assertEquals(filmController.get(2L), film2, "Полученный по id фильм не совпадает с добавленным");
    }

    @Test
    void putLike() {
        UserStorage userStorage = new InMemoryUserStorage();
        userController = new UserController(new UserService(userStorage));
        filmController = new FilmController(new FilmService(new InMemoryFilmStorage(), userStorage));

        Film film1 = new Film(null, "Scary Movie", "AAA",
                LocalDate.of(2001, 02, 03), 100, new HashSet<>());
        filmController.create(film1);
        User user = userController.create(new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01), new HashSet<>()));

        filmController.putLike(film1.getId(), user.getId());

        assertTrue(filmController.get(1L).getAllLikesCount() == 1,
                "У фильма неправильное общее количество лайков");

        filmController.putLike(film1.getId(), user.getId());
        filmController.putLike(film1.getId(), user.getId());
        assertTrue(filmController.get(1L).getAllLikesCount() == 1,
                "У фильма неправильное общее количество лайков");

        assertTrue(filmController.get(1L).getUserLikes().contains(user.getId()),
                "У фильма неправильное id пользователя, поставившего лайк");
    }

    @Test
    void deleteLike() {
        UserStorage userStorage = new InMemoryUserStorage();
        userController = new UserController(new UserService(userStorage));
        filmController = new FilmController(new FilmService(new InMemoryFilmStorage(), userStorage));

        Film film1 = new Film(null, "Scary Movie", "AAA",
                LocalDate.of(2001, 02, 03), 100, new HashSet<>());
        filmController.create(film1);
        User user1 = userController.create(new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01), new HashSet<>()));

        User user2 = userController.create(new User(3L, "nottest@gmail.com", "AlexTheGreat", "Alexander",
                LocalDate.of(1980, 03, 05), new HashSet<>()));

        filmController.putLike(film1.getId(), user1.getId());
        filmController.putLike(film1.getId(), user2.getId());

        filmController.deleteLike(1L, user1.getId());
        assertTrue(filmController.get(1L).getAllLikesCount() == 1,
                "После удаления 1 лайка у фильма неправильное общее количество лайков");
        assertFalse(filmController.get(1L).getUserLikes().contains(user1.getId()),
                "У фильма неправильное id пользователя, чей лайк остался после удаления");

        filmController.deleteLike(1L, user2.getId());
        assertTrue(filmController.get(1L).getAllLikesCount() == 0,
                "После удаления всех лайков у фильма ненулевое общее количество лайков");
    }

    @Test
    void getMostLikedFilms() {
        UserStorage userStorage = new InMemoryUserStorage();
        userController = new UserController(new UserService(userStorage));
        filmController = new FilmController(new FilmService(new InMemoryFilmStorage(), userStorage));

        Film film1 = new Film(null, "Scary Movie", "AAA",
                LocalDate.of(2001, 02, 03), 100, new HashSet<>());
        filmController.create(film1);
        Film film2 = new Film(3L, "Scary Movie 2", "BBB",
                LocalDate.of(2004, 06, 01), 112, new HashSet<>());
        filmController.create(film2);
        Film film3 = new Film(47L, "Scary Movie 3", "CCC",
                LocalDate.of(2014, 03, 21), 80, new HashSet<>());
        filmController.create(film3);

        User user1 = userController.create(new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01), new HashSet<>()));
        User user2 = userController.create(new User(3L, "nottest@gmail.com", "AlexTheGreat", "Alexander",
                LocalDate.of(1980, 03, 05), new HashSet<>()));
        User user3 = userController.create(new User(0L, "a@business.com", "Singkh", "Si",
                LocalDate.of(1985, 06, 15), new HashSet<>()));

        filmController.putLike(film1.getId(), user1.getId());
        filmController.putLike(film1.getId(), user2.getId());
        filmController.putLike(film1.getId(), user3.getId());
        filmController.putLike(film2.getId(), user1.getId());
        filmController.putLike(film3.getId(), user3.getId());
        filmController.putLike(film3.getId(), user2.getId());

        List<Film> tenMostLikedFilms = filmController.getMostLikedFilms(10);
        assertEquals(tenMostLikedFilms.get(0), film1,
                "Фильмы в списке самых популярных неправильно отсортированы");
        assertEquals(tenMostLikedFilms.get(1), film3,
                "Фильмы в списке самых популярных неправильно отсортированы");
        assertEquals(tenMostLikedFilms.get(2), film2,
                "Фильмы в списке самых популярных неправильно отсортированы");

        List<Film> mostLikedFilm = filmController.getMostLikedFilms(1);
        assertEquals(mostLikedFilm.get(0), film1,
                "Фильмы в списке самых популярных неправильно отсортированы");
    }
}
