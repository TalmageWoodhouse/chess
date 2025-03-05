package dataaccess;

import chess.ChessGame;
import model.*;
import java.util.*;

public class MemoryDataAccess implements DataAccess {
    Map<String, UserData> userDataMap = new HashMap<>();
    Map<String, AuthData> authDataMap = new HashMap<>();
    Map<Integer, GameData> gameDataMap = new HashMap<>();

    @Override
    public boolean addUser(UserData user) throws DataAccessExceptMod {
        userDataMap.put(user.username(), user);
        return userDataMap.containsValue(user);
    }

    @Override
    public UserData getUser(String username) throws DataAccessExceptMod {
        return userDataMap.get(username);
    }

    @Override
    public AuthData getAuthData(String authData) throws DataAccessExceptMod {
        return authDataMap.get(authData);
    }

    @Override
    public boolean isValidAuthToken(String authToken) throws DataAccessExceptMod {
        return authDataMap.get(authToken) !=null && authDataMap.get(authToken).authToken().equals(authToken);
    }

    @Override
    public AuthData createAuthData(String username) throws DataAccessExceptMod {
        if (username == null || username.isBlank()) {
            throw new DataAccessExceptMod(500, "username is null or empty");
        }
        //Generate a unique authentication token
        String token = UUID.randomUUID().toString();
        //creat and AuthData object
        AuthData authData = new AuthData(username, token);

        //store it in the map
        authDataMap.put(token,authData);
        return authData;
    }

    @Override
    public boolean deleteAuthToken(String authToken) throws DataAccessExceptMod {
        authDataMap.remove(authToken);
        if(authDataMap.containsKey(authToken)) {
            throw new DataAccessExceptMod(500, "Error deleting token");
        }
        return true;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessExceptMod {
        int gameID = game.gameID();
        gameDataMap.remove(gameID);
        gameDataMap.put(gameID, game);
    }

    @Override
    public GameData createGame(GameData game) throws DataAccessExceptMod {
        if (game == null) {
            throw new DataAccessExceptMod(500, "Game data is null");
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
    public GameData getGameData(int gameID) throws DataAccessExceptMod {
        return gameDataMap.get(gameID);
    }

    @Override
    public List<GameData> listGames() throws DataAccessExceptMod {
        // get all games from the memory
       List<GameData> gamesList = new ArrayList<>(gameDataMap.values());
        // check if the list is empty
        if(gamesList.isEmpty()) {
           throw new DataAccessExceptMod(500, "list is empty");
       }
        return gamesList;
    }

    @Override
    public boolean clear() throws DataAccessExceptMod {
        try {
            // Clear all data
            userDataMap.clear();
            gameDataMap.clear();
            authDataMap.clear();

            return true;
        } catch (Exception e) {
            // Catch any unexpected errors and throw a custom exception
            throw new DataAccessExceptMod(500, "Error clearing memory: " + e.getMessage());
        }
    }

}
