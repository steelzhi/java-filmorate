package ru.yandex.practicum.filmorate.controller;

import java.util.List;

public abstract class CrudController<T> {
    public abstract T create(T value);

    public abstract T update(T value);

    public abstract List<T> get();

    public abstract T get(Long id);
}
