package service;

import dataaccess.*;
import model.*;

public class UserService {
        private final UserDao userData;
        private final AuthTokenDao authData;

    public UserService(UserDao userData, AuthTokenDao authData) {
        this.userData = userData;
        this.authData = authData;
    }

    public AuthData registerResult(UserData user) throws DataAccessException {
        // adds a user to database
        userData.addUser(user);
        // creates authToken for the user and returns authData
        return authData.createAuthData(user.username());
    }

    public AuthData loginResult(UserData user) throws DataAccessException {
        //get userData of user with username
        UserData user1 = userData.getUser(user.username());
        // checks if the password input matches the password in userDataMap
        if (!user.password().equals(user1.password())) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
        // create and return authData
        return authData.createAuthData(user.username());
    }

    public void logoutResult(String authToken) throws DataAccessException {
        // Check if token matches token in userDataMap
        if (!authData.isValidAuthToken(String.valueOf(authToken))) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
        authData.deleteAuthToken(authToken);
    }
}