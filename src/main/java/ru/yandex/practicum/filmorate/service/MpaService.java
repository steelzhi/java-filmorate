package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Service
public class MpaService {
    private final MpaStorage mpaStorage;

    @Autowired
    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Mpa create(Mpa mpa) {
        return mpaStorage.create(mpa);
    }

    public Mpa update(Mpa mpa) {
        return mpaStorage.update(mpa);
    }

    public List<Mpa> get() {
        return mpaStorage.get();
    }

    public Mpa get(Long id) {
        return mpaStorage.get(id);
    }
}