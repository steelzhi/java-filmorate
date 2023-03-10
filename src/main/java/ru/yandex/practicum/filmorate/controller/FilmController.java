/*
Никита, приветствую!
Взаимно)
Исправления внес, проверьте, пожалуйста.
 */
package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NoSuitableUnitException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class FilmController extends Controller<Film> {
    private final Map<Integer, Film> films = new HashMap<>();
    private int id = 1;
    private static final int MAX_LENGTH = 200;

    @PostMapping("/films")
    public Film create(@RequestBody Film film) {
        checkFilmParams(film);
        log.info("Добавление нового фильма {}.", film);
        film.setId(id);
        films.put(film.getId(), film);
        id++;
        return film;
    }

    @PutMapping("/films")
    public Film update(@RequestBody Film film) {
        checkFilmParams(film);
        Film filmWithOldParams = films.get(film.getId());
        if (filmWithOldParams == null) {
            throw new NoSuitableUnitException("Фильма с таким id нет в списке!");
        }

        log.info("Изменение данных ранее добавленного фильма \"{}\".", filmWithOldParams);
        films.put(film.getId(), film);
        return film;
    }

    @GetMapping("/films")
    public List<Film> get() {
        log.info("Получение списка фильмов.");
        List<Film> filmList = new ArrayList<>();
        filmList.addAll(films.values());
        return filmList;
    }

    private void checkFilmParams(Film film) {
        if (film == null
                || film.getName().isBlank()
                || film.getDescription().isBlank()
                || film.getDescription().length() > MAX_LENGTH
                || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))
                || film.getDuration() <= 0) {
            throw new ValidationException("Введены некорректные параметры фильма!");
        }
    }
}
