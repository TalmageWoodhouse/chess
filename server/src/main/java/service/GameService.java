package service;

import dataaccess.*;
import model.*;
import results.*;
import java.util.List;

public class GameService {
    private final GameDao gameDao;
    private final AuthTokenDao authData;

    public GameService(GameDao gameDao, AuthTokenDao authData) {
        this.gameDao = gameDao;
        this.authData = authData;
    }

    public List<GameData> listGames(String authToken) throws DataAccessException {
        // Check if token matches token in userDataMap
        if (!authData.isValidAuthToken(String.valueOf(authToken))) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
        return gameDao.listGames();
    }

    public CreateGameResult createGame(GameData gameName, String authToken) throws DataAccessException {
        // check if valid token
        if (!authData.isValidAuthToken(String.valueOf(authToken))) {
            throw new DataAccessException(401, "Error: unauthorized");
        }// Check if good token
        Integer gameID = gameDao.createGame(gameName);
        return new CreateGameResult(gameID);
    }

    public void joinGame(int gameID, String authToken, String playerColor) throws DataAccessException {
        // check if valid token
        if (!authData.isValidAuthToken(String.valueOf(authToken))) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
        // retrieve the gameData
        GameData gameData =  gameDao.getGameData(gameID);
        // check if game taken
        if (playerColor.equals("WHITE")) {
            if (gameData.whiteUsername() != null) {
                throw new DataAccessException(403, "Error: already taken");
            }
        }
        else if (playerColor.equals("BLACK")) {
            if (gameData.blackUsername() != null) {
                throw new DataAccessException(403, "Error: already taken");
            }
        } else {
            throw new DataAccessException(400, "Error: bad request");
        }
        // get username with token
        AuthData auth = authData.getAuthData(authToken);
        // join game
        gameDao.joinGame(gameID, playerColor, auth.username());
    }



}
