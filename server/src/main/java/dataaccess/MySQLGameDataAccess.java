package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static dataaccess.DatabaseManager.executeUpdate;
import static dataaccess.DatabaseManager.getConnection;

public class MySQLGameDataAccess implements GameDao {

    public DatabaseManager dbManager;

    public MySQLGameDataAccess() {
        try {
            dbManager = new DatabaseManager();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GameData getGameData(int gameID) throws DataAccessException {
        try (Connection conn = getConnection()) {
            var statement = "select * from games where gameID=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(500, String.format("Error: Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        var statement = "INSERT INTO games (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
        String gameJson = new Gson().toJson(game.game());
        return executeUpdate(statement, game.whiteUsername(), game.blackUsername(), game.gameName(), gameJson);
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        var result = new ArrayList<GameData>();
        try (Connection conn = getConnection()) {
            var statement = "SELECT * FROM games";
            try(PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(readGame(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Error: Unable to read data: %s", e.getMessage()));
        }
        return result;
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        //read values from database
        var gameID = rs.getInt("gameID");
        var whiteUsername = rs.getString("whiteUsername");
        var blackUsername = rs.getString("blackUsername");
        var gameName = rs.getString("gameName");
        //get json column
        var gameJson = rs.getString("game");
        //convert json to java object
        ChessGame chessGame = new Gson().fromJson(gameJson, ChessGame.class);

        return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
    }

    @Override
    public void joinGame(String playerColor, int gameID, String username) throws DataAccessException {
        //get the game with that gameID
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM games WHERE gameID=?")) {
                ps.setInt(1, gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { return; }

                    GameData game = readGame(rs);
                    //find the column being updated
                    if (playerColor.equals("BLACK") && game.blackUsername() == null) {
                        var stmt = "UPDATE games SET blackUsername=? WHERE gameID=?";
                        executeUpdate(stmt, username, gameID);
                    }
                    if (playerColor.equals("WHITE") && game.whiteUsername() == null) {
                        var stmt = "UPDATE games SET whiteUsername=? WHERE gameID=?";
                        executeUpdate(stmt, username, gameID);
                    }
                }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Error: Unable to read data: %s", e.getMessage()));
        }
    }

    public void updateGame(int gameID, ChessGame game) throws DataAccessException {
        String statement = "UPDATE games SET game = ? WHERE gameID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {

            String gameJson = new Gson().toJson(game);
            ps.setString(1, gameJson);
            ps.setInt(2, gameID);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new DataAccessException(500, "Unable to update game");
            }

        } catch (SQLException e) {
            throw new DataAccessException(500,
                    String.format("Error: Unable to update game: %s", e.getMessage()));
        }
    }

    public void updateGameData(GameData gameData) throws DataAccessException {
        String statement = "UPDATE games SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameID = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(statement)) {

            ps.setString(1, gameData.whiteUsername());
            ps.setString(2, gameData.blackUsername());
            ps.setString(3, gameData.gameName());
            ps.setString(4, new Gson().toJson(gameData.game()));
            ps.setInt(5, gameData.gameID());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException(500, "Unable to update game data");
            }
        } catch (SQLException e) {
            throw new DataAccessException(500, "Unable to update game data");
        }
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE games";
        executeUpdate(statement);
    }

}

