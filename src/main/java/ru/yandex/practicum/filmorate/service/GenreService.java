package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genres;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@Service
public class GenreService {
    private final GenreStorage genreStorage;

    public GenreService(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    public Genres create(Genres genres) {
        return genreStorage.create(genres);
    }

    public Genres update(Genres genres) {
        return genreStorage.update(genres);
    }

    public List<Genres> get() {
        return genreStorage.get();
    }

    public Genres get(Long id) {
        return genreStorage.get(id);
    }
}