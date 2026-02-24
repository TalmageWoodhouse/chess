package service;


import dataaccess.AuthDao;
import dataaccess.GameDao;
import dataaccess.UserDao;

public class ClearService {
    private final UserDao userData;
    private final AuthDao authData;
    private final GameDao gameData;

    public ClearService(UserDao userData, AuthDao authData, GameDao gameData) {
        this.gameData = gameData;
        this.userData = userData;
        this.authData = authData;
    }

    public void clear() {
        userData.clear();
        authData.clear();
        //gameData.clear();
    }
}
