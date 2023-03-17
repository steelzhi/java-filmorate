package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserCrudControllerTest {
    UserController controller;

    private boolean areUserParamsValid(User user) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.usingContext().getValidator();
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        if (violations.isEmpty()) {
            return true;
        }
        return false;
    }

    @BeforeEach
    void createControllerWithEmptyData() {
        controller = new UserController(new UserService(new InMemoryUserStorage()));
    }

    @Test
    void createCorrectUser() {
        User user = new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01), new HashSet<>());
        controller.create(user);
        assertTrue(controller.get().size() == 1,
                "Пользователь некорректно добавлен в список пользователей");
        assertTrue(controller.get().contains(user),
                "Список пользователей не содержит добавленного пользователя");
    }

    @Test
    void createUserWithEmptyEmail() {
        User user = new User(null, "", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01), new HashSet<>());
        boolean areUserParamsValid = areUserParamsValid(user);
        assertTrue(areUserParamsValid == false, "Введен пустой email.");
    }

    @Test
    void createUserWithEmptyLogin() {
        User user = new User(null, "test@ya.ru", "", "Vasily",
                LocalDate.of(1990, 01, 01), new HashSet<>());
        boolean areUserParamsValid = areUserParamsValid(user);
        assertTrue(areUserParamsValid == false, "Введен пустой логин.");
    }

    @Test
    void createUserWithEmptyName() {
        User user = new User(null, "test@ya.ru", "Vasya54", "",
                LocalDate.of(1990, 01, 01), new HashSet<>());
        controller.create(user);
        assertTrue(user.getName().equals(user.getLogin()),
                "Пользователю без имени не был автоматически присвоен логин в качестве имени");
    }

    @Test
    void createUserWithIncorrectDate() {
        User user = new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(2990, 01, 01), new HashSet<>());
        boolean areUserParamsValid = areUserParamsValid(user);
        assertTrue(areUserParamsValid == false, "Введена дата в будущем.");
    }

    @Test
    void updateUser() {
        User user1 = new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01), new HashSet<>());
        controller.create(user1);
        long user1Id = user1.getId();
        User user2 = new User(3L, "nottest@gmail.com", "AlexTheGreat", "Alexander",
                LocalDate.of(1980, 03, 05), new HashSet<>());
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
                LocalDate.of(1990, 01, 01), new HashSet<>());
        controller.create(user1);
        User user2 = new User(3L, "nottest@gmail.com", "AlexTheGreat", "Alexander",
                LocalDate.of(1980, 03, 05), new HashSet<>());
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