package dataaccess;

import model.AuthData;

public interface AuthTokenDao {
    AuthData getAuthData(String authData) throws DataAccessExceptMod;

    boolean isValidAuthToken(String authToken) throws DataAccessExceptMod;

    AuthData createAuthData(String username) throws DataAccessExceptMod;

    boolean deleteAuthToken(String authToken) throws DataAccessExceptMod;

    boolean clear() throws DataAccessExceptMod;
}
