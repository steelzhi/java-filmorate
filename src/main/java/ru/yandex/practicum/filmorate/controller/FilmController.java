/*
Никита, здравствуйте!
Задание выполнил, проверьте, пожалуйста.
Прим.: для хранения фильмов и пользователей я выбрал структуру HashSet. Возможно, удобнее было бы использовать HashMap,
но тогда возникло бы дублирование кода: т.к. согласно ТЗ в классах фильма и пользователя должны быть id, то при выборе
HashMap этот id стал бы повторяться дважды (у объекта и у ключа хэшмапы). Можно ли здесь оставить HashSet? Или с учетом
дальнейших ТЗ нужно выбрать другую структуру для хранения?
 */
package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NoSuitableUnitException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@RestController
@Slf4j
public class FilmController {
    private final Set<Film> films = new HashSet<>();
    private static Integer id = 1;

    @PostMapping("/films")
    public Film createFilm(@RequestBody Film film) {
        checkFilmParams(film);
        log.info("Добавление нового фильма.");
        film.setId(id);
        films.add(film);
        id++;
        return film;
    }

    @PutMapping("/films")
    public Film updateFilm(@RequestBody Film film) {
        checkFilmParams(film);
        Film filmWithOldParams = getFilmById(film.getId());
        if (filmWithOldParams == null) {
            throw new NoSuitableUnitException("Фильма с таким id нет в списке!");
        }

        films.remove(filmWithOldParams);
        log.info("Изменение данных ранее добавленного фильма \"{}\".", film.getName());
        films.add(film);
        return film;
    }

    @GetMapping("/films")
    public Set<Film> getFilms() {
        return films;
    }


    private void checkFilmParams(Film film) {
        if (film == null
                || film.getName().isBlank()
                || film.getDescription().isBlank()
                || film.getDescription().length() > 200
                || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))
                || film.getDuration() <= 0) {
            throw new ValidationException("Введены некорректные параметры фильма!");
        }
    }

    private Film getFilmById(Integer id) {
        for (Film film : films) {
            if (film.getId() == id) {
                return film;
            }
        }
        return null;
    }
}
