package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryAuthDataAccess implements AuthTokenDao {
    Map<String, AuthData> authDataMap = new HashMap<>();

    @Override
    public AuthData getAuthData(String authData) throws DataAccessException {
        return authDataMap.get(authData);
    }

    @Override
    public boolean isValidAuthToken(String authToken) throws DataAccessException {
        return authDataMap.get(authToken) !=null && authDataMap.get(authToken).authToken().equals(authToken);
    }

    @Override
    public AuthData createAuthData(String username) throws DataAccessException {
        //Generate a unique authentication token
        String token = UUID.randomUUID().toString();

        //creat and AuthData object
        AuthData authData = new AuthData(username, token);

        //store it in the map
        authDataMap.put(token,authData);
        return authData;
    }

    @Override
    public boolean deleteAuthToken(String authToken) throws DataAccessException {
        // delete authToken
        authDataMap.remove(authToken);
        // check if it was deleted
        if(authDataMap.containsKey(authToken)) {
            throw new DataAccessException("Error: Couldn't delete token");
        }
        return true;
    }

    @Override
    public boolean clear() throws DataAccessException {
        try {
            // Clear all data
            authDataMap.clear();

            return true;
        } catch (Exception e) {
            // Catch any unexpected errors and throw a custom exception
            throw new DataAccessException("Error: " + e.getMessage());
        }
    }
}
