package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import static java.util.stream.Collectors.toSet;

@Repository
@RequiredArgsConstructor
public class UserDdStorage implements UserStorage {
    private static final String FIND_ALL =
            "SELECT USERS.*, " +
                   "ARRAY_AGG(UF.FRIEND_ID) AS FRIENDS_IDS " +
            "FROM USERS " +
            "LEFT JOIN USER_FRIENDS UF on USERS.ID = UF.USER_ID ";
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<User> findAll() {
        String sql = FIND_ALL +
                    "GROUP BY USERS.ID";
        return jdbcTemplate.query(sql, this::mapRowToUser);
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        int id = rs.getInt("ID");
        String email = rs.getString("EMAIL");
        String login = rs.getString("LOGIN");
        String name = rs.getString("NAME");
        LocalDate birthday = rs.getDate("BIRTHDAY").toLocalDate();
        Set<Integer> friendsIds = getSetOfFriendsIds(rs);
        return new User(id, friendsIds, email, login, name, birthday);
    }

    private Set<Integer> getSetOfFriendsIds(ResultSet rs) throws SQLException {
        Array idsArr = rs.getArray("FRIENDS_IDS");
        Object[] values = (Object[]) idsArr.getArray();
        if (values[0] == null) {
            return Collections.emptySet();
        }
        return Arrays.stream(values)
                .map(value -> (Integer) value)
                .collect(toSet());
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO USERS (EMAIL, LOGIN, NAME, BIRTHDAY) " +
                     "VALUES (?, ?, ?, ?)";
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
        String sql = "UPDATE USERS " +
                     "SET EMAIL = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ? " +
                     "WHERE ID = ?";
        if (jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()), user.getId()) == 0) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    @Override
    public boolean deleteById(int id) {
        String sql = "DELETE FROM USERS " +
                     "WHERE ID = ?";
        return jdbcTemplate.update(sql, id) > 0;
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = FIND_ALL +
                    "WHERE USERS.ID = ? " +
                    "GROUP BY USERS.ID";
        List<User> results = jdbcTemplate.query(sql, this::mapRowToUser, id);
        return results.isEmpty() ?
                Optional.empty() :
                Optional.of(results.get(0));
    }

    @Override
    public Collection<User> findFriendsById(int id) {
        String sql = FIND_ALL +
                    "INNER JOIN USER_FRIENDS U on USERS.ID = U.FRIEND_ID " +
                    "WHERE U.USER_ID = ? " +
                    "GROUP BY USERS.ID";
        return jdbcTemplate.query(sql, this::mapRowToUser, id);
    }

    @Override
    public boolean addFriend(int userId, int friendId) {
        String sql = "MERGE INTO USER_FRIENDS(USER_ID, FRIEND_ID) " +
                     "VALUES (?, ?)";
        return jdbcTemplate.update(sql, userId, friendId) > 0;
    }

    @Override
    public boolean removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM USER_FRIENDS " +
                     "WHERE USER_ID = ? AND FRIEND_ID = ?";
        return jdbcTemplate.update(sql, userId, friendId) > 0;
    }

    @Override
    public Collection<User> findCommonFriendsByIds(int firstUserId, int secondUserId) {
        String sql = FIND_ALL +
                    "JOIN USER_FRIENDS U on USERS.ID = U.FRIEND_ID " +
                    "JOIN USER_FRIENDS F on USERS.ID = F.FRIEND_ID " +
                    "WHERE U.USER_ID = ? AND F.USER_ID = ? " +
                    "GROUP BY USERS.ID";
        return jdbcTemplate.query(sql, this::mapRowToUser, firstUserId, secondUserId);
    }
}
