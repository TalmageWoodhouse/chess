package service;

import dataaccess.AuthDao;
import dataaccess.DataAccessException;
import dataaccess.GameDao;
import model.AuthData;
import model.GameData;

import java.util.List;


public class GameService {

    private final GameDao gameData;
    private final AuthDao authData;

    public GameService (GameDao gameData, AuthDao authData) {
        this.gameData = gameData;
        this.authData = authData;
    }
//    public ListGamesResult listGames(ListGamesRequest listGamesRequest) {}
    public int createGame(GameData game, String authToken) throws DataAccessException {
        // validate auth
        if (!authData.isValidAuthToken(String.valueOf(authToken))) {
            throw new DataAccessException(401, "Error: unauthorized");
        }

        return gameData.createGame(game);
    }
    public void joinGame(String playerColor, String authToken, int gameID) throws DataAccessException {
        // validate auth
        if (!authData.isValidAuthToken(String.valueOf(authToken))) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
        // retrieve gameData
        GameData gameD = gameData.getGameData(gameID);
        //check if game taken
        if (playerColor.equals("BLACK")) {
            if (gameD.blackUsername() != null) {
                throw new DataAccessException(403, "Error: already taken");
            }
        }
        else if (playerColor.equals("WHITE")) {
            if (gameD.blackUsername() != null) {
                throw new DataAccessException(403, "Error: already taken");
            }
        } else {
            throw new DataAccessException(400, "Error: bad request");
        }
        //get username with token
        AuthData auth = authData.getAuthData(authToken);
        //join game
        gameData.joinGame(playerColor, gameID, auth.username());
    }

    public List<GameData> listGames(String authToken) throws DataAccessException {
        //validate auth
        if (!authData.isValidAuthToken(String.valueOf(authToken))) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
        //get game list
        return gameData.listGames();
    }

    public void clear() {
        gameData.clear();
    }
}
