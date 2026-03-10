package dataaccess;

import model.AuthData;
import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import static dataaccess.DatabaseManager.executeUpdate;

public class MySQLAuthDataAccess implements AuthDao {

    @Override
    public AuthData getAuthData(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "select authtoken, username from auths where authToken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        authToken = rs.getString("username");
                        var username = rs.getString("password");
                        return new AuthData(authToken, username);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public AuthData createAuthData(String username) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        var statement = "INSERT INTO auths (authToken, username)";
        executeUpdate(statement);
        return new AuthData(authToken, username);
    }

    @Override
    public void deleteAuthToken(String authToken) throws DataAccessException {
        var statement = "Delete from auths where authToken=?";
        executeUpdate(statement);
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "truncate auths";
        executeUpdate(statement);
    }
}
