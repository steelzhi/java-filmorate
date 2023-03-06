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
    void addFilm() {
        Film film1 = new Film( "Scary Movie", "AAA",
                LocalDate.of(2001, 02, 03), 100);
        controller.createFilm(film1);
        assertTrue(controller.getFilms().size() == 1, "Фильм некорректно добавлен в список фильмов");
        assertTrue(controller.getFilms().contains(film1), "Список фильмов не содержит добавленного фильма");

        Film film2 = new Film( "", "AAA",
                LocalDate.of(2001, 02, 03), 100);
        ValidationException validationException2 = assertThrows(ValidationException.class,
                () -> controller.createFilm(film2));
        assertEquals("Введены некорректные параметры фильма!", validationException2.getMessage(),
                "В список добавлен фильм с пустым именем");

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 201; i++) {
            builder.append("A");
        }
        Film film3 = new Film("Scary Movie", builder.toString(),
                LocalDate.of(2001, 02, 03), 100);
        ValidationException validationException3 = assertThrows(ValidationException.class,
                () -> controller.createFilm(film3));
        assertEquals("Введены некорректные параметры фильма!", validationException3.getMessage(),
                "В список добавлен фильм с описанием длиннее 200 символов");

        Film film4 = new Film("Scary Movie", "AAA",
                LocalDate.of(1895, 12, 27), 100);
        ValidationException validationException4 = assertThrows(ValidationException.class,
                () -> controller.createFilm(film4));
        assertEquals("Введены некорректные параметры фильма!", validationException4.getMessage(),
                "В список добавлен фильм с релизом до 28.12.1895");

        Film film5 = new Film("Scary Movie", "AAA",
                LocalDate.of(2001, 02, 03), 0);
        ValidationException validationException5 = assertThrows(ValidationException.class,
                () -> controller.createFilm(film5));
        assertEquals("Введены некорректные параметры фильма!", validationException5.getMessage(),
                "В список добавлен фильм с длительностью <= 0");

        ValidationException validationException6 = assertThrows(ValidationException.class,
                () -> controller.createFilm(null));
        assertEquals("Введены некорректные параметры фильма!", validationException6.getMessage(),
                "В список добавлен фильм без параметров");

        Film film7 = new Film("", "AAA",
                LocalDate.of(1895, 12, 27), 0);
        ValidationException validationException7 = assertThrows(ValidationException.class,
                () -> controller.createFilm(film7));
        assertEquals("Введены некорректные параметры фильма!", validationException7.getMessage(),
                "В список добавлен фильм с длительностью <= 0");
    }

    @Test
    void updateFilm() {
        Film film1 = new Film("Scary Movie", "AAA",
                LocalDate.of(2001, 02, 03), 100);
        controller.createFilm(film1);
        Integer film1Id = film1.getId();
        Film film2 = new Film("Scary Movie 2", "BBB",
                LocalDate.of(2004, 06, 01), 112);
        film2.setId(film1Id);
        controller.updateFilm(film2);

        assertTrue(controller.getFilms().size() == 1, "Фильм некорректно обновлен в списке фильмов");
        assertTrue(controller.getFilms().contains(film2), "Список фильмов не содержит обновленного фильма");
        assertFalse(controller.getFilms().contains(film1), "Список фильмов содержит необновленный фильм");
    }

    @Test
    void getFilms() {
        Film film1 = new Film("Scary Movie", "AAA",
                LocalDate.of(2001, 02, 03), 100);
        controller.createFilm(film1);
        Film film2 = new Film("Scary Movie 2", "BBB",
                LocalDate.of(2004, 06, 01), 112);
        controller.createFilm(film2);

        assertTrue(controller.getFilms().size() == 2, "Фильмы некорректно добавлены в список фильмов");
        assertTrue(controller.getFilms().contains(film2), "Список фильмов не содержит фильма \""
                + film1.getName() + "\"");
        assertTrue(controller.getFilms().contains(film2), "Список фильмов не содержит фильма \""
                + film2.getName() + "\"");
    }
}