package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NoSuitableUnitException;
import ru.yandex.practicum.filmorate.exception.NotUniqueEntityException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("userDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User create(User user) {
        log.info("Добавление нового пользователя {} в БД.", user);
        try {
            String queryUsersInsert = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?);";

            jdbcTemplate.update(queryUsersInsert,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday());

            String queryUsersSelect = "SELECT user_id FROM users WHERE email = ?;";
            Long userId = jdbcTemplate.queryForObject(queryUsersSelect, (rs, rowNum) -> mapRowToUserId(rs), user.getEmail());
            user.setId(userId);

            return user;
        } catch (RuntimeException e) {
            throw new NotUniqueEntityException("Пользователь с email = " + user.getEmail() + " уже есть в БД");
        }
    }

    @Override
    public User update(User user) {
        log.info("Изменение данных в БД у пользователя с id = {}.", user.getId());
        String queryUsersUpdate = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?;";

        try {
            jdbcTemplate.update(queryUsersUpdate, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
            return user;
        } catch (RuntimeException e) {
            throw new ValidationException("Невозможно поменять email на " + user.getEmail() + " - пользователь с таким email уже существует.");
        }
    }

    @Override
    public List<User> get() {
        log.info("Чтение всех пользователей из БД.");
        String sql = "SELECT * FROM users;";

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToUser(rs));
    }

    @Override
    public User get(Long id) {
        log.info("Чтение пользователя с id = " + id + " из БД.");
        String sql = "SELECT * FROM users WHERE user_id = ?;";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToUser(rs), id);
        } catch (Exception e) {
            throw new NoSuitableUnitException("В БД отсутствует запрошенный пользователь");
        }
    }

    @Override
    public Map<Long, User> getValues() {
        log.info("Создание копии всех пользователей из БД.");
        String sql = "SELECT * FROM users;";

        Map<Long, User> userMap = new HashMap<>();
        List<User> userList = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToUser(rs));
        for (User user : userList) {
            userMap.put(user.getId(), user);
        }
        return userMap;
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("user_id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getDate("birthday").toLocalDate(),
                new HashSet<>(), // эту строку впоследствии нужно заменить на правильную.
                new HashSet<>()); // эту строку впоследствии нужно заменить на правильную.
    }

    private Long mapRowToUserId(ResultSet rs) throws SQLException {
        return rs.getLong("user_id");
    }
}
