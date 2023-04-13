package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NoSuitableUnitException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        checkUserParams(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        checkUserParams(user);
        return userStorage.update(user);
    }

    public List<User> get() {
        return userStorage.get();
    }

    public User get(Long id) {
        return userStorage.get(id);
    }

    public Set<Long> addFriend(Long id, Long friendId) {
        log.info("Добавление в список друзей пользователя с id = {} друга с id = {}", id, friendId);
        if (!doUsersExist(id, friendId)) {
            throw new NoSuitableUnitException(
                    "Пользователь(-ли) с введенным(-ми) id отсутствует(-ют) в списке пользователей.");
        }

        User user = getUsers().get(id);
        User friendOfUser = getUsers().get(friendId);
        user.addFriend(friendId);
        friendOfUser.addFriend(id);
        return user.getFriendsIds();
    }

    public Set<Long> deleteFriend(Long id, Long friendId) {
        log.info("Удаление у пользователя с id = {} из списка друзей друга с id = {}", id, friendId);
        if (!doUsersExist(id, friendId)) {
            throw new NoSuitableUnitException(
                    "Пользователь(-ли) с введенным(-ми) id отсутствует(-ют) в списке пользователей.");
        }

        User user = getUsers().get(id);
        User friendOfUser = getUsers().get(friendId);
        user.deleteFriend(friendId);
        friendOfUser.deleteFriend(id);
        return user.getFriendsIds();
    }

    public List<User> getFriends(Long id) {
        log.info("Возвращение списка друзей пользователя с id = {}", id);
        if (!doUsersExist(id)) {
            throw new NoSuitableUnitException(
                    "Пользователь с введенным id отсутствует в списке пользователей.");
        }

        List<User> friends = new ArrayList<>();
        Set<Long> friendsIds = getUsers().get(id).getFriendsIds();
        friendsIds.stream()
                .forEach(o1 -> friends.add(getUsers().get(o1)));
        return friends;
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        log.info("Получение списка общих друзей пользователей с id = {} и id = {}", id, otherId);
        if (!doUsersExist(id, otherId)) {
            throw new NoSuitableUnitException(
                    "Пользователь(-ли) с введенным(-ми) id отсутствует(-ют) в списке пользователей.");
        }

        Set<Long> friendsIdsOfFirstUser = getUsers().get(id).getFriendsIds();
        Set<Long> friendsIdsOfSecondUser = getUsers().get(otherId).getFriendsIds();
        if (friendsIdsOfFirstUser.isEmpty() || friendsIdsOfSecondUser.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> commonFriendsIds = new HashSet<>();
        commonFriendsIds.addAll(friendsIdsOfFirstUser);
        commonFriendsIds.retainAll(friendsIdsOfSecondUser);
        return getUsersByIds(commonFriendsIds);
    }


    private void checkUserParams(User user) {
        if (user.getLogin().contains(" ")) {
            log.info("Попытка добавить пользователя с пробелом в логине.");
            throw new ValidationException("Введены некорректный логин пользователя!");
        }
    }

    private boolean doUsersExist(Long... receivedUsersIds) {
        for (Long id : receivedUsersIds) {
            if (!getUsers().containsKey(id)) {
                return false;
            }
        }
        return true;
    }

    private List<User> getUsersByIds(Set<Long> ids) {
        List<User> userList = new ArrayList<>();
        ids.stream()
                .forEach(o1 -> userList.add(getUsers().get(o1)));
        return userList;
    }

    private Map<Long, User> getUsers() {
        return userStorage.getValues();
    }
}