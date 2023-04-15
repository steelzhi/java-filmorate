package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotUniqueEntityException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;
    private final UserService userService;

    @BeforeEach
    public void deleteAllUsersData() {
        userStorage.deleteAllUsers();
    }

    @Test
    public void userCreateCorrectUser() {
        User user = new User(null, "ivanov@ya.ru", "Iv", "Ivan",
                LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user);
        Long userId = user.getId();
        assertTrue(userStorage.get().size() == 1, "Количество пользователей в БД не равно 1.");
        assertTrue(userStorage.get(userId).getEmail().equals("ivanov@ya.ru"),
                "Электронные адреса не совпадают!");
        assertTrue(userStorage.get(userId).getName().equals("Ivan"), "Имена не совпадают!");
        assertTrue(userStorage.get(userId).getLogin().equals("Iv"), "Логины не совпадают!");
        assertTrue(userStorage.get(userId).getBirthday().equals(LocalDate.of(2000, 01, 01)),
                "Даты рождения не совпадают!");
    }

    @Test
    public void userCreateUserWithoutName() {
        User user = new User(null, "ivanov@ya.ru", "Iv", null,
                LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user);
        Long userId = user.getId();
        assertTrue(userStorage.get(userId).getName().equals("Iv"), "Имена не совпадают!");
    }

    @Test
    public void userCreateUsersWithSameEmails() {
        User user1 = new User(null, "ivanov@ya.ru", "Iv1", null,
                LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        User user2 = new User(null, "ivanov@ya.ru", "Iv2", null,
                LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user1);

        NotUniqueEntityException notUniqueEntityException = assertThrows(NotUniqueEntityException.class,
                () -> userStorage.create(user2));
        assertEquals("Пользователь с email = " + user2.getEmail() + " уже есть в БД",
                notUniqueEntityException.getMessage(),
                "В список добавлен пользователь с email, который уже был добавлени другим пользователем");
    }

    @Test
    public void userUpdateUserWithCorrectParams() {
        User user1 = new User(null, "ivanov@ya.ru", "Iv5", "Ivan",
                LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user1);
        String name = user1.getName();
        String login = user1.getLogin();
        LocalDate birthday = user1.getBirthday();
        int currentNumberOfUsersInDb = userStorage.get().size();

        User user2 = new User(user1.getId(), "ivanov@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.update(user2);
        int numberOfUsersInDbAfterUserUpdate = userStorage.get().size();

        assertTrue(currentNumberOfUsersInDb == numberOfUsersInDbAfterUserUpdate,
                "После изменения данных пользователя изменилось число пользователей!");
        assertTrue(userStorage.get(user2.getId()).getName().equals(name), "Имя пользователя изменилось!");
        assertTrue(!userStorage.get(user2.getId()).getLogin().equals(login),
                "Логин пользователя не изменился!");
        assertTrue(!userStorage.get(user2.getId()).getBirthday().equals(birthday),
                "День рождения пользователя не изменился!");
    }

    @Test
    public void userUpdateUserWithInorrectLogin() {
        User user1 = new User(null, "ivanov@ya.ru", "Iv5", "Ivan",
                LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userService.create(user1);
        User user2 = new User(user1.getId(), "ivanov@ya.ru", "Iv an", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());

        ValidationException validationException = assertThrows(ValidationException.class,
                () -> userService.update(user2));

        assertEquals("Введен некорректный логин пользователя!", validationException.getMessage(),
                "В список добавлен пользователь с некорректным логином");
    }

    @Test
    public void getAllUsers() {
        List<User> userList = userStorage.get();
        int currentListSize = userList.size();
        User user1 = new User(null, "ivanov@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user1);

        assertTrue(userStorage.get().size() == currentListSize + 1,
                "Количество пользователей в БД не соответсвует реальному количеству!");
        User user2 = new User(null, "ivanov2@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user2);
        assertTrue(userStorage.get().size() == currentListSize + 2,
                "Количество пользователей в БД не соответсвует реальному количеству!");
    }

    @Test
    public void getAllUserWithId() {
        User user = new User(null, "ivanov@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user);
        Long lastAddedUserId = userStorage.get().get(userStorage.get().size() - 1).getId();
        assertTrue(userStorage.get(lastAddedUserId).equals(user),
                "Добавленный в БД пользователь с id = " + user.getId() +
                        " не соответствует полученному из БД пользователю!");
    }

    @Test
    public void addFriend() {
        User user1 = new User(null, "ivanov@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User user2 = new User(null, "ivanov2@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user1);
        userStorage.create(user2);
        Long friendOfUserId = userStorage.get().get(userStorage.get().size() - 1).getId();
        user1.addFriend(friendOfUserId);
        assertTrue(!user1.getFriendsIds().isEmpty(), "Список друзей пользователя содержит некорректные id!");
        assertTrue(user1.getFriendsIds().contains(friendOfUserId),
                "Список друзей пользователя не содержит id добавленного друга!");
    }

    @Test
    public void deleteFriend() {
        User user1 = new User(null, "ivanov@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User user2 = new User(null, "ivanov2@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user1);
        userStorage.create(user2);
        Long friendOfUserId = userStorage.get().get(userStorage.get().size() - 1).getId();
        user1.addFriend(friendOfUserId);
        user1.deleteFriend(friendOfUserId);
        assertTrue(user1.getFriendsIds().isEmpty(), "Список друзей пользователя не пуст!");
        assertTrue(!user1.getFriendsIds().contains(friendOfUserId),
                "Список друзей пользователя содержит id удаленного друга");
    }

    @Test
    public void getFriendsIds() {
        User user1 = new User(null, "ivanov@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User user2 = new User(null, "ivanov2@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User user3 = new User(null, "ivanov3@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user1);
        userStorage.create(user2);
        userStorage.create(user3);
        Long firstFriendOfUserId = userStorage.get().get(userStorage.get().size() - 2).getId();
        Long secondFriendOfUserId = userStorage.get().get(userStorage.get().size() - 1).getId();
        user1.addFriend(firstFriendOfUserId);
        user1.addFriend(secondFriendOfUserId);
        assertTrue(user1.getFriendsIds().size() == 2,
                "Размер списка друзей пользователя не соответствует реальному количеству друзей!");
        assertTrue(user1.getFriendsIds().contains(firstFriendOfUserId),
                "Список друзей пользователя несодержит id первого друга");
        assertTrue(user1.getFriendsIds().contains(secondFriendOfUserId),
                "Список друзей пользователя несодержит id второго друга");
    }

    @Test
    public void getCommonFriendsIds() {
        User user1 = new User(null, "ivanov@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User user2 = new User(null, "ivanov2@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User user3 = new User(null, "ivanov3@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userService.create(user1);
        userService.create(user2);
        userService.create(user3);
        Long firstUserId = userService.get().get(userService.get().size() - 3).getId();
        Long firstFriendOfFirstUserId = userService.get().get(userService.get().size() - 2).getId();
        Long secondFriendOfFirstUserId = userService.get().get(userService.get().size() - 1).getId();
        userService.addFriend(firstUserId, firstFriendOfFirstUserId);
        userService.addFriend(firstUserId, firstFriendOfFirstUserId);
        userService.addFriend(firstUserId, secondFriendOfFirstUserId);
        user1 = userService.get().get(userService.get().size() - 3);
        User user4 = new User(null, "ivanov4@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User user5 = new User(null, "ivanov5@ya.ru", "Ivan", "Ivan",
                LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userService.create(user4);
        userService.create(user5);
        Long secondUserId = userService.get().get(userService.get().size() - 2).getId();
        Long firstFriendOfSecondUserId = userService.get().get(userService.get().size() - 1).getId();
        userService.addFriend(secondUserId, firstFriendOfSecondUserId);
        userService.addFriend(secondUserId, secondFriendOfFirstUserId);
        user4 = userService.get().get(userService.get().size() - 2);
        List<User> commonFriends = userService.getCommonFriends(user1.getId(), user4.getId());

        assertTrue(!commonFriends.isEmpty(), "Список общих друзей пуст!");
        assertTrue(commonFriends.size() == 1,
                "Размер списка общих друзей не совпадает с реальным количество общих друзей!");
        assertTrue(commonFriends.get(0).equals(user3), "Список общих друзей содержит неверных пользователей!");
    }
}