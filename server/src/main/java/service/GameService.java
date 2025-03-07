package service;

import dataaccess.AuthTokenDao;
import dataaccess.DataAccessException;
import dataaccess.GameDao;
import model.*;

import java.util.List;

public class GameService {
    private final GameDao gameData;
    private final AuthTokenDao authData;

    public GameService(GameDao gameData, AuthTokenDao authData) {
        this.gameData = gameData;
        this.authData = authData;
    }

    public List<GameData> listGames(String authToken) throws DataAccessException {
        // Check if token matches token in userDataMap
        if (!authData.isValidAuthToken(String.valueOf(authToken))) {
            throw new DataAccessException("Error: unauthorized");
        }
        return gameData.listGames();
    }
    // createGame
    // joinGame
}
