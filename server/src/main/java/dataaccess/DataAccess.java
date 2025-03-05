package dataaccess;

import model.*;
import java.util.List;

public interface DataAccess {

    boolean addUser(UserData user) throws DataAccessExceptMod;

    UserData getUser(String username) throws DataAccessExceptMod;

    AuthData getAuthData(String authData) throws DataAccessExceptMod;

    boolean isValidAuthToken(String authToken) throws DataAccessExceptMod;

    AuthData createAuthData(String username) throws DataAccessExceptMod;

    boolean deleteAuthToken(String authToken) throws DataAccessExceptMod;

    void updateGame(GameData game) throws DataAccessExceptMod;

    GameData createGame(GameData game) throws DataAccessExceptMod;

    GameData getGameData(int gameID) throws DataAccessExceptMod;

    List<GameData> listGames() throws DataAccessExceptMod;

    boolean clear() throws DataAccessExceptMod;

}
