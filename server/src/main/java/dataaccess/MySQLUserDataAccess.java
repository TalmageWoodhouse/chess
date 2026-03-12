package dataaccess;

import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static dataaccess.DatabaseManager.executeUpdate;

public class MySQLUserDataAccess implements UserDao {

    public DatabaseManager dbManager;

    public MySQLUserDataAccess() {
        try {
            dbManager = new DatabaseManager();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addUser(UserData user) throws DataAccessException {
        try {
            var statement = "INSERT INTO users (username, password, email) values (?, ?, ?)";
            executeUpdate(statement, user.username(), user.password(), user.email());
        } catch (DataAccessException e) {
            throw new DataAccessException(500, String.format("Error: Unable to update database: %s", e.getMessage()));
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "select username, password, email from users where username=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        var dbUsername = rs.getString("username");
                        var password = rs.getString("password");
                        var email = rs.getString("email");
                        return new UserData(dbUsername, password, email);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Error: Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE users";
        executeUpdate(statement);
    }

}
