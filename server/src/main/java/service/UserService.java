package service;

import dataaccess.*;
import model.*;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private final UserDao userDao;
    private final AuthDao authDao;

    public UserService(UserDao userDao, AuthDao authDao) {
        this.userDao = userDao;
        this.authDao = authDao;
    }

    public AuthData register(UserData user) throws DataAccessException {
        //check if user already taken
        if (userDao.getUser(user.username()) != null) {
            throw new DataAccessException(403, "Error: already taken");
        }
        //hash password before storing
        String hashedPass = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        UserData hashedUser = new UserData(user.username(), hashedPass, user.email());
        //Add user to db
        userDao.addUser(hashedUser);
        //create authToken for the user and returns authData
        return authDao.createAuthData(hashedUser.username());
    }

    public AuthData login(UserData user) throws DataAccessException {
        //get user data of user with username
        UserData stored = userDao.getUser(user.username());
        //checks if the password input matches the password in userDataMap
        if (BCrypt.checkpw(user.password(), stored.password())) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
        //create and return authData
        return authDao.createAuthData(user.username());
    }

    public void logout(String authToken) throws DataAccessException {
        //check if token matches the token in userDataMap
        if (authDao.getAuthData(String.valueOf(authToken)) == null) {
            throw new DataAccessException(401, "Error: unauthorized");
        }
        authDao.deleteAuthToken(authToken);
    }
}
