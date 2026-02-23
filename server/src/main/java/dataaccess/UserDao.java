package dataaccess;

import model.UserData;

public interface UserDao {
    UserData getUser(String username) throws DataAccessException;

    void addUser(String username) throws DataAccessException;

    void clear();
}
