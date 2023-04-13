package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genres;

import java.util.List;

public interface GenreStorage {
    List<Genres> get();

    Genres get(Long id);
}
