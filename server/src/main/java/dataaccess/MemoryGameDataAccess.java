package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryGameDataAccess implements GameDao {
    Map<Integer, GameData> gameDataMap = new HashMap<>();
    private final AtomicInteger gameIdCounter = new AtomicInteger(1);

    @Override
    public GameData getGameData(int gameID) throws DataAccessException {
        GameData gameData = gameDataMap.get(gameID);

        if (gameData == null) {
            throw new DataAccessException(404, "Error: Game with ID " + gameID + "not found.");
        }

        return gameData;
    }

    @Override
    public int createGame(GameData game) throws DataAccessException {
        //generate a unique game ID
        int gameID = gameIdCounter.getAndIncrement();
        //create a new game with the ID
        GameData newGame = new GameData(
                gameID,
                game.whiteUsername(),
                game.blackUsername(),
                game.gameName(),
                game.game()
        );
        //store game in memory
        gameDataMap.put(gameID, newGame);
        return gameID;
    }

    @Override
    public List<GameData> listGames(){
        //get all games from memory
        List<GameData> gameList = new ArrayList<>(gameDataMap.values());

        return List.of();
    }

    @Override
    public void joinGame(String playerColor, int gameID, String username) throws DataAccessException {

    }

    @Override
    public void clear() {
        gameDataMap.clear();
    }
}
