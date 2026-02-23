package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryUserDataAccess implements UserDao {
    Map<String, UserData> userDataMap = new HashMap<>();

    //Create user
    @Override
    public void addUser(UserData user) throws DataAccessException {
        if (userDataMap.containsKey(user.username())) {
            throw new DataAccessException (403, "Error: already taken");
        }
        userDataMap.put(user.username(), user);
    }
    //get user
    @Override
    public UserData getUser(String username) {
        UserData user = userDataMap.get(username);
        if (user == null){
            throw new DataAccessException(401, "Error: unauthorized");
        }
        return user; //returns userdata
    }
    //clear
    @Override
    public void clear() {
        userDataMap.clear();
    }
}
