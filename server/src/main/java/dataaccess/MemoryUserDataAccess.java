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
    public void clear() {
            // Clear all data
            userDataMap.clear();
    }

}
