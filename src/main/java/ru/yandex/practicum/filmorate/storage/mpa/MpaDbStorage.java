package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NoSuitableUnitException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Mpa create(Mpa mpa) {
        log.info("Создание нового рейтинга.");
        String queryMpaInsert = "INSERT INTO mpa VALUES (?, ?);";
        jdbcTemplate.update(queryMpaInsert, mpa.getId(), mpa.getName());

        return mpa;
    }

    @Override
    public Mpa update(Mpa mpa) {
        log.info("Изменение рейтинга с id = {}.", mpa.getId());
        String queryMpaSelect = "SELECT * FROM mpa WHERE mpa_id = ?;";

        Mpa existingMpa = jdbcTemplate.queryForObject(queryMpaSelect, (rs, rowNum) -> mapRowToMpa(rs), mpa.getId());
        if (existingMpa == null) {
            throw new NoSuitableUnitException("Такого рейтинга нет в БД.");
        }

        String queryMpaUpdate = "UPDATE mpa SET mpa = ? WHERE mpa_id = ?";
        jdbcTemplate.update(queryMpaUpdate, mpa.getName(), mpa.getId());

        return mpa;
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

    @Override
    public Map<Long, Mpa> getValues() {
        String queryMpaSelect = "SELECT * FROM mpa";
        List<Mpa> mpaList = jdbcTemplate.query(queryMpaSelect, (rs, rowNum) -> mapRowToMpa(rs));
        Map<Long, Mpa> mpaMap = new HashMap<>();
        for (Mpa mpa : mpaList) {
            mpaMap.put((long) mpa.getId(), mpa);
        }
        return mpaMap;
    }

    /**
     * Метод, необходимый для тестирования
     */
    public void deleteLastMpa() {
        String queryMpaSelect = "SELECT * FROM mpa;";
        List<Mpa> allMpa = jdbcTemplate.query(queryMpaSelect, (rs, rowNum) -> mapRowToMpa(rs));

        Integer lastMpaId = allMpa.get(allMpa.size() - 1).getId();

        String queryMpaDelete = "DELETE FROM mpa WHERE mpa_id = ?";
        jdbcTemplate.update(queryMpaDelete, lastMpaId);
    }
}