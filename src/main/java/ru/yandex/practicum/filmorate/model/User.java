package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class User {
    private Integer id;
    @NonNull
    private final String email;
    @NonNull
    private final String login;
    private String name;
    @NonNull
    private final LocalDate birthday;
}
