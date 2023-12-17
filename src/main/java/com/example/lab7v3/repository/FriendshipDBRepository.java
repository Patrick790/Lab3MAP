package com.example.lab7v3.repository;

import com.example.lab7v3.domain.Friendship;
import com.example.lab7v3.domain.Tuple;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class FriendshipDBRepository implements Repository<Tuple<Long, Long>, Friendship> {

    private final String url;
    private final String username;
    private final String password;

    public FriendshipDBRepository(String url, String username, String password){
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public Optional<Friendship> findOne(Tuple<Long, Long> id) {
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM friendships WHERE user1_id = ? AND user2_id = ? OR user1_id = ? AND user2_id = ?"
             )
        ){
            statement.setLong(1, id.getLeft());
            statement.setLong(2, id.getRight());
            statement.setLong(3, id.getRight());
            statement.setLong(4, id.getLeft());
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                Long user1Id = resultSet.getLong("user1_id");
                Long user2Id = resultSet.getLong("user2_id");
                LocalDateTime friendsFrom = resultSet.getTimestamp("friends_from").toLocalDateTime();
                Friendship friendship = new Friendship();
                friendship.setId(id);
                friendship.setFriendsFrom(friendsFrom);
                return Optional.of(friendship);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();

    }

    @Override
    public Iterable<Friendship> findAll() {
        Set<Friendship> friendships = new HashSet<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM friendships");
        ResultSet resultSet = statement.executeQuery();
        ) {
            while(resultSet.next()) {
                Long user1Id = resultSet.getLong("user1_id");
                Long user2Id = resultSet.getLong("user2_id");
                LocalDateTime friendsFrom = resultSet.getTimestamp("friends_from").toLocalDateTime();

                Friendship friendship = new Friendship();
                friendship.setId(new Tuple<>(user1Id, user2Id));
                friendship.setFriendsFrom(friendsFrom);

                friendships.add(friendship);
            }
            return friendships;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Friendship> save(Friendship entity) {
        String sql = "INSERT INTO friendships (user1_id, user2_id, friends_from) VALUES (?, ?, ?)";
        try(Connection connection = DriverManager.getConnection(url, username, password);
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setLong(1, entity.getId().getLeft());
            statement.setLong(2, entity.getId().getRight());
            statement.setTimestamp(3, Timestamp.valueOf(entity.getFriendsFrom()));
            int affectedRows = statement.executeUpdate();
            if(affectedRows == 0){
                return Optional.empty();
            }
            return Optional.of(entity);
        } catch(SQLException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Friendship> delete(Tuple<Long, Long> id) {
        Optional<Friendship> deleted = findOne(id);
        if(deleted.isPresent()){
            String sql = "DELETE FROM friendships WHERE user1_id = ? AND user2_id = ? OR user1_id = ? AND user2_id = ?";
            try(Connection connection = DriverManager.getConnection(url, username, password);
                PreparedStatement statement = connection.prepareStatement(sql);
            ) {
                statement.setLong(1, id.getLeft());
                statement.setLong(2, id.getRight());
                statement.setLong(3, id.getRight());
                statement.setLong(4, id.getLeft());
                int rowsAffected = statement.executeUpdate();
                if(rowsAffected == 0){
                    return Optional.empty();
                }
            } catch (SQLException e){
                throw new RuntimeException(e);
            }
        }
        return deleted;
    }

    @Override
    public Optional<Friendship> update(Friendship entity) {
        return Optional.empty();
    }
}
