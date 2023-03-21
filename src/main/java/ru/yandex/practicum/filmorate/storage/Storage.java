package ru.yandex.practicum.filmorate.storage;

import java.util.List;
import java.util.Map;

public interface Storage<T> {

    T create(T value);

    T update(T value);

    List<T> get();

    T get(Long id);

    Map<Long, T> getValues();
}