package dataaccess;

import model.GameData;

import java.util.List;

public class MySQLGameDataAccess implements GameDao {
    @Override
    public GameData getGameData(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        return 0;
    }

    @Override
    public List<GameData> listGames() {
        return List.of();
    }

    @Override
    public void joinGame(String playerColor, int gameID, String username) throws DataAccessException {

    }

    @Override
    public void clear() {

    }

}
