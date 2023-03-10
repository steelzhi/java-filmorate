package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public abstract class Controller<T> {
    public abstract T create(@RequestBody T value);

    public abstract T update(@RequestBody T value);

    public abstract List<T> get();
}
