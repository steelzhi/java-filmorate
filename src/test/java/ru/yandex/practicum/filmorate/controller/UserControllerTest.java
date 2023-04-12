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

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    UserController userController;

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
        userController = new UserController(new UserService(new InMemoryUserStorage()));
    }

    @Test
    void createCorrectUser() {
        User user = new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01), new HashSet<>(), new HashSet<>());
        userController.create(user);
        assertTrue(userController.get().size() == 1,
                "Пользователь некорректно добавлен в список пользователей");
        assertTrue(userController.get().contains(user),
                "Список пользователей не содержит добавленного пользователя");
    }

    @Test
    void createUserWithEmptyEmail() {
        User user = new User(null, "", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01), new HashSet<>(), new HashSet<>());
        boolean areUserParamsValid = areUserParamsValid(user);
        assertTrue(areUserParamsValid == false, "Введен пустой email.");
    }

    @Test
    void createUserWithEmptyLogin() {
        User user = new User(null, "test@ya.ru", "", "Vasily",
                LocalDate.of(1990, 01, 01), new HashSet<>(), new HashSet<>());
        boolean areUserParamsValid = areUserParamsValid(user);
        assertTrue(areUserParamsValid == false, "Введен пустой логин.");
    }

    @Test
    void createUserWithEmptyName() {
        User user = new User(null, "test@ya.ru", "Vasya54", "",
                LocalDate.of(1990, 01, 01), new HashSet<>(), new HashSet<>());
        userController.create(user);
        assertTrue(user.getName().equals(user.getLogin()),
                "Пользователю без имени не был автоматически присвоен логин в качестве имени");
    }

    @Test
    void createUserWithIncorrectDate() {
        User user = new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(2990, 01, 01), new HashSet<>(), new HashSet<>());
        boolean areUserParamsValid = areUserParamsValid(user);
        assertTrue(areUserParamsValid == false, "Введена дата в будущем.");
    }

    @Test
    void updateUser() {
        User user1 = new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01), new HashSet<>(), new HashSet<>());
        userController.create(user1);
        User user2 = new User(3L, "nottest@gmail.com", "AlexTheGreat", "Alexander",
                LocalDate.of(1980, 03, 05), new HashSet<>(), new HashSet<>());
        user2.setId(user1.getId());
        userController.update(user2);

        assertTrue(userController.get().size() == 1,
                "Пользователь некорректно обновлен в списке пользователей");
        assertTrue(userController.get().contains(user2),
                "Список пользователей не содержит обновленного пользователя");
        assertFalse(userController.get().contains(user1),
                "Список пользователей содержит необновленного пользователя");
    }

    @Test
    void getUsers() {
        User user1 = new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01), new HashSet<>(), new HashSet<>());
        userController.create(user1);
        User user2 = new User(3L, "nottest@gmail.com", "AlexTheGreat", "Alexander",
                LocalDate.of(1980, 03, 05), new HashSet<>(), new HashSet<>());
        userController.create(user2);

        assertTrue(userController.get().size() == 2,
                "Пользователи некорректно добавлены в список пользователей");
        assertTrue(userController.get().contains(user2), "Список пользователей не содержит пользователя "
                + user1.getName());
        assertTrue(userController.get().contains(user2), "Список пользователей не содержит пользователя "
                + user2.getName());
    }

    @Test
    void getUser() {
        User user1 = new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01), new HashSet<>(), new HashSet<>());
        userController.create(user1);
        User user2 = new User(3L, "nottest@gmail.com", "AlexTheGreat", "Alexander",
                LocalDate.of(1980, 03, 05), new HashSet<>(), new HashSet<>());
        userController.create(user2);

        assertEquals(user1, userController.get(user1.getId()), "Некорректный возврат пользователя по id");
        assertEquals(user2, userController.get(user2.getId()), "Некорректный возврат пользователя по id");
    }

    @Test
    void addFriend() {
        User user1 = new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01), new HashSet<>(), new HashSet<>());
        userController.create(user1);
        User user2 = new User(3L, "nottest@gmail.com", "AlexTheGreat", "Alexander",
                LocalDate.of(1980, 03, 05), new HashSet<>(), new HashSet<>());
        userController.create(user2);
        userController.addFriend(user1.getId(), user2.getId());

        assertTrue(userController.get(user1.getId()).getFriendsIds().contains(user2.getId()) &&
                        userController.get(user1.getId()).getFriendsIds().size() == 1,
                "У пользователя " + user1 + " некорректные id друзей");
        assertTrue(userController.get(user2.getId()).getFriendsIds().contains(user1.getId()) &&
                        userController.get(user2.getId()).getFriendsIds().size() == 1,
                "Пользователю " + user2 + " некорректно добавлен id друга " + user1);
    }

    @Test
    void deleteFriend() {
        User user1 = new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01), new HashSet<>(), new HashSet<>());
        userController.create(user1);
        User user2 = new User(3L, "nottest@gmail.com", "AlexTheGreat", "Alexander",
                LocalDate.of(1980, 03, 05), new HashSet<>(), new HashSet<>());
        userController.create(user2);
        userController.addFriend(user1.getId(), user2.getId());
        userController.deleteFriend(user1.getId(), user2.getId());

        assertTrue(userController.get(user1.getId()).getFriendsIds().isEmpty(),
                "У пользователя " + user1 + " не удалился из друзей пользователь " + user2);
        assertTrue(userController.get(user2.getId()).getFriendsIds().isEmpty(),
                "У пользователя " + user2 + " не удалился из друзей пользователь " + user1);
    }

    @Test
    void getFriends() {
        User user1 = new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01), new HashSet<>(), new HashSet<>());
        userController.create(user1);
        User user2 = new User(3L, "nottest@gmail.com", "AlexTheGreat", "Alexander",
                LocalDate.of(1980, 03, 05), new HashSet<>(), new HashSet<>());
        userController.create(user2);
        User user3 = new User(0L, "a@business.com", "Singkh", "Si",
                LocalDate.of(1985, 06, 15), new HashSet<>(), new HashSet<>());
        userController.create(user3);
        userController.addFriend(user1.getId(), user2.getId());
        userController.addFriend(user1.getId(), user3.getId());

        assertTrue(userController.getFriends(user1.getId()).contains(user2)
                        && userController.getFriends(user1.getId()).contains(user3)
                        && userController.getFriends(user1.getId()).size() == 2,
                "У пользователя " + user1 + " некорректные id друзей");
    }

    @Test
    void getCommonFriends() {
        User user1 = new User(null, "test@ya.ru", "Vasya54", "Vasily",
                LocalDate.of(1990, 01, 01), new HashSet<>(), new HashSet<>());
        userController.create(user1);
        User user2 = new User(3L, "nottest@gmail.com", "AlexTheGreat", "Alexander",
                LocalDate.of(1980, 03, 05), new HashSet<>(), new HashSet<>());
        userController.create(user2);
        User user3 = new User(0L, "a@business.com", "Singkh", "Si",
                LocalDate.of(1985, 06, 15), new HashSet<>(), new HashSet<>());
        userController.create(user3);
        User user4 = new User(null, "ab@vc.com", "Jay", "Jay",
                LocalDate.of(1975, 07, 17), new HashSet<>(), new HashSet<>());
        userController.create(user4);
        userController.addFriend(user1.getId(), user2.getId());
        userController.addFriend(user1.getId(), user3.getId());

        assertTrue(userController.getCommonFriends(user2.getId(), user4.getId()).isEmpty(),
                "Некорректное отображение общих друзей у пользователей " + user2 + " и " + user4);

        userController.addFriend(user4.getId(), user2.getId());
        userController.addFriend(user4.getId(), user3.getId());

        assertTrue(userController.getCommonFriends(user1.getId(), user4.getId()).size() == 2
                        && userController.getCommonFriends(user1.getId(), user4.getId()).contains(user2)
                        && userController.getCommonFriends(user1.getId(), user4.getId()).contains(user3),
                "Некорректное отображение общих друзей у пользователей " + user1 + " и " + user4);
    }
}