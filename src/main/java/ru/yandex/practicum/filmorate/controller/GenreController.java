package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genres;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

@RestController
@RequestMapping("/genres")
public class GenreController extends CrudController<Genres>{
    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @Override
    public Genres create(Genres value) {
        return null;
    }

    @Override
    public Genres update(Genres value) {
        return null;
    }

    @GetMapping
    public List get() {
        return genreService.get();
    }

    @GetMapping("/{id}")
    public Genres get(@PathVariable Long id) {
        return genreService.get(id);
    }
}
