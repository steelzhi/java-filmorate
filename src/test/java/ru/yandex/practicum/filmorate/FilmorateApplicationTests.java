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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;

    @Test
    @Order(1)
    public void userCreateCorrectUser() {
        System.out.println("Тест 1. Текущий размер БД пользователей: " + userStorage.get().size());
        System.out.println();

        User user = new User(null, "ivanov@ya.ru", "Iv", "Ivan", LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user);
        Long userId = user.getId();
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


        User user = new User(null, "ivanov2@ya.ru", "Iv", null, LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user);
        Long userId = user.getId();
        assertTrue(userStorage.get(userId).getName().equals("Iv"), "Имена не совпадают!");
    }

    @Test
    @Order(3)
    public void userCreateUsersWithSameEmails() {
        System.out.println("Тест 3. Текущий размер БД пользователей: " + userStorage.get().size());
        System.out.println();


        User user1 = new User(null, "ivanov3@ya.ru", "Iv1", null, LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        User user2 = new User(null, "ivanov3@ya.ru", "Iv2", null, LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user1);
        System.out.println("Тест 3. Текущий размер БД пользователей: " + userStorage.get().size());

        NotUniqueEntityException notUniqueEntityException = assertThrows(NotUniqueEntityException.class,
                () -> userStorage.create(user2));
        System.out.println("Тест 3. Текущий размер БД пользователей: " + userStorage.get().size());
        System.out.println(userStorage.get().toString());

        assertEquals("Пользователь с email = " + user2.getEmail() + " уже есть в БД", notUniqueEntityException.getMessage(),
                "В список добавлен пользователь с email, который уже был добавлени другим пользователем");
    }

/*    @Test
    @Order(4)
    public void userUpdateUser() {
        System.out.println("Тест 4. Текущий размер БД пользователей: " + userStorage.get().size());
        System.out.println();

        User user1 = new User(null, "ivanov4@ya.ru", "Iv5", "Ivan", LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.create(user1);
        String name = user1.getName();
        String login = user1.getLogin();
        LocalDate birthday = user1.getBirthday();
        int currentNumberOfUsersInDb = userStorage.get().size();

        User user2 = new User(user1.getId(), "ivanov4@ya.ru", "Ivan", "Ivan", LocalDate.of(2010, 01, 01), new HashSet<>(), new HashSet<>());
        userStorage.update(user2);
        int numberOfUsersInDbAfterUserUpdate = userStorage.get().size();

        assertTrue(currentNumberOfUsersInDb == numberOfUsersInDbAfterUserUpdate, "После изменения данных пользователя изменилось число пользователей!");
        assertTrue(userStorage.get(user2.getId()).getName().equals(name), "Имя пользователя изменилось!");
        assertTrue(!userStorage.get(user2.getId()).getLogin().equals(login), "Логин пользователя не изменился!");
        assertTrue(!userStorage.get(user2.getId()).getBirthday().equals(birthday), "День рождения пользователя не изменился!");
    }*/

/*
    @Test
    @Order(5)
    public void getAllUsers() {
        System.out.println("Тест 5. Текущий размер БД пользователей: " + userStorage.get().size());
        System.out.println();


        List<User> userList = userStorage.get();
        System.out.println(userStorage.get().size());
        assertTrue(userList.size() == 4, "Количество пользователей в БД не соответсвует реальному количеству!");
    }
*/





    private List<String> toString(List<User> users) {
        List<String> usersList = new ArrayList<>();
        for (User user : users) {
            usersList.add(user.getId() + " " + user.getEmail());
        }
        return usersList;
    }
}