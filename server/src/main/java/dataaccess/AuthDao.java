package dataaccess;

import model.AuthData;

public interface AuthDao {

    AuthData getAuthData(String authData) throws DataAccessException;
}
