package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NoSuitableUnitException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public FilmStorage getFilmStorage() {
        return filmStorage;
    }

    public Film putLike(Long id, Long userId) {
        log.info("Фильму с id = {} ставит лайк пользователь с id = {}", id, userId);
        if (!doFilmAndUserExist(id, userId)) {
            throw new NoSuitableUnitException("Фильм или пользователь с указанными id не существуют!");
        }

        Film film = filmStorage.get(id);
        film.addUserLike(userId);
        return film;
    }

    public Film deleteLike(Long id, Long userId) {
        log.info("Удаление лайка у фильма с id = {} от пользователя с id = {}", id, userId);
        if (!doFilmAndUserExist(id, userId)) {
            throw new NoSuitableUnitException("Фильм или пользователь с указанными id не существуют!");
        }

        Film film = filmStorage.get(id);
        film.deleteUserLike(userId);
        return film;
    }

    public List<Film> getMostLikedFilms(Integer listSize) {
        log.info("Отображение {} фильмов с наибольшим числом лайков", listSize);
        List<Film> sortedListByLikesCount = new ArrayList<>();
        sortedListByLikesCount.addAll(filmStorage.getValues().values());
        Collections.sort(sortedListByLikesCount, (o1, o2) -> {
            if (o1.getAllLikesCount() < o2.getAllLikesCount()) {
                return 1;
            }
            return -1;
        });

        if (listSize == 1) {
            return List.of(sortedListByLikesCount.get(0));
        }
        if (sortedListByLikesCount.size() < listSize) {
            return sortedListByLikesCount;
        } else {
            List<Film> selectedFilms = sortedListByLikesCount.subList(0, listSize - 1);
            System.out.println(selectedFilms);
            return selectedFilms;
        }
    }

    private boolean doFilmAndUserExist(Long filmId, Long userId) {
        if (filmStorage.getValues().containsKey(filmId)
                && userService.getUserStorage().getValues().containsKey(userId)) {
            return true;
        }
        return false;
    }
}
