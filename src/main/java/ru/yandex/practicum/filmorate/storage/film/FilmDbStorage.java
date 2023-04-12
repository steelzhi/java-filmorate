package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
        checkGenreAndRating(film);
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

        String queryFilmsSelect = "SELECT film_id FROM films WHERE name = ? AND description = ? AND mpa_id = ? " +
                "AND release_date = ? AND duration = ?;";

        Long filmId = jdbcTemplate.queryForObject(queryFilmsSelect, (rs, rowNum) -> mapRowToFilmId(rs),
                film.getName(),
                film.getDescription(),
                film.getMpa().getId(),
                film.getReleaseDate(),
                film.getDuration());

        String queryFilmGenresInsert = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        for (Genres genre : film.getGenres()) {
            jdbcTemplate.update(queryFilmGenresInsert, filmId, genre.getId());
        }

        film.setId(filmId);
        return film;
    }

    @Override
    public Film update(Film film) {
        checkGenreAndRating(film);
        log.info("Изменение в БД фильма с id = {}.", film.getId());

        try {
            String queryFilmsUpdate = "UPDATE films SET name = ?, description = ?, mpa_id = ?, release_date = ?, " +
                    "duration = ? WHERE film_id = ?;";
            jdbcTemplate.update(queryFilmsUpdate,
                    film.getName(),
                    film.getDescription(),
                    film.getMpa().getId(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getId());

            String queryFilmsGenresDelete = "DELETE FROM film_genres WHERE film_id = ?;";
            jdbcTemplate.update(queryFilmsGenresDelete, film.getId());
            String queryFilmsGenresInsert = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genres genre : film.getGenres()) {
                jdbcTemplate.update(queryFilmsGenresInsert, film.getId(), genre.getId());
            }
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

        List<Film> rawFilmList = jdbcTemplate.query(queryFilmsSelect, (rs, rowNum) -> mapRowToFilm(rs));

        if (rawFilmList.isEmpty()) {
            return new ArrayList<>();
        }

        Long currentFilmId = rawFilmList.get(0).getId();
        List<Genres> genresOfCurrentFilm = new ArrayList<>();
        List<Film> films = new ArrayList<>();
        Film lastFilmInRawFilmList = null;

        for (Film film : rawFilmList) {
            if (film.getId() == currentFilmId) {
                genresOfCurrentFilm.addAll(film.getGenres());
                lastFilmInRawFilmList = film;
            } else {
                films.add(film);
                film.setGenres(genresOfCurrentFilm);
                genresOfCurrentFilm.clear();
                currentFilmId = film.getId();
            }
        }
        films.add(lastFilmInRawFilmList);
        lastFilmInRawFilmList.setGenres(genresOfCurrentFilm);

        return films;
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
            List<Film> rawFilmList = jdbcTemplate.query(queryFilmsSelect, (rs, rowNum) -> mapRowToFilm(rs), id);
            List<Genres> allGenresOfFilm = new ArrayList<>();
            for (Film film : rawFilmList) {
                allGenresOfFilm.addAll(film.getGenres());
            }

            Film f = rawFilmList.get(0);
            return new Film(f.getId(), f.getName(), f.getDescription(), allGenresOfFilm, f.getMpa(), f.getReleaseDate(), f.getDuration(), new HashSet<>());

        } catch (RuntimeException e) {
            throw new NoSuitableUnitException("Фильма с таким id нет в БД");
        }
    }

    // Убрать этот метод из этого класса и из интерфейса Storage?
    @Override
    public Map<Long, Film> getValues() {
        return null;
    }

    private Film mapRowToFilm(ResultSet rs) throws SQLException {
        Long id = rs.getLong("film_id");
        String name = rs.getString("name");
        String description = rs.getString("description");

        Integer genreId = rs.getInt("genre_id");
        String genreName = rs.getString("genre");
        Genres genre = new Genres(genreId, genreName);

        Integer mpaId = rs.getInt("mpa_id");
        String mpaName = rs.getString("mpa");
        Mpa mpa = new Mpa(mpaId, mpaName);

        LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
        Integer duration = rs.getInt("duration");

        return new Film(id, name, description, List.of(genre), mpa, releaseDate, duration, new HashSet<>());
    }

    private void checkGenreAndRating(Film film) {
        String sqlGenre = "SELECT genre_id FROM genres;";
        String sqlMpa = "SELECT mpa_id FROM mpa;";

        List<Integer> genresIds = jdbcTemplate.query(sqlGenre, (rs, rowNum) -> mapRowToGenreId(rs));
        List<Integer> mpaIds = jdbcTemplate.query(sqlMpa, (rs, rowNum) -> mapRowToMpaId(rs));

        for (Genres genres : film.getGenres())
            if (!genresIds.contains(genres.getId())) {
                throw new ValidationException("Неверно введен жанр фильма");
            }
        if (!mpaIds.contains(film.getMpa().getId())) {
            throw new ValidationException("Неверно введен рейтинг фильма");
        }
    }

    private Integer mapRowToGenreId(ResultSet rs) throws SQLException {
        return rs.getInt("genre_id");
    }

    private Integer mapRowToMpaId(ResultSet rs) throws SQLException {
        return rs.getInt("mpa_id");
    }

    private Long mapRowToFilmId(ResultSet rs) throws SQLException {
        return rs.getLong("film_id");
    }
}
