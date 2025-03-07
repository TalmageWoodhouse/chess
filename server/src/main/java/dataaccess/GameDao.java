package dataaccess;

import model.GameData;
import java.util.List;

public interface GameDao {
    void updateGame(GameData game) throws DataAccessException;

    GameData createGame(GameData game) throws DataAccessException;

    GameData getGameData(int gameID) throws DataAccessException;

    List<GameData> listGames() throws DataAccessException;

    boolean clear() throws DataAccessException;
}
