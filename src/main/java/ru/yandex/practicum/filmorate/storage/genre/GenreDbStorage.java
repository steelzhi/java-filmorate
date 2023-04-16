package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NoSuitableUnitException;
import ru.yandex.practicum.filmorate.model.Genres;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Genres create(Genres genres) {
        log.info("Создание нового жанра.");
        String queryMpaInsert = "INSERT INTO genres VALUES (?, ?);";
        jdbcTemplate.update(queryMpaInsert, genres.getId(), genres.getName());

        return genres;
    }

    @Override
    public Genres update(Genres genres) {
        log.info("Изменение жанра с id = {}.", genres.getId());
        String queryGenresSelect = "SELECT * FROM genres WHERE genre_id = ?;";

        Genres existingGenres = jdbcTemplate.queryForObject(queryGenresSelect,
                (rs, rowNum) -> mapRowToGenre(rs), genres.getId());
        if (existingGenres == null) {
            throw new NoSuitableUnitException("Такого рейтинга нет в БД.");
        }

        String queryGenresUpdate = "UPDATE genres SET genre = ? WHERE genre_id = ?";
        jdbcTemplate.update(queryGenresUpdate, genres.getName(), genres.getId());

        return genres;
    }

    @Override
    public List<Genres> get() {
        log.info("Получение из БД всех жанров.");
        String sql = "SELECT * " +
                "FROM genres " +
                "ORDER BY genre_id;";

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToGenre(rs));
    }

    @Override
    public Genres get(Long id) {
        log.info("Получение из БД жанра c id = {}.", id);
        String sql = "SELECT * " +
                "FROM genres " +
                "WHERE genre_id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToGenre(rs), id);

        } catch (RuntimeException e) {
            throw new NoSuitableUnitException("Жанра с таким id нет в БД");
        }
    }

    @Override
    public Map<Long, Genres> getValues() {
        String queryGenresSelect = "SELECT * FROM genres";
        List<Genres> genresList = jdbcTemplate.query(queryGenresSelect, (rs, rowNum) -> mapRowToGenre(rs));
        Map<Long, Genres> genresMap = new HashMap<>();
        for (Genres genre : genresList) {
            genresMap.put((long) genre.getId(), genre);
        }
        return genresMap;
    }

    private Genres mapRowToGenre(ResultSet rs) throws SQLException {
        return new Genres(
                rs.getInt("genre_id"),
                rs.getString("genre"));
    }

    /**
     * Метод, необходимый для тестирования
     */
    public void deleteLastGenres() {
        String queryGenresSelect = "SELECT * FROM genres;";
        List<Genres> allGenres = jdbcTemplate.query(queryGenresSelect, (rs, rowNum) -> mapRowToGenre(rs));

        Integer lastGenresId = allGenres.get(allGenres.size() - 1).getId();

        String queryGenresDelete = "DELETE FROM genres WHERE genre_id = ?";
        jdbcTemplate.update(queryGenresDelete, lastGenresId);
    }
}