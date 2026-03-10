package dataaccess;

import model.AuthData;

public class MySQLAuthDataAccess implements AuthDao {

    @Override
    public AuthData getAuthData(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public boolean isValidAuthToken(String authToken) throws DataAccessException {
        return false;
    }

    @Override
    public AuthData createAuthData(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuthToken(String authToken) throws DataAccessException {

    }

    @Override
    public void clear() {

    }
}
