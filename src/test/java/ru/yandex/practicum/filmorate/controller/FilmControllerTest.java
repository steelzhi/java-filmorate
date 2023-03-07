package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    FilmController controller;

    @BeforeEach
    void createControllerWithEmptyData() {
        controller = new FilmController();
    }

    @Test
    void addCorrectFilm() {
        Film film = new Film(null, "Scary Movie", "AAA",
                LocalDate.of(2001, 02, 03), 100);
        controller.create(film);
        assertTrue(controller.get().size() == 1, "Фильм некорректно добавлен в список фильмов");
        assertTrue(controller.get().contains(film),
                "Список фильмов не содержит добавленного фильма");
    }

    @Test
    void addFilmWithEmptyName() {
        Film film = new Film(null, "", "AAA",
                LocalDate.of(2001, 02, 03), 100);
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> controller.create(film));
        assertEquals("Введены некорректные параметры фильма!", validationException.getMessage(),
                "В список добавлен фильм с пустым именем");
    }

    @Test
    void addFilmWithTooLongDescription() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 201; i++) {
            builder.append("A");
        }
        Film film = new Film(null, "Scary Movie", builder.toString(),
                LocalDate.of(2001, 02, 03), 100);
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> controller.create(film));
        assertEquals("Введены некорректные параметры фильма!", validationException.getMessage(),
                "В список добавлен фильм с описанием длиннее 200 символов");
    }

    @Test
    void addFilmWithTooEarlyReleaseDate() {
        Film film = new Film(null, "Scary Movie", "AAA",
                LocalDate.of(1895, 12, 27), 100);
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> controller.create(film));
        assertEquals("Введены некорректные параметры фильма!", validationException.getMessage(),
                "В список добавлен фильм с релизом до 28.12.1895");
    }

    @Test
    void addFilmWithNonPositiveDuration() {
        Film film = new Film(null, "Scary Movie", "AAA",
                LocalDate.of(2001, 02, 03), 0);
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> controller.create(film));
        assertEquals("Введены некорректные параметры фильма!", validationException.getMessage(),
                "В список добавлен фильм с длительностью <= 0");
    }

    @Test
    void addNullInsteadOfFilm() {
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> controller.create(null));
        assertEquals("Введены некорректные параметры фильма!", validationException.getMessage(),
                "В список добавлен фильм без параметров");
    }

    @Test
    void updateFilm() {
        Film film1 = new Film(null, "Scary Movie", "AAA",
                LocalDate.of(2001, 02, 03), 100);
        controller.create(film1);
        Integer film1Id = film1.getId();
        Film film2 = new Film(null, "Scary Movie 2", "BBB",
                LocalDate.of(2004, 06, 01), 112);
        film2.setId(film1Id);
        controller.update(film2);

        assertTrue(controller.get().size() == 1, "Фильм некорректно обновлен в списке фильмов");
        assertTrue(controller.get().contains(film2),
                "Список фильмов не содержит обновленного фильма");
        assertFalse(controller.get().contains(film1),
                "Список фильмов содержит необновленный фильм");
    }

    @Test
    void getFilms() {
        Film film1 = new Film(null, "Scary Movie", "AAA",
                LocalDate.of(2001, 02, 03), 100);
        controller.create(film1);
        Film film2 = new Film(3, "Scary Movie 2", "BBB",
                LocalDate.of(2004, 06, 01), 112);
        controller.create(film2);

        assertTrue(controller.get().size() == 2, "Фильмы некорректно добавлены в список фильмов");
        assertTrue(controller.get().contains(film2), "Список фильмов не содержит фильма \""
                + film1.getName() + "\"");
        assertTrue(controller.get().contains(film2), "Список фильмов не содержит фильма \""
                + film2.getName() + "\"");
    }
}