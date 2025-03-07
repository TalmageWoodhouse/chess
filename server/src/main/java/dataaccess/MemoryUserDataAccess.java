package dataaccess;

import model.UserData;
import java.util.HashMap;
import java.util.Map;

public class MemoryUserDataAccess implements UserDao {
    Map<String, UserData> userDataMap = new HashMap<>();

    @Override
    public void addUser(UserData user) throws DataAccessException {
        // check to see if username already taken
        if (userDataMap.containsKey(user.username())) {
            throw new DataAccessException ("Error: already taken");
        }
        userDataMap.put(user.username(), user); // adds user to data map
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        UserData user = userDataMap.get(username);
        if (user == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        return user; // returns userData
    }

    @Override
    public boolean clear() throws DataAccessException {
        try {
            // Clear all data
            userDataMap.clear();

            return true;
        } catch (Exception e) {
            // Catch any unexpected errors and throw a custom exception
            throw new DataAccessException ("Error: " + e.getMessage());
        }
    }

}
