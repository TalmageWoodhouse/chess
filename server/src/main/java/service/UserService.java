package service;

import dataaccess.*;
import model.*;

public class UserService {
    private final UserDao userData;
    private final AuthDao authData;

    public UserService(UserDao userData, AuthDao authData) {
        this.userData = userData;
        this.authData = authData;
    }
    public AuthData register(UserData user) throws DataAccessException {
        //Add user to db
        userData.addUser(user);
        //create authToken for the user and returns authData
        return authData.;
    }
    public AuthData login(UserData user) throws DataAccessException {}
    public void logout(String authToken) throws DataAccessException {}
}
