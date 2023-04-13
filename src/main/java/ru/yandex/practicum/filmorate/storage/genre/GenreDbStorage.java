package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NoSuitableUnitException;
import ru.yandex.practicum.filmorate.model.Genres;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Slf4j
public class GenreDbStorage implements GenreStorage{
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genres> get() {
        log.info("Получение из БД всех жанров.");
        String sql = "SELECT * " +
                "FROM genres";

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

    private Genres mapRowToGenre(ResultSet rs) throws SQLException {
        return new Genres(
                rs.getInt("genre_id"),
                rs.getString("genre"));
    }
}
