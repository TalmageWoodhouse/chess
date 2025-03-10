package dataaccess;

import model.GameData;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryGameDataAccess implements GameDao {
    Map<Integer, GameData> gameDataMap = new HashMap<>();
    private final AtomicInteger gameIdCounter = new AtomicInteger(1);

    @Override
    public void joinGame(int gameID, String playerColor, String username) throws DataAccessException {
        // get correct game from map with ID
        GameData game = gameDataMap.get(gameID);
        // check if the game exists
        if (game == null) {
            throw new DataAccessException(404, "game not found");
        }
        String blackPlayer = game.blackUsername();
        String whitePlayer = game.whiteUsername();

        if (playerColor.equals("BLACK") && blackPlayer == null) {
            blackPlayer = username;
        } else if (playerColor.equals("WHITE") && whitePlayer == null) {
            whitePlayer = username;
        }

        GameData updateGame = new GameData(
                game.gameID(),
                whitePlayer,
                blackPlayer,
                game.gameName(),
                game.game()
        );

        gameDataMap.put(gameID, updateGame);

    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        // generate a unique game ID
        int gameID = gameIdCounter.getAndIncrement();
        // create a new game with the ID
        GameData newGame = new GameData(
                gameID,
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                game.game()
        );

        // store game in memory
        gameDataMap.put(gameID, newGame);

        return gameID;
    }

    @Override
    public GameData getGameData(int gameID) throws DataAccessException {
        GameData gameData = gameDataMap.get(gameID);
        // check if game with ID exists
        if (gameData == null) {
            throw new DataAccessException(404, "Error: Game with ID " + gameID + " not found.");
        }

        return gameData;
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        // get all games from the memory
        List<GameData> gamesList = new ArrayList<>(gameDataMap.values());

        return gamesList;
    }

    @Override
    public void clear() {
            // Clear all data
            gameDataMap.clear();

    }
}
