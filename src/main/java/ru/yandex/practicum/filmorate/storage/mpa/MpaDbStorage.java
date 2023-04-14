package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Slf4j
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Mpa> get() {
        log.info("Получение всех рейтингов из БД.");
        String queryMpaSelect = "SELECT * " +
                "FROM mpa " +
                "ORDER BY mpa_id;";

        return jdbcTemplate.query(queryMpaSelect, (rs, rowNum) -> mapRowToMpa(rs));
    }

    @Override
    public Mpa get(Long id) {
        log.info("Получение рейтинга с id = {} из БД.", id);
        String queryMpaSelect = "SELECT * " +
                "FROM mpa " +
                "WHERE mpa_id = ?;";

        return jdbcTemplate.queryForObject(queryMpaSelect, (rs, rowNum) -> mapRowToMpa(rs), id);
    }

    private Mpa mapRowToMpa(ResultSet rs) throws SQLException {
        return new Mpa(
                rs.getInt("mpa_id"),
                rs.getString("mpa"));
    }
}
