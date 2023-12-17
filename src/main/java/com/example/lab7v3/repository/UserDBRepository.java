package com.example.lab7v3.repository;

import com.example.lab7v3.domain.User;
import com.example.lab7v3.repository.paging.Page;
import com.example.lab7v3.repository.paging.Pageable;
import com.example.lab7v3.repository.paging.PagingRepository;

import java.sql.*;
import java.util.*;

public class UserDBRepository implements PagingRepository<Long, User> {

    private final String url;
    private final String username;
    private final String password;

    public UserDBRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public Optional<User> findOne(Long longID) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("select * from users " +
                     "where id = ?");

        ) {
            statement.setInt(1, Math.toIntExact(longID));
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                User u = new User(firstName, lastName);
                u.setId(longID);
                return Optional.ofNullable(u);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public Iterable<User> findAll() {
        Set<User> users = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("select * from users");
             ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                User user = new User(firstName, lastName);
                user.setId(id);
                users.add(user);

            }
            return users;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Optional<User> save(User entity) {
        String sql = "INSERT INTO users (first_name, last_name) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                return Optional.empty();
            }
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    entity.setId(id);
                    return Optional.of(entity);
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> delete(Long aLong) {
        Optional<User> deleted = findOne(aLong);

        if (deleted.isPresent()) {
            try (Connection connection = DriverManager.getConnection(url, username, password)) {
                connection.setAutoCommit(false);

                // Delete friendships associated with the user
                String deleteFriendshipsSql = "DELETE FROM friendships WHERE user1_id = ? OR user2_id = ?";
                try (PreparedStatement deleteFriendshipsStatement = connection.prepareStatement(deleteFriendshipsSql)) {
                    deleteFriendshipsStatement.setLong(1, aLong);
                    deleteFriendshipsStatement.setLong(2, aLong);
                    deleteFriendshipsStatement.executeUpdate();
                }

                // Delete the user
                String deleteUserSql = "DELETE FROM users WHERE id = ?";
                try (PreparedStatement deleteUserStatement = connection.prepareStatement(deleteUserSql)) {
                    deleteUserStatement.setLong(1, aLong);
                    deleteUserStatement.executeUpdate();
                }

                connection.commit();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return deleted;
    }


    @Override
    public Optional<User> update(User entity) {
        Optional<User> existingUser = findOne(entity.getId());
        if (existingUser.isPresent()) {
            String sql = "UPDATE users SET first_name = ?, last_name = ? WHERE id = ?";
            try (Connection connection = DriverManager.getConnection(url, username, password);
                 PreparedStatement statement = connection.prepareStatement(sql)
            ) {
                statement.setString(1, entity.getFirstName());
                statement.setString(2, entity.getLastName());
                statement.setLong(3, entity.getId());
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected == 0) {
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return existingUser;
    }

    @Override
    public Page<User> findAllOnPage(Pageable pageable) {
        List<User> users = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement pageStatement = connection.prepareStatement("SELECT * FROM users LIMIT ? OFFSET ?");
             PreparedStatement countStatement = connection.prepareStatement("SELECT COUNT(*) AS count FROM users")
        ) {
            pageStatement.setInt(1, pageable.getPageSize());
            pageStatement.setInt(2, pageable.getPageSize() * pageable.getPageNr());

            try (
                    ResultSet pageResultSet = pageStatement.executeQuery();
                    ResultSet countResultSet = countStatement.executeQuery();
            ) {
                int count = 0;
                if (countResultSet.next()) {
                    count = countResultSet.getInt("count");
                }

                while (pageResultSet.next()) {
                    Long id = pageResultSet.getLong("id");
                    String firstName = pageResultSet.getString("first_name");
                    String lastName = pageResultSet.getString("last_name");
                    User user = new User(firstName, lastName);
                    user.setId(id);

                    users.add(user);
                }
                return new Page<>(users, count);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
