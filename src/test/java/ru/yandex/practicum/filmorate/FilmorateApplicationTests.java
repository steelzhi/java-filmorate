package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.NotUniqueEntityException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;
    private final UserService userService;

    @Test
    @Order(1)
    public void userCreateCorrectUser() {
        System.out.println("Тест 1. Текущий размер БД пользователей: " + userStorage.get().size());
        System.out.println();

        User user1 = new User(null, "ivanov@ya.ru", "Iv", "Ivan", LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user1);
        Long userId = user1.getId();
        assertTrue(userStorage.get().size() == 1, "Количество пользователей в БД не равно 1.");
        assertTrue(userStorage.get(userId).getEmail().equals("ivanov@ya.ru"), "Электронные адреса не совпадают!");
        assertTrue(userStorage.get(userId).getName().equals("Ivan"), "Имена не совпадают!");
        assertTrue(userStorage.get(userId).getLogin().equals("Iv"), "Логины не совпадают!");
        assertTrue(userStorage.get(userId).getBirthday().equals(LocalDate.of(2000, 01, 01)), "Даты рождения не совпадают!");
    }

    @Test
    @Order(2)
    public void userCreateUserWithoutName() {
        System.out.println("Тест 2. Текущий размер БД пользователей: " + userStorage.get().size());
        System.out.println();


        User user2 = new User(null, "ivanov2@ya.ru", "Iv", null, LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user2);
        Long userId = user2.getId();
        assertTrue(userStorage.get(userId).getName().equals("Iv"), "Имена не совпадают!");
    }

    @Test
    @Order(3)
    public void userCreateUsersWithSameEmails() {
        System.out.println("Тест 3. Текущий размер БД пользователей: " + userStorage.get().size());
        System.out.println();


        User user3 = new User(null, "ivanov3@ya.ru", "Iv1", null, LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        User user4 = new User(null, "ivanov3@ya.ru", "Iv2", null, LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user3);
        System.out.println("Тест 3. Текущий размер БД пользователей: " + userStorage.get().size());

        NotUniqueEntityException notUniqueEntityException = assertThrows(NotUniqueEntityException.class,
                () -> userStorage.create(user4));
        System.out.println("Тест 3. Текущий размер БД пользователей: " + userStorage.get().size());
        System.out.println(userStorage.get().toString());

        assertEquals("Пользователь с email = " + user4.getEmail() + " уже есть в БД", notUniqueEntityException.getMessage(),
                "В список добавлен пользователь с email, который уже был добавлени другим пользователем");
    }

    @Test
    @Order(4)
    public void userUpdateUserWithCorrectParams() {
        System.out.println("Тест 4. Текущий размер БД пользователей: " + userStorage.get().size());
        System.out.println();

        User user5 = new User(null, "ivanov4@ya.ru", "Iv5", "Ivan", LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user5);
        String name = user5.getName();
        String login = user5.getLogin();
        LocalDate birthday = user5.getBirthday();
        int currentNumberOfUsersInDb = userStorage.get().size();

        User user2 = new User(user5.getId(), "ivanov4@ya.ru", "Ivan", "Ivan", LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.update(user2);
        int numberOfUsersInDbAfterUserUpdate = userStorage.get().size();

        assertTrue(currentNumberOfUsersInDb == numberOfUsersInDbAfterUserUpdate, "После изменения данных пользователя изменилось число пользователей!");
        assertTrue(userStorage.get(user2.getId()).getName().equals(name), "Имя пользователя изменилось!");
        assertTrue(!userStorage.get(user2.getId()).getLogin().equals(login), "Логин пользователя не изменился!");
        assertTrue(!userStorage.get(user2.getId()).getBirthday().equals(birthday), "День рождения пользователя не изменился!");
    }

    @Test
    @Order(4)
    public void userUpdateUserWithInorrectLogin() {
        User user5 = new User(null, "ivanov44@ya.ru", "Iv5", "Ivan", LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userService.create(user5);
        User user2 = new User(user5.getId(), "ivanov44@ya.ru", "Iv an", "Ivan", LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());

        ValidationException validationException = assertThrows(ValidationException.class,
                () -> userService.update(user2));

        assertEquals("Введен некорректный логин пользователя!", validationException.getMessage(),
                "В список добавлен пользователь с некорректным логином");

    }

    @Test
    @Order(5)
    public void getAllUsers() {
        System.out.println("Тест 5. Текущий размер БД пользователей: " + userStorage.get().size());
        System.out.println();

        List<User> userList = userStorage.get();
        int currentListSize = userList.size();
        System.out.println(userStorage.get().size());
        User user6 = new User(null, "ivanov5@ya.ru", "Ivan", "Ivan", LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user6);
        System.out.println("Тест 5. Текущий размер БД пользователей: " + userStorage.get().size());

        assertTrue(userStorage.get().size() == currentListSize + 1, "Количество пользователей в БД не соответсвует реальному количеству!");
    }

    @Test
    @Order(6)
    public void getAllUserWithId() {
        User user7 = new User(null, "ivanov7@ya.ru", "Ivan", "Ivan", LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user7);
        Long lastAddedUserId = userStorage.get().get(userStorage.get().size() - 1).getId();
        assertTrue(userStorage.get(lastAddedUserId).equals(user7), "Добавленный в БД пользователь с id = " + user7.getId() + " не соответствует полученному из БД пользователю!");
    }

    @Test
    @Order(7)
    public void addFriend() {
        User user8 = new User(null, "ivanov8@ya.ru", "Ivan", "Ivan", LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User user9 = new User(null, "ivanov9@ya.ru", "Ivan", "Ivan", LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user8);
        userStorage.create(user9);
        Long friendOfUserId = userStorage.get().get(userStorage.get().size() - 1).getId();
        user8.addFriend(friendOfUserId);
        assertTrue(!user8.getFriendsIds().isEmpty(), "Список друзей пользователя содержит некорректные id!");
        assertTrue(user8.getFriendsIds().contains(friendOfUserId), "Список друзей пользователя не содержит id добавленного друга!");
    }

    @Test
    @Order(8)
    public void deleteFriend() {
        User user10 = new User(null, "ivanov10@ya.ru", "Ivan", "Ivan", LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User user11 = new User(null, "ivanov11@ya.ru", "Ivan", "Ivan", LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user10);
        userStorage.create(user11);
        Long friendOfUserId = userStorage.get().get(userStorage.get().size() - 1).getId();
        user10.addFriend(friendOfUserId);
        user10.deleteFriend(friendOfUserId);
        assertTrue(user10.getFriendsIds().isEmpty(), "Список друзей пользователя не пуст!");
        assertTrue(!user10.getFriendsIds().contains(friendOfUserId), "Список друзей пользователя содержит id удаленного друга");
    }

    @Test
    @Order(9)
    public void getFriendsIds() {
        User user12 = new User(null, "ivanov12@ya.ru", "Ivan", "Ivan", LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User user13 = new User(null, "ivanov13@ya.ru", "Ivan", "Ivan", LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User user14 = new User(null, "ivanov14@ya.ru", "Ivan", "Ivan", LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user12);
        userStorage.create(user13);
        userStorage.create(user14);
        Long firstFriendOfUserId = userStorage.get().get(userStorage.get().size() - 2).getId();
        Long secondFriendOfUserId = userStorage.get().get(userStorage.get().size() - 1).getId();
        user12.addFriend(firstFriendOfUserId);
        user12.addFriend(secondFriendOfUserId);
        assertTrue(user12.getFriendsIds().size() == 2, "Размер списка друзей пользователя не соответствует реальному количеству друзей!");
        assertTrue(user12.getFriendsIds().contains(firstFriendOfUserId), "Список друзей пользователя несодержит id первого друга");
        assertTrue(user12.getFriendsIds().contains(secondFriendOfUserId), "Список друзей пользователя несодержит id второго друга");
    }

    @Test
    @Order(10)
    public void getCommonFriendsIds() {
        User user15 = new User(null, "ivanov15@ya.ru", "Ivan", "Ivan", LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User user16 = new User(null, "ivanov16@ya.ru", "Ivan", "Ivan", LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User user17 = new User(null, "ivanov17@ya.ru", "Ivan", "Ivan", LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userService.create(user15);
        userService.create(user16);
        userService.create(user17);
        Long firstUserId = userService.get().get(userService.get().size() - 3).getId();
        Long firstFriendOfFirstUserId = userService.get().get(userService.get().size() - 2).getId();
        Long secondFriendOfFirstUserId = userService.get().get(userService.get().size() - 1).getId();
        userService.addFriend(firstUserId, firstFriendOfFirstUserId);
        userService.addFriend(firstUserId, secondFriendOfFirstUserId);
        user15 = userService.get().get(userService.get().size() - 3);
        User user18 = new User(null, "ivanov18@ya.ru", "Ivan", "Ivan", LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        User user19 = new User(null, "ivanov19@ya.ru", "Ivan", "Ivan", LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userService.create(user18);
        userService.create(user19);
        Long secondUserId = userService.get().get(userService.get().size() - 2).getId();
        Long firstFriendOfSecondUserId = userService.get().get(userService.get().size() - 1).getId();
        userService.addFriend(secondUserId , firstFriendOfSecondUserId);
        userService.addFriend(secondUserId , secondFriendOfFirstUserId);
        user18 = userService.get().get(userService.get().size() - 2);
        List<User> commonFriends = userService.getCommonFriends(user15.getId(), user18.getId());

        assertTrue(!commonFriends.isEmpty(), "Список общих друзей пуст!");
        assertTrue(commonFriends.size() == 1, "Размер списка общих друзей не совпадает с реальным количество общих друзей!");
        assertTrue(commonFriends.get(0).equals(user17), "Список общих друзей содержит неверных пользователей!");
    }



    private List<String> toString(List<User> users) {
        List<String> usersList = new ArrayList<>();
        for (User user : users) {
            usersList.add(user.getId() + " " + user.getEmail());
        }
        return usersList;
    }
}