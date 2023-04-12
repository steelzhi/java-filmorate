package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private List<Genres> genres;
    @NotNull
    private Mpa mpa;
    @NotNull
    private LocalDate releaseDate;
    @NotNull
    @Positive
    private Integer duration;
    @JsonIgnore
    private Set<Long> userLikes = new HashSet<>();

    public Set<Long> getUserLikes() {
        return userLikes;
    }

    public void addUserLike(Long userId) {
        userLikes.add(userId);
    }

    public void deleteUserLike(Long userId) {
        userLikes.remove(userId);
    }

    public int getAllLikesCount() {
        return userLikes.size();
    }
}
