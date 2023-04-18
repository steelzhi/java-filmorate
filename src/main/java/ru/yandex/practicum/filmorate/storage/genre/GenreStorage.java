package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genres;
import ru.yandex.practicum.filmorate.storage.Storage;

public interface GenreStorage extends Storage<Genres> {
    void checkGenreExististing(Long genreId);
}