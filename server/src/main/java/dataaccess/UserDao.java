package dataaccess;

import model.UserData;

public interface UserDao {
    boolean addUser(UserData user) throws DataAccessExceptMod;

    UserData getUser(String username) throws DataAccessExceptMod;

    boolean clear() throws DataAccessExceptMod;
}
