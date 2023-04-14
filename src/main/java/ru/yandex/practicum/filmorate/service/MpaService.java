package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NoSuitableUnitException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Service
public class MpaService {
    private final MpaStorage mpaStorage;

    public MpaService(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
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
