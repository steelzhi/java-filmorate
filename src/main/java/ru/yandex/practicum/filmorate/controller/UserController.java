package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NoSuitableUnitException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@RestController
@Slf4j
public class UserController extends Controller<User> {
    private final Map<Integer, User> users = new HashMap<>();
    private int id = 1;

    @PostMapping("/users")
    public User create(@RequestBody User user) {
        checkUserParams(user);
        log.info("Добавление нового пользователя {}.", user);
        user.setId(id);
        users.put(user.getId(), getUserWithNonEmptyName(user));
        id++;
        return user;
    }

    @PutMapping("/users")
    public User update(@RequestBody User user) {
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

    private void checkUserParams(User user) {
        if (user.getEmail().isBlank()
                || !user.getEmail().contains("@")
                || user.getLogin().isBlank()
                || user.getLogin().contains(" ")
                || user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Введены некорректные параметры пользователя!");
        }
    }

    private User getUserWithNonEmptyName(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        return user;
    }
}