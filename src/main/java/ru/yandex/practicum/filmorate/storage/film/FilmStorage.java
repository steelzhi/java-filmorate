package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.Storage;

public interface FilmStorage extends Storage<Film> {
    Film putLike(Long id, Long userId);

    Film deleteLike(Long id, Long userId);

    boolean doesFilmExist(Long filmId);
}