package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Film {
    private Long id;
    @NotEmpty
    private String name;
    @NotEmpty
    private String description;
    @NotNull
    private LocalDate releaseDate;
    @NotNull
    @Positive
    private Integer duration;
    private List<Long> userLikes = new ArrayList<>();

    public List<Long> getUserLikes() {
        return userLikes;
    }

    public void addUserLike(Long userId) {
        if (!userLikes.contains(userId)) {
            userLikes.add(userId);
        }
    }

    public void deleteUserLike(Long userId) {
        userLikes.remove(userId);
    }

    public int getAllLikesCount() {
        return userLikes.size();
    }
}
