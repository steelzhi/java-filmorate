package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NoSuitableUnitException;
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
        return mpaStorage.create(mpa);
    }

    public List<Mpa> get() {
        return mpaStorage.get();
    }

    public Mpa get(Long id) {
        if (!doesMpaExist(id)) {
            throw new NoSuitableUnitException("Рейтинг с указанным id не существует!");
        }
        return mpaStorage.get(id);
    }

    private boolean doesMpaExist(Long mpaId) {
        for (Mpa mpa : get()) {
            if (mpa.getId() == mpaId) {
                return true;
            }
        }
        return false;
    }
}