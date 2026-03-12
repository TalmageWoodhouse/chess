package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryAuthDataAccess implements AuthDao {
    Map<String, AuthData> authDataMap = new HashMap<>();

    @Override
    public AuthData getAuthData(String authToken) throws DataAccessException {
        return authDataMap.get(authToken);
    }

    @Override
    public AuthData createAuthData(String username) throws DataAccessException {
        //generate a unique auth token
        String token = UUID.randomUUID().toString();
        //create an authData object
        AuthData authData = new AuthData(token, username);
        //store it in the map
        authDataMap.put(token, authData);
        return authData;
    }

    @Override
    public void deleteAuthToken(String authToken) throws DataAccessException {
        authDataMap.remove(authToken);
    }

    @Override
    public void clear() {
        authDataMap.clear();
    }
}
