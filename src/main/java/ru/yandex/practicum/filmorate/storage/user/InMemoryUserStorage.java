package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import ru.yandex.practicum.filmorate.exception.NoSuitableUnitException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private long id = 1;
    private final Map<Long, User> users = new HashMap<>();

    public User create(User user) {
        checkUserParams(user);
        log.info("Добавление нового пользователя {}.", user);
        user.setId(id);
        users.put(user.getId(), getUserWithNonEmptyName(user));
        id++;
        return user;
    }

    @PutMapping("/users")
    public User update(User user) {
        checkUserParams(user);
        User userWithOldParams = users.get(user.getId());
        if (userWithOldParams == null) {
            throw new NoSuitableUnitException("Пользователя с таким id нет в списке!");
        }

        log.info("Изменение данных имеющегося пользователя {}.", userWithOldParams);
        users.put(user.getId(), getUserWithNonEmptyName(user));
        return user;
    }

    @GetMapping("/users")
    public List<User> get() {
        log.info("Получение списка пользователей.");
        List<User> userList = new ArrayList<>();
        userList.addAll(users.values());
        return userList;
    }

    @Override
    public User get(Long id) {
        log.info("Получение пользователя с id {}.", id);
        if (!users.containsKey(id)) {
            throw new NoSuitableUnitException("Пользователя с таким id нет в списке!");
        }

        return users.get(id);
    }

    private void checkUserParams(User user) {
        if (user.getLogin().contains(" ")) {
            log.info("Попытка добавить пользователя с пробелом в логине.");
            throw new ValidationException("Введены некорректный логин пользователя!");
        }
    }

    private User getUserWithNonEmptyName(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        return user;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Map<Long, User> getValues() {
        return users;
    }
}
