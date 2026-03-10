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
        return authData.createAuthData(user.username());
    }

    public AuthData login(UserData user) throws DataAccessException {
        //get user data of user with username
        UserData user1 = userData.getUser(user.username());
        //checks if the password input matches the password in userDataMap
        if (!user.password().equals(user1.password())) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
        //create and return authData
        return authData.createAuthData(user.username());
    }

    public void logout(String authToken) throws DataAccessException {
        //check if token matches the token in userDataMap
        if (authData.getAuthData(String.valueOf(authToken)) == null) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
        authData.deleteAuthToken(authToken);
    }
}
