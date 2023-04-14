package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NoSuitableUnitException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class FilmService {
    private static final int MAX_LENGTH = 200;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final Comparator<Film> likeCountComparator = (o1, o2) -> {
        if (o1.getAllLikesCount() < o2.getAllLikesCount()) {
            return 1;
        }
        return -1;
    };

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        if (!areFilmParamsCorrect(film)) {
            throw new ValidationException("Введены некорректные параметры фильма!");
        }

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (!doesFilmExist(film.getId())) {
            throw new NoSuitableUnitException("Фильм с указанным id не существует!");
        }
        areFilmParamsCorrect(film);
        return filmStorage.update(film);
    }

    public List<Film> get() {
        return filmStorage.get();
    }

    public Film get(Long id) {
        return filmStorage.get(id);
    }

    public Film putLike(Long id, Long userId) {
        log.info("Фильму с id = {} ставит лайк пользователь с id = {}", id, userId);
        if (!doesFilmExist(id) || !doesUserExist(userId)) {
            throw new NoSuitableUnitException("Фильм или пользователь с указанными id не существуют!");
        }

        return filmStorage.putLike(id, userId);
    }

    public Film deleteLike(Long id, Long userId) {
        log.info("Удаление лайка у фильма с id = {} от пользователя с id = {}", id, userId);
        if (!doesFilmExist(id) || !doesUserExist(userId)) {
            throw new NoSuitableUnitException("Фильм или пользователь с указанными id не существуют!");
        }

        return filmStorage.deleteLike(id, userId);
    }

    public List<Film> getMostLikedFilms(Integer listSize) {
        log.info("Отображение {} фильмов с наибольшим числом лайков", listSize);
        List<Film> sortedListByLikesCount = new ArrayList<>();
        sortedListByLikesCount.addAll(filmStorage.getValues().values());
        Collections.sort(sortedListByLikesCount, likeCountComparator);

        if (listSize == 1) {
            return List.of(sortedListByLikesCount.get(0));
        }
        if (sortedListByLikesCount.size() < listSize) {
            return sortedListByLikesCount;
        } else {
            List<Film> selectedFilms = sortedListByLikesCount.subList(0, listSize - 1);
            return selectedFilms;
        }
    }

    private boolean areFilmParamsCorrect(Film film) {
        if (film == null
                || film.getDescription().length() > MAX_LENGTH
                || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            return false;
        }
        return true;
    }

    private boolean doesUserExist(Long userId) {
        if (userStorage.getValues().containsKey(userId)) {
            return true;
        }
        return false;
    }

    private boolean doesFilmExist(Long filmId) {
        if (filmStorage.getValues().containsKey(filmId)) {
            return true;
        }
        return false;
    }
}
