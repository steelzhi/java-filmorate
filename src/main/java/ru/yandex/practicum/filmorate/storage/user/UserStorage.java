package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.Storage;

import java.util.Set;

public interface UserStorage extends Storage<User> {
    Set<Long> addFriend(Long id, Long friendId);

    Set<Long> deleteFriend(Long id, Long friendId);

    boolean doUsersExist(Long... receivedUsersIds);
}