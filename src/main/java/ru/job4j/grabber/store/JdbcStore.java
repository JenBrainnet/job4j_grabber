package ru.job4j.grabber.store;

import org.apache.log4j.Logger;
import ru.job4j.grabber.model.Post;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcStore implements Store {

    private static final Logger LOGGER = Logger.getLogger(JdbcStore.class);
    private final Connection connection;

    public JdbcStore(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement statement = connection.prepareStatement(
                             "INSERT INTO posts(name, link, text, created) VALUES (?, ?, ?, ?) "
                                     + "ON CONFLICT (link) DO NOTHING",
                             PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getLink());
            statement.setString(3, post.getDescription());
            statement.setTimestamp(4, Timestamp.from(Instant.ofEpochMilli(post.getTime())));
            statement.executeUpdate();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("When insert a new post", e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM posts")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(createPost(resultSet));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("When find all posts", e);
        }
        return posts;
    }

    @Override
    public Optional<Post> findById(Long id) {
        Optional<Post> result = Optional.empty();
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM posts WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    result = Optional.of(createPost(resultSet));
                }
            }
        } catch (SQLException e) {
            LOGGER.error(String.format("When find a post by id: %d", id), e);
        }
        return result;
    }

    private Post createPost(ResultSet resultSet) throws SQLException {
        Post post = new Post();
        post.setId(resultSet.getLong("id"));
        post.setTitle(resultSet.getString("name"));
        post.setLink(resultSet.getString("link"));
        post.setDescription(resultSet.getString("text"));
        post.setTime(resultSet.getTimestamp("created").getTime());
        return post;
    }

}
