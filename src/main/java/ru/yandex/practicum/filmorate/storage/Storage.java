package ru.yandex.practicum.filmorate.storage;

import java.util.List;
import java.util.Map;

public interface Storage<T> {

    public T create(T value);

    public T update(T value);

    public List<T> get();

    public T get(Long id);

    public Long getId();

    public Map<Long, T> getValues();

}
