package dataaccess;

import model.GameData;

import java.util.List;

public interface GameDao {
    void updateGame(GameData game) throws DataAccessExceptMod;

    GameData createGame(GameData game) throws DataAccessExceptMod;

    GameData getGameData(int gameID) throws DataAccessExceptMod;

    List<GameData> listGames() throws DataAccessExceptMod;

    boolean clear() throws DataAccessExceptMod;
}
