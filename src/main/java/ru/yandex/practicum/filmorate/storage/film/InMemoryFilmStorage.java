package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NoSuitableUnitException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private long id = 1;
    private final Map<Long, Film> films = new HashMap<>();
    private static final int MAX_LENGTH = 200;

    public Film create(Film film) {
        checkFilmParams(film);
        log.info("Добавление нового фильма {}.", film);
        film.setId(id);
        films.put(film.getId(), film);
        id++;
        return film;
    }

    public Film update(Film film) {
        checkFilmParams(film);
        Film filmWithOldParams = films.get(film.getId());
        if (filmWithOldParams == null) {
            throw new NoSuitableUnitException("Фильма с таким id нет в списке!");
        }

        log.info("Изменение данных ранее добавленного фильма \"{}\".", filmWithOldParams);
        films.put(film.getId(), film);
        return film;
    }

    public List<Film> get() {
        log.info("Получение списка фильмов.");
        List<Film> filmList = new ArrayList<>();
        filmList.addAll(films.values());
        return filmList;
    }

    @Override
    public Film get(Long id) {
        log.info("Получение фильма с id {}.", id);
        if (!films.containsKey(id)) {
            throw new NoSuitableUnitException("Фильма с id = " + id + " нет в списке.");
        }

        return films.get(id);
    }

    private void checkFilmParams(Film film) {
        if (film == null
                || film.getDescription().length() > MAX_LENGTH
                || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Введены некорректные параметры фильма!");
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Map<Long, Film> getValues() {
        return films;
    }
}
