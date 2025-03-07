package dataaccess;

import model.AuthData;

public interface AuthTokenDao {
    //
    AuthData getAuthData(String authData) throws DataAccessException;

    boolean isValidAuthToken(String authToken) throws DataAccessException;

    AuthData createAuthData(String username) throws DataAccessException;

    boolean deleteAuthToken(String authToken) throws DataAccessException;

    boolean clear() throws DataAccessException;
}
