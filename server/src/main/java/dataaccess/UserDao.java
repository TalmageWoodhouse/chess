package dataaccess;

import model.UserData;

public interface UserDao {
    void addUser(UserData user) throws DataAccessException;

    UserData getUser(String username) throws DataAccessException;

    void clear();
}
