package dataaccess;

import model.GameData;
import java.util.List;

public interface GameDao {

    void joinGame(int gameID, String playerColor, String username) throws DataAccessException;

    int createGame(GameData game) throws DataAccessException;

    GameData getGameData(int gameID) throws DataAccessException;

    List<GameData> listGames() throws DataAccessException;

    void clear();
}
