package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.h2.jdbc.JdbcSQLSyntaxErrorException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NoSuitableUnitException;
import ru.yandex.practicum.filmorate.exception.NotUniqueEntityException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genres;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film create(Film film) {
        log.info("Добавление нового фильма {} в БД.", film);
        checkRating(film);
        try {
            String queryFilmsInsert = "INSERT INTO films (name, description, mpa_id, release_date, duration) " +
                    "VALUES (?, ?, ?, ?, ?);";
            jdbcTemplate.update(queryFilmsInsert,
                    film.getName(),
                    film.getDescription(),
                    film.getMpa().getId(),
                    film.getReleaseDate(),
                    film.getDuration());
        } catch (RuntimeException e) {
            throw new NotUniqueEntityException("Фильм с такими данными уже есть в БД");
        }

        String queryFilmsSelect = "SELECT film_id " +
                "FROM films " +
                "WHERE name = ? AND description = ? AND mpa_id = ? AND release_date = ? AND duration = ?;";

        Long filmId = jdbcTemplate.queryForObject(queryFilmsSelect,
                (rs, rowNum) -> mapRowToIdLong(rs, "film_id"),
                film.getName(),
                film.getDescription(),
                film.getMpa().getId(),
                film.getReleaseDate(),
                film.getDuration());

        film.setId(filmId);
        updateGenresAndLeaveOnlyUnique(film);

        return film;
    }

    @Override
    public Film update(Film film) {
        checkRating(film);
        if (!areGenresCorrect(film)) {
            throw new ValidationException("Неверно введен жанр фильма");
        }
        log.info("Изменение в БД фильма с id = {}.", film.getId());

        try {
            String queryFilmsUpdate = "UPDATE films " +
                    "SET name = ?, description = ?, mpa_id = ?, release_date = ?, duration = ? WHERE film_id = ?;";
            jdbcTemplate.update(queryFilmsUpdate,
                    film.getName(),
                    film.getDescription(),
                    film.getMpa().getId(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getId());

            String queryFilmsGenresDelete = "DELETE FROM film_genres WHERE film_id = ?;";
            jdbcTemplate.update(queryFilmsGenresDelete, film.getId());
            updateGenresAndLeaveOnlyUnique(film);
            return film;

        } catch (RuntimeException e) {
            throw new NoSuitableUnitException("Фильма с таким id нет в БД");
        }
    }

    @Override
    public List<Film> get() {
        log.info("Получение из БД всех фильмов.");
        String queryFilmsSelect = "SELECT * " +
                "FROM films AS f " +
                "LEFT JOIN film_genres AS fg ON f.film_id=fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.genre_id " +
                "LEFT JOIN mpa AS m ON f.mpa_id=m.mpa_id;";

        List<Film> filmsWithoutGenresLikes = jdbcTemplate.query(queryFilmsSelect, (rs, rowNum) -> mapRowToFilm(rs));

        if (filmsWithoutGenresLikes.isEmpty()) {
            return new ArrayList<>();
        }

        List<Film> filmsWithoutLikes = getFilmsWithGenres(filmsWithoutGenresLikes);
        List<Film> filmsWithLikes = new ArrayList<>();
        for (Film film : filmsWithoutLikes) {
            filmsWithLikes.add(getFilmWithLikes(film));
        }

        Collections.sort(filmsWithLikes, (o1, o2) -> (int) (o1.getId() - o2.getId()));

        return filmsWithLikes;
    }

    @Override
    public Film get(Long id) {
        log.info("Получение из БД фильма с id = " + id + " .");
        String queryFilmsSelect = "SELECT * " +
                "FROM films AS f " +
                "LEFT JOIN film_genres AS fg ON f.film_id=fg.film_id " +
                "LEFT JOIN genres AS g ON fg.genre_id = g.genre_id " +
                "LEFT JOIN mpa AS m ON f.mpa_id=m.mpa_id " +
                "WHERE f.film_id = ?;";

        try {
            List<Film> filmsWithoutLikes = jdbcTemplate.query(queryFilmsSelect, (rs, rowNum) -> mapRowToFilm(rs), id);
            List<Genres> allGenresOfFilm = new ArrayList<>();
            for (Film film : filmsWithoutLikes) {
                if (film.getGenres() != null) {
                    allGenresOfFilm.addAll(film.getGenres());
                }
            }

            Film filmWithoutLikes = filmsWithoutLikes.get(0);
            Film film = new Film(
                    filmWithoutLikes.getId(),
                    filmWithoutLikes.getName(),
                    filmWithoutLikes.getDescription(),
                    allGenresOfFilm,
                    filmWithoutLikes.getMpa(),
                    filmWithoutLikes.getReleaseDate(),
                    filmWithoutLikes.getDuration(),
                    new HashSet<>());

            return getFilmWithLikes(film);
        } catch (RuntimeException e) {
            throw new NoSuitableUnitException("Фильма с таким id нет в БД");
        }
    }

    @Override
    public Map<Long, Film> getValues() {
        Map<Long, Film> filmMap = new HashMap<>();
        List<Film> films = get();
        for (Film film : films) {
            filmMap.put(film.getId(), getFilmWithLikes(film));
        }
        return filmMap;
    }

    @Override
    public Film putLike(Long id, Long userId) {
        String queryUserLikesSelect = "SELECT * FROM user_likes WHERE film_id = ?;";

        List<Long> filmLikes = jdbcTemplate.query(queryUserLikesSelect,
                (rs, rowNum) -> mapRowToIdLong(rs, "user_id"), id);
        if (!filmLikes.contains(userId)) {
            String queryUserLikesInsert = "INSERT INTO user_likes (film_id, user_id) VALUES (?, ?);";

            jdbcTemplate.update(queryUserLikesInsert, id, userId);
        }

        Film film = get(id);
        return film;
    }

    @Override
    public Film deleteLike(Long id, Long userId) {
        String queryUserLikesSelect = "SELECT * FROM user_likes WHERE film_id = ?;";

        List<Long> filmLikes = jdbcTemplate.query(queryUserLikesSelect,
                (rs, rowNum) -> mapRowToIdLong(rs, "user_id"), id);
        if (filmLikes.contains(userId)) {
            String queryUserLikesDelete = "DELETE FROM user_likes WHERE user_id = ?";

            jdbcTemplate.update(queryUserLikesDelete, userId);
        }

        Film film = get(id);
        return film;
    }

    private void checkRating(Film film) {
        String sqlMpa = "SELECT mpa_id " +
                "FROM mpa;";

        List<Integer> mpaIds = jdbcTemplate.query(sqlMpa, (rs, rowNum) -> mapRowToIdInteger(rs, "mpa_id"));

        if (!mpaIds.contains(film.getMpa().getId())) {
            throw new ValidationException("Неверно введен рейтинг фильма");
        }
    }

    private boolean areGenresCorrect(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sqlGenre = "SELECT genre_id " +
                    "FROM genres;";

            List<Integer> genresIds = jdbcTemplate.query(sqlGenre,
                    (rs, rowNum) -> mapRowToIdInteger(rs, "genre_id"));

            for (Genres genres : film.getGenres())
                if (!genresIds.contains(genres.getId())) {
                    return false;
                }
            return true;
        }
        return true;
    }

    private Film mapRowToFilm(ResultSet rs) throws SQLException {
        Long id = rs.getLong("film_id");
        String name = rs.getString("name");
        String description = rs.getString("description");

        List<Genres> genresList = null;
        Mpa mpa;

        try {
            Integer genreId = rs.getInt("genre_id");
            String genreName = rs.getString("genre");
            if (genreName != null) {
                Genres genre = new Genres(genreId, genreName);
                genresList = List.of(genre);
            }
        } catch (JdbcSQLSyntaxErrorException e) {
            genresList = null;
        }

        try {
            Integer mpaId = rs.getInt("mpa_id");
            String mpaName = rs.getString("mpa");
            mpa = new Mpa(mpaId, mpaName);
        } catch (JdbcSQLSyntaxErrorException e) {
            mpa = null;
        }

        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        Integer duration = rs.getInt("duration");

        return new Film(id, name, description, genresList, mpa, releaseDate, duration, new HashSet<>());
    }

    private Long mapRowToIdLong(ResultSet rs, String column) throws SQLException {
        return rs.getLong(column);
    }

    private Integer mapRowToIdInteger(ResultSet rs, String column) throws SQLException {
        return rs.getInt(column);
    }

    private Genres mapRowToGenres(ResultSet rs) throws SQLException {
        return new Genres(rs.getInt("genre_id"), rs.getString("genre"));
    }

    private int getGenresId(List<Genres> genres) {
        return genres.get(0).getId();
    }

    private Film getFilmWithLikes(Film film) {
        Long filmId = film.getId();

        String queryUserLikesSelect = "SELECT user_id " +
                "FROM user_likes " +
                "WHERE film_id = ?;";
        List<Long> userLikes = jdbcTemplate.query(queryUserLikesSelect,
                (rs, rowNum) -> mapRowToIdLong(rs, "user_id"), filmId);

        Set<Long> allUserLikes = new HashSet<>();
        allUserLikes.addAll(userLikes);
        film.setUserLikes(allUserLikes);

        return film;
    }

    private List<Film> getFilmsWithGenres(List<Film> filmsWithoutGenres) {
        Map<Long, Set<Long>> genresOfFilms = new HashMap<>();
        for (Film film : filmsWithoutGenres) {
            genresOfFilms.put(film.getId(), new HashSet<>());
        }
        for (Film film : filmsWithoutGenres) {
            Set<Long> currentGenresIds = genresOfFilms.get(film.getId());
            if (film.getGenres() != null) {
                currentGenresIds.add((long) getGenresId(film.getGenres()));
                genresOfFilms.put(film.getId(), currentGenresIds);
            }
        }

        String queryGenresSelect = "SELECT * FROM genres;";
        List<Genres> allGenres = jdbcTemplate.query(queryGenresSelect, (rs, rowNum) -> mapRowToGenres(rs));

        List<Film> filmsWithGenres = new ArrayList<>();
        Long currentFilmId = null;
        for (Film film : filmsWithoutGenres) {
            if (film.getId() != currentFilmId) {
                filmsWithGenres.add(getFilmWithGenres(film, genresOfFilms.get(film.getId()), allGenres));
                currentFilmId = film.getId();
            }
        }

        return filmsWithGenres;
    }

    private Film getFilmWithGenres(Film film, Set<Long> genreIds, List<Genres> allGenres) {
        List<Genres> genresOfCurrentFilm = new ArrayList<>();

        if (film.getGenres() != null) {
            for (Long genreId : genreIds) {
                for (Genres genres : allGenres) {
                    if (genres.getId() == genreId) {
                        genresOfCurrentFilm.add(genres);
                    }
                }
            }
        }
        film.setGenres(genresOfCurrentFilm);

        return film;
    }

    private void updateGenresAndLeaveOnlyUnique(Film film) {
        if (film.getGenres() != null) {
            Set<Integer> genresIdsSet = new TreeSet<>();
            for (Genres genres : film.getGenres()) {
                genresIdsSet.add(genres.getId());
            }

            Set<Genres> genresSet = new HashSet<>();
            for (Genres genre : film.getGenres()) {
                for (Integer genreId : genresIdsSet) {
                    if (genre.getId() == genreId) {
                        genresSet.add(genre);
                    }
                }
            }
            List<Genres> genres = new ArrayList<>(genresSet);

            Collections.sort(genres, (o1, o2) -> {
                if (o1.getId() <= o2.getId()) {
                    return -1;
                } else {
                    return 1;
                }
            });

            String queryFilmsGenresInsert = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?);";
            for (Genres genre : genres) {
                jdbcTemplate.update(queryFilmsGenresInsert, film.getId(), genre.getId());
            }
            film.setGenres(genres);
        }
    }

    @Override
    public boolean doesFilmExist(Long filmId) {
        String queryFilmsSelect = "SELECT * " +
                "FROM films " +
                "WHERE film_id = ?;";
        try {
            jdbcTemplate.queryForObject(queryFilmsSelect, (rs, rowNum) -> mapRowToFilm(rs), filmId);
        } catch (EmptyResultDataAccessException e) {
            return false;
        }

        return true;
    }

    /**
     * Метод, необходимый для проведения тестов
     */

    public void deleteAllFilms() {
        String queryFilmGenresDelete = "DELETE FROM film_genres;";
        jdbcTemplate.update(queryFilmGenresDelete);

        String queryUserLikesDelete = "DELETE FROM user_likes;";
        jdbcTemplate.update(queryUserLikesDelete);

        String queryFilmsDelete = "DELETE FROM films;";
        jdbcTemplate.update(queryFilmsDelete);
    }
}