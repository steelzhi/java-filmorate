package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NoSuitableUnitException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("inMemoryFilmStorage")
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private long id = 1;
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film create(Film film) {
        log.info("Добавление нового фильма {}.", film);
        film.setId(id);
        films.put(film.getId(), film);
        id++;
        return film;
    }

    @Override
    public Film update(Film film) {
        Film filmWithOldParams = films.get(film.getId());
        if (filmWithOldParams == null) {
            throw new NoSuitableUnitException("Фильма с таким id нет в списке!");
        }

        log.info("Изменение данных ранее добавленного фильма \"{}\".", filmWithOldParams);
        films.put(film.getId(), film);
        return film;
    }

    @Override
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

    @Override
    public Map<Long, Film> getValues() {
        Map<Long, Film> copyOfFilms = new HashMap<>(films);
        return copyOfFilms;
    }
}