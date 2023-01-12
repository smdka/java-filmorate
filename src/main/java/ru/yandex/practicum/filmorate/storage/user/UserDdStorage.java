package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Repository
public class UserDdStorage implements UserStorage {
    private JdbcTemplate jdbcTemplate;

    public UserDdStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM USERS ", this::mapRowToUser);
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        int id = rs.getInt("ID");
        String email = rs.getString("EMAIL");
        String login = rs.getString("LOGIN");
        String name = rs.getString("NAME");
        LocalDate birthday = rs.getDate("BIRTHDAY").toLocalDate();
        Set<Integer> friendsIds = getFriendsIdsByUserId(id);
        return new User(id, friendsIds, email, login, name, birthday);
    }

    private Set<Integer> getFriendsIdsByUserId(int id) {
        String sql = "SELECT FRIEND_ID FROM USER_FRIENDS WHERE USER_ID = ?";
        return new HashSet<>(jdbcTemplate.query(sql, ((rs, rowNum) -> rs.getInt("FRIEND_ID")), id));
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[] {"ID"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        int userId = keyHolder.getKey().intValue();
        user.setId(userId);

        return user;
    }

    @Override
    public Optional<User> update(User user) {
        String sql = "UPDATE USERS SET " +
                     "EMAIL = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ? " +
                     "WHERE ID = ?";
        if (jdbcTemplate.update(sql, user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()) == 0) {
            return Optional.empty();
        }

        saveFriends(user);
        return Optional.of(user);
    }

    private void saveFriends(User user) {
        String sql = "DELETE FROM USER_FRIENDS WHERE USER_ID = ?";
        jdbcTemplate.update(sql, user.getId());

        Set<Integer> friendsIds = user.getFriendIds();
        int userId = user.getId();

        sql = "INSERT INTO USER_FRIENDS (USER_ID, FRIEND_ID) VALUES (?, ?)";
        for (int friendId : friendsIds) {
            jdbcTemplate.update(sql, userId, friendId);
        }
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM USERS " +
                     "WHERE ID = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT USERS.ID, " +
                            "USERS.EMAIL, " +
                            "USERS.LOGIN, " +
                            "USERS.NAME, " +
                            "USERS.BIRTHDAY " +
                     "FROM USERS " +
                     "WHERE USERS.ID = ?";
        List<User> results = jdbcTemplate.query(sql, this::mapRowToUser, id);
        return results.isEmpty() ?
                Optional.empty() :
                Optional.of(results.get(0));
    }

    @Override
    public Collection<User> findFriendsById(int id) {
        try {
            String sql = "SELECT U.* FROM USER_FRIENDS UF " +
                    "LEFT JOIN USERS U on U.ID = UF.FRIEND_ID " +
                    "WHERE USER_ID = ? ";
            return jdbcTemplate.query(sql, this::mapRowToUser, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
