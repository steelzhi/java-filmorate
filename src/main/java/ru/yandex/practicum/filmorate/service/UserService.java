package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NoSuitableUnitException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private Map<Long, User> users;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public UserStorage getUserStorage() {
        return userStorage;
    }

    public Set<Long> addFriend(Long id, Long friendId) {
        log.info("Добавление в список друзей пользователя с id = {} друга с id = {}", id, friendId);
        if (!doUsersExist(id, friendId)) {
            throw new NoSuitableUnitException(
                    "Пользователь(-ли) с введенным(-ми) id отсутствует(-ют) в списке пользователей.");
        }

        User user = users.get(id);
        User friendOfUser = users.get(friendId);
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

        User user = users.get(id);
        User friendOfUser = users.get(friendId);
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
        Set<Long> friendsIds = users.get(id).getFriendsIds();
        for (Long friendId : friendsIds) {
            if (users.containsKey(friendId)) {
                friends.add(users.get(friendId));
            }
        }

        return friends;
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        log.info("Получение списка общих друзей пользователей с id = {} и id = {}", id, otherId);
        if (!doUsersExist(id, otherId)) {
            throw new NoSuitableUnitException(
                    "Пользователь(-ли) с введенным(-ми) id отсутствует(-ют) в списке пользователей.");
        }

        Set<Long> friendsIdsOfFirstUser = users.get(id).getFriendsIds();
        Set<Long> friendsIdsOfSecondUser = users.get(otherId).getFriendsIds();
        if (friendsIdsOfFirstUser.isEmpty() || friendsIdsOfSecondUser.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> commonFriendsIds = new HashSet<>();
        commonFriendsIds.addAll(friendsIdsOfFirstUser);
        commonFriendsIds.retainAll(friendsIdsOfSecondUser);
        return getUsersByIds(commonFriendsIds);
    }

    private boolean doUsersExist(Long... receivedUsersIds) {
        users = userStorage.getValues();
        for (Long id : receivedUsersIds) {
            if (!users.containsKey(id)) {
                return false;
            }
        }
        return true;
    }

    private List<User> getUsersByIds(Set<Long> ids) {
        List<User> userList = new ArrayList<>();
        for (Long id : ids) {
            if (users.containsKey(id)) {
                userList.add(users.get(id));
            }
        }
        return userList;
    }
}
