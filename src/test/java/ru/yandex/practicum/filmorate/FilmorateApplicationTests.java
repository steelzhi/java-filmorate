package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;

    @BeforeEach
    public void init() {
        userStorage.create(new User(1L, "aa@aa.ru", "d", "s", LocalDate.of(2000, 01, 01), new HashSet<>(), new HashSet<>()));
    }

    @Test
    public void testFindUserById() {
        List<User> users = userStorage.get();
        System.out.println(userStorage);
        assertTrue(!users.isEmpty(), "Список пользователей пуст!");
        assertTrue(users.get(0).getEmail().equals("aa@aa.ru"), "Электронные адреса не совпадают!");
    }
}