package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaController extends CrudController<Mpa> {
    private final MpaService mpaService;

    public MpaController(MpaService mpaService) {
        this.mpaService = mpaService;
    }

    @Override
    public Mpa create(Mpa mpa) {
        return mpaService.create(mpa);
    }

    @Override
    public Mpa update(Mpa mpa) {
        return mpaService.update(mpa);
    }

    @Override
    @GetMapping
    public List<Mpa> get() {
        return mpaService.get();
    }

    @Override
    @GetMapping("/{id}")
    public Mpa get(@PathVariable Long id) {
        return mpaService.get(id);
    }
}