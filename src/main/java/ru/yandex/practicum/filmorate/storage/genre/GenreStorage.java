package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genres;

import java.util.List;

public interface GenreStorage {
    public List<Genres> get();
}
