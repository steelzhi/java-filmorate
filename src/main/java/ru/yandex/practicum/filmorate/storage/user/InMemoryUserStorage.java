package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NoSuitableUnitException;
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

    @Override
    public User create(User user) {
        log.info("Добавление нового пользователя {}.", user);
        user.setId(id);
        users.put(user.getId(), getUserWithNonEmptyName(user));
        id++;
        return user;
    }

    @Override
    public User update(User user) {
        User userWithOldParams = users.get(user.getId());
        if (userWithOldParams == null) {
            throw new NoSuitableUnitException("Пользователя с таким id нет в списке!");
        }

        log.info("Изменение данных имеющегося пользователя {}.", userWithOldParams);
        users.put(user.getId(), getUserWithNonEmptyName(user));
        return user;
    }

    @Override
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

    @Override
    public Map<Long, User> getValues() {
        Map<Long, User> copyOfUsers = new HashMap<>(users);
        return copyOfUsers;
    }

    private User getUserWithNonEmptyName(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        return user;
    }
}
