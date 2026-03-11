package dataaccess;

import model.GameData;

import java.util.List;

public interface GameDao {
    GameData getGameData(int gameID) throws DataAccessException;

    int createGame(GameData game) throws DataAccessException;

    List<GameData> listGames() throws DataAccessException;

    void joinGame(String playerColor, int gameID, String username) throws DataAccessException;

    void clear() throws DataAccessException;
}
