package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static dataaccess.DatabaseManager.executeUpdate;

public class MySQLGameDataAccess implements GameDao {
    @Override
    public GameData getGameData(int gameID) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "select * from games where gameID=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        var statement = "INSERT INTO games VALUES (?, ?, ?, ?, ?)";
        executeUpdate(statement, game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), new ChessGame());
        return 0;
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        var result = new ArrayList<GameData>();
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM games";
            try(PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(readGame(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
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
        //return a
        return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
    }

    @Override
    public void joinGame(String playerColor, int gameID, String username) throws DataAccessException {
        //get the game with that gameID
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "select * from games where gameID=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
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
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE games";
        executeUpdate(statement);
    }

}
