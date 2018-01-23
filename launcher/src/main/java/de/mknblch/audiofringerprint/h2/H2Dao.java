package de.mknblch.audiofringerprint.h2;

import de.mknblch.audiofingerprint.Hash;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.server.web.WebServer;
import org.h2.tools.Server;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.sql.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author mknblch
 */
public class H2Dao {

    private static final Logger LOGGER = getLogger(H2Dao.class);

    public static final String INSERT_STATEMENT = "INSERT INTO tracks (name) VALUES (?)";

    private final Connection connection;
    private final Path path;

    public H2Dao(Path path) throws SQLException {
        this.path = path;
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(String.format("jdbc:h2:file:%s", path.toAbsolutePath().toString()));
        connection = ds.getConnection();
        createTables();
    }

    public Server startWebServer() throws SQLException {
        WebServer webServer = new WebServer();
        Server web = new Server(webServer, "-webPort", "8080");
        web.start();
        final String url = webServer.addSession(connection);
        System.out.println("url = " + url);
        return web;
    }

    private ResultSet executeQuery(String query) {
        final Statement statement;
        try {
            statement = connection.createStatement();
            LOGGER.trace("query {}", query);
            return statement.executeQuery(query);
        } catch (SQLException e) {
            LOGGER.error("Error", e);
        }
        return null;
    }

    private void execute(String query) {
        try {
            final Statement statement = connection.createStatement();
            LOGGER.trace("query {}", query);
            statement.execute(query);
            statement.close();
        } catch (SQLException e) {
            LOGGER.error("Error", e);
        }
    }

    public boolean trackExists(String track) {
        final ResultSet resultSet = executeQuery("SELECT id FROM tracks WHERE name='" + track + "'");
        try {
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int addOrGetTrackId(String track) {
        final ResultSet resultSet = executeQuery("SELECT id FROM tracks WHERE name='" + track + "'");
        try {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }

            PreparedStatement preparedStatement = connection.prepareStatement(INSERT_STATEMENT, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, track);
            preparedStatement.executeUpdate();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getTrack(int id) {
        final ResultSet resultSet = executeQuery("SELECT name FROM tracks WHERE id=" + id);
        try {
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "?";
    }

    public ResultSet findHashes(Hash[] hashes) {
        final StringBuilder builder = new StringBuilder();
        for (Hash hash : hashes) {
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(hash.hash());
        }
        return executeQuery("SELECT track, ts FROM hashes WHERE hash IN (" + builder.toString() + ")");
    }

    public void createTables() {
        execute("CREATE TABLE IF NOT EXISTS tracks (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(512))");

        execute("CREATE TABLE IF NOT EXISTS hashes (" +
                "track INT, " +
                "ts INT, " +
                "hash INT)");
        execute("CREATE INDEX IF NOT EXISTS idx ON hashes (hash)");
        execute("CREATE INDEX IF NOT EXISTS idx2 ON tracks (id)");
    }

    public void deleteDB() {
        LOGGER.debug("deleting tracks");
        execute("DELETE FROM hashes");
        execute("DELETE FROM tracks");

        LOGGER.debug("recreating database");
        createTables();
    }

    public BatchInsert batchInsert(String track) throws SQLException {
        return new BatchInsert(addOrGetTrackId(track));
    }

    public class BatchInsert {

        private final StringBuilder builder;
        private final int trackId;

        public BatchInsert(int trackId) throws SQLException {
            this.trackId = trackId;
            builder = new StringBuilder();
        }

        public void insertHash(Hash[] hashes) {
            if (null == hashes || hashes.length == 0) {
                return;
            }
            final StringBuilder sub = new StringBuilder();
            for (Hash hash : hashes) {
                if (sub.length() > 0) {
                    sub.append(",");
                }
                sub.append("(").append(trackId).append(",")
                        .append(hash.timestamp).append(",")
                        .append(hash.hash()).append(")");
            }
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append(sub.toString());
        }

        public void execute() throws SQLException {
            H2Dao.this.execute("INSERT INTO hashes (track, ts, hash) VALUES " + builder.toString());
        }
    }
}
