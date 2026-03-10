package dataaccess;

import model.AuthData;

import javax.xml.crypto.Data;

public interface AuthDao {

    AuthData getAuthData(String authToken) throws DataAccessException;

    AuthData createAuthData(String username) throws DataAccessException;

    void deleteAuthToken(String authToken) throws DataAccessException;

    void clear() throws DataAccessException;
}
