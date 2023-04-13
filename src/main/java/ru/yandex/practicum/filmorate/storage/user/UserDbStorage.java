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
import java.util.*;

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
                    getUserWithNonEmptyName(user).getName(),
                    user.getBirthday());

            String queryUsersSelect = "SELECT user_id FROM users WHERE email = ?;";
            Long userId = jdbcTemplate.queryForObject(queryUsersSelect, (rs, rowNum) -> mapRowToId(rs, "user_id"), user.getEmail());
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

        List<User> rawUsers = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToUser(rs));
        List<User> users = new ArrayList<>();
        for (User user : rawUsers) {
            users.add(getUserWithAllFieldsFilled(user));
        }

        return users;
    }

    @Override
    public User get(Long id) {
        log.info("Чтение пользователя с id = " + id + " из БД.");
        String sql = "SELECT * FROM users WHERE user_id = ?;";
        try {
            User rawUser = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToUser(rs), id);
            return getUserWithAllFieldsFilled(rawUser);
        } catch (Exception e) {
            throw new NoSuitableUnitException("В БД отсутствует запрошенный пользователь");
        }
    }

    @Override
    public Map<Long, User> getValues() {
        log.info("Выгрузка всех пользователей из БД.");
        String queryUsersSelect = "SELECT * FROM users;";

        Map<Long, User> userMap = new HashMap<>();
        List<User> userList = jdbcTemplate.query(queryUsersSelect, (rs, rowNum) -> mapRowToUser(rs));
        for (User user : userList) {
            userMap.put(user.getId(), user);
        }
        return userMap;
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getLong("user_id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getDate("birthday").toLocalDate(),
                new HashSet<>(),
                new HashSet<>());

        return getUserWithAllFieldsFilled(user);
    }

    private User getUserWithAllFieldsFilled(User user) {
        Long userId = user.getId();
        String firstQueryFriendShipSelect = "SELECT friend_two_id FROM friendship WHERE friend_one_id = ? AND friendship_status = true;";
        String secondQueryFriendShipSelect = "SELECT friend_one_id FROM friendship WHERE friend_two_id = ?;";

        List<Long> firstListOfFriends = jdbcTemplate.query(firstQueryFriendShipSelect, (rs, rowNum) -> mapRowToId(rs, "friend_two_id"), userId);
        List<Long> secondListOfFriends = jdbcTemplate.query(secondQueryFriendShipSelect, (rs, rowNum) -> mapRowToId(rs, "friend_one_id"), userId);

        Set<Long> allFriends = new HashSet<>();
        allFriends.addAll(firstListOfFriends);
        allFriends.addAll(secondListOfFriends);

        String queryUserLikesSelect = "SELECT film_id FROM user_likes WHERE user_id = ?;";
        List<Long> filmLikes = jdbcTemplate.query(queryUserLikesSelect, (rs, rowNum) -> mapRowToId(rs, "film_id"), userId);

        Set<Long> allLikedFilms = new HashSet<>();
        allLikedFilms.addAll(filmLikes);

        user.setFriendsIds(allFriends);
        user.setLikedFilmsIds(allLikedFilms);

        return user;
    }

    private Long mapRowToId(ResultSet rs, String column) throws SQLException {
        return rs.getLong(column);
    }

    private User getUserWithNonEmptyName(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        return user;
    }
}
