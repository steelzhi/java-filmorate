package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    UserController controller;

    @BeforeEach
    void createControllerWithEmptyData() {
        controller = new UserController();
    }

    @Test
    void createUser() {
        User user1 = new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01));
        controller.createUser(user1);
        assertTrue(controller.getUsers().size() == 1,
                "Пользователь некорректно добавлен в список пользователей");
        assertTrue(controller.getUsers().contains(user1),
                "Список пользователей не содержит добавленного пользователя");

        User user2 = new User(null, "", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01));
        ValidationException validationException2 = assertThrows(ValidationException.class,
                () -> controller.createUser(user2));
        assertEquals("Введены некорректные параметры пользователя!", validationException2.getMessage(),
                "Добавлен пользователь с пустым email");

        User user3 = new User(null, "test@ya.ru", "", "Vasily",
                LocalDate.of(1990, 01, 01));
        ValidationException validationException3 = assertThrows(ValidationException.class,
                () -> controller.createUser(user3));
        assertEquals("Введены некорректные параметры пользователя!", validationException3.getMessage(),
                "Добавлен пользователь с пустым логином");

        User user4 = new User(null, "test@ya.ru", "Vasya54", "",
                LocalDate.of(1990, 01, 01));
        controller.createUser(user4);
        assertTrue(user4.getName().equals(user4.getLogin()),
                "Пользователю без имени не был автоматически присвоен логин в качестве имени");

        User user5 = new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(2990, 01, 01));
        ValidationException validationException5 = assertThrows(ValidationException.class,
                () -> controller.createUser(user5));
        assertEquals("Введены некорректные параметры пользователя!", validationException5.getMessage(),
                "Добавлен пользователь с датой рождения после текущей даты");
    }

    @Test
    void updateUser() {
        User user1 = new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01));
        controller.createUser(user1);
        Integer user1Id = user1.getId();
        User user2 = new User(3, "nottest@gmail.com", "AlexTheGreat", "Alexander",
                LocalDate.of(1980, 03, 05));
        user2.setId(user1.getId());
        controller.updateUser(user2);

        assertTrue(controller.getUsers().size() == 1,
                "Пользователь некорректно обновлен в списке пользователей");
        assertTrue(controller.getUsers().contains(user2),
                "Список пользователей не содержит обновленного пользователя");
        assertFalse(controller.getUsers().contains(user1),
                "Список пользователей содержит необновленного пользователя");
    }

    @Test
    void getUsers() {
        User user1 = new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01));
        controller.createUser(user1);
        User user2 = new User(3, "nottest@gmail.com", "AlexTheGreat", "Alexander",
                LocalDate.of(1980, 03, 05));
        user2.setId(user1.getId());
        controller.createUser(user2);

        assertTrue(controller.getUsers().size() == 2,
                "Пользователи некорректно добавлены в список пользователей");
        assertTrue(controller.getUsers().contains(user2), "Список пользователей не содержит пользователя "
                + user1.getName());
        assertTrue(controller.getUsers().contains(user2), "Список пользователей не содержит пользователя "
                + user2.getName());
    }
}