package dataaccess;

import model.GameData;

import java.util.*;

public class MemoryGameDataAccess implements GameDao {
    Map<Integer, GameData> gameDataMap = new HashMap<>();

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        int gameID = game.gameID();
        gameDataMap.remove(gameID);
        gameDataMap.put(gameID, game);
    }

    @Override
    public GameData createGame(GameData game) throws DataAccessException {
        if (game == null) {
            throw new DataAccessException( "Game data is null");
        }
        // generate a unique game ID
        String gameID = UUID.randomUUID().toString();
        // create a new game with the ID
        GameData newGame = new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        // store game in memory
        gameDataMap.put(Integer.valueOf(gameID), newGame);

        return game;
    }

    @Override
    public GameData getGameData(int gameID) throws DataAccessException {
        return gameDataMap.get(gameID);
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        // get all games from the memory
        List<GameData> gamesList = new ArrayList<>(gameDataMap.values());
        // check if the list is empty
        if(gamesList.isEmpty()) {
            throw new DataAccessException( "list is empty");
        }
        return gamesList;
    }

    @Override
    public void clear() {
            // Clear all data
            gameDataMap.clear();

    }
}
