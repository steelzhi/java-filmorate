package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
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
    void createCorrectUser() {
        User user = new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01));
        controller.create(user);
        assertTrue(controller.get().size() == 1,
                "Пользователь некорректно добавлен в список пользователей");
        assertTrue(controller.get().contains(user),
                "Список пользователей не содержит добавленного пользователя");
    }

    @Test
    void createUserWithEmptyEmail() {
        User user = new User(null, "", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01));
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> controller.create(user));
        assertEquals("Введены некорректные параметры пользователя!", validationException.getMessage(),
                "Добавлен пользователь с пустым email");
    }

    @Test
    void createUserWithEmptyLogin() {
        User user = new User(null, "test@ya.ru", "", "Vasily",
                LocalDate.of(1990, 01, 01));
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> controller.create(user));
        assertEquals("Введены некорректные параметры пользователя!", validationException.getMessage(),
                "Добавлен пользователь с пустым логином");
    }

    @Test
    void createUserWithEmptyName() {
        User user = new User(null, "test@ya.ru", "Vasya54", "",
                LocalDate.of(1990, 01, 01));
        controller.create(user);
        assertTrue(user.getName().equals(user.getLogin()),
                "Пользователю без имени не был автоматически присвоен логин в качестве имени");
    }

    @Test
    void createUserWithIncorrectDate() {
        User user = new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(2990, 01, 01));
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> controller.create(user));
        assertEquals("Введены некорректные параметры пользователя!", validationException.getMessage(),
                "Добавлен пользователь с датой рождения после текущей даты");
    }

    @Test
    void updateUser() {
        User user1 = new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01));
        controller.create(user1);
        Integer user1Id = user1.getId();
        User user2 = new User(3, "nottest@gmail.com", "AlexTheGreat", "Alexander",
                LocalDate.of(1980, 03, 05));
        user2.setId(user1.getId());
        controller.update(user2);

        assertTrue(controller.get().size() == 1,
                "Пользователь некорректно обновлен в списке пользователей");
        assertTrue(controller.get().contains(user2),
                "Список пользователей не содержит обновленного пользователя");
        assertFalse(controller.get().contains(user1),
                "Список пользователей содержит необновленного пользователя");
    }

    @Test
    void getUsers() {
        User user1 = new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01));
        controller.create(user1);
        User user2 = new User(3, "nottest@gmail.com", "AlexTheGreat", "Alexander",
                LocalDate.of(1980, 03, 05));
        user2.setId(user1.getId());
        controller.create(user2);

        assertTrue(controller.get().size() == 2,
                "Пользователи некорректно добавлены в список пользователей");
        assertTrue(controller.get().contains(user2), "Список пользователей не содержит пользователя "
                + user1.getName());
        assertTrue(controller.get().contains(user2), "Список пользователей не содержит пользователя "
                + user2.getName());
    }
}