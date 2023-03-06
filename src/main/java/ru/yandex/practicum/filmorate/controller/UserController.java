package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NoSuitableUnitException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@RestController
@Slf4j
public class UserController {
    private final Set<User> users = new HashSet<>();
    private static Integer id = 1;


    @PostMapping("/users")
    public User createUser(@RequestBody User user) {
        checkUserParams(user);
        log.info("Добавление нового пользователя.");
        user.setId(id);
        users.add(getUserWithNonEmptyName(user));
        id++;
        return user;
    }

    @PutMapping("/users")
    public User updateUser(@RequestBody User user) {
        checkUserParams(user);
        User userWithOldParams = getUserById(user.getId());
        if (userWithOldParams == null) {
            throw new NoSuitableUnitException("Пользователя с таким id нет в списке!");
        }

        users.remove(userWithOldParams);
        log.info("Изменение данных имеющегося пользователя {}.", user.getName());
        users.add(getUserWithNonEmptyName(user));
        return user;
    }

    @GetMapping("/users")
    public Set<User> getUsers() {
        return users;
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
        if (user.getName() == null || user.getName().equals("")) {
            user.setName(user.getLogin());
        }
        return user;
    }

    private User getUserById(int id) {
        for (User user: users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }
}
